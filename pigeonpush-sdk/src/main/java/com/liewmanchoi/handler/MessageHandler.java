package com.liewmanchoi.handler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.liewmanchoi.api.MessageListener;
import com.liewmanchoi.client.Client;
import com.liewmanchoi.domain.message.Message;
import com.liewmanchoi.domain.message.PushMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * 负责消息消费与ACK回传，保证消费的幂等性
 *
 * @author wangsheng
 * @date 2019/7/10
 */
@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<Message> {
  /** 用于执行消息回调的线程池 */
  private ThreadPoolExecutor executor =
      new ThreadPoolExecutor(
          2,
          2,
          0L,
          TimeUnit.HOURS,
          new LinkedBlockingQueue<>(),
          new ThreadFactoryBuilder().setNameFormat("MessageHandler-Biz-ThreadPool-%d").build());

  private Client client;

  public MessageHandler(Client client) {
    this.client = client;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
    // 只针对push消息处理
    if (msg.getType() == Message.PUSH) {
      PushMessage pushMessage = msg.getPushMessage();
      Long messageId = pushMessage.getMessageId();
      log.info(">>>   客户端接收到id=[{}]的推送消息   <<<", messageId);
      if (client.hasConsumed(messageId)) {
        // 如果消息已经被消费，则丢弃之
        return;
      }

      // 否则，缓存消息ID，并且调用消息处理方法
      client.putInCache(messageId);

      // 回送消息确认ACK
      // 直接利用原来的ack
      String clientId = client.getClientId();
      Message ackMsg = Message.buildACK(clientId, messageId);
      // 必须使用Channel调用writeAndFlush()方法
      ctx.channel().writeAndFlush(ackMsg);

      // 使用线程池执行消息回调函数
      MessageListener listener = client.getListener();
      if (listener != null) {
        executor.submit(() -> listener.process(pushMessage));
      }
    }

    msg.recycle();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.error(">>>   客户端[{}]发生异常   <<<", client.getClientId(), cause);
  }
}
