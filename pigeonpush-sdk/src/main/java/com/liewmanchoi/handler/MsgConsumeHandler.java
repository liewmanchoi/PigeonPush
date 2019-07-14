package com.liewmanchoi.handler;

import com.liewmanchoi.client.Client;
import com.liewmanchoi.domain.message.Message;
import com.liewmanchoi.domain.message.PushMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;

/**
 * 负责消息消费与ACK回传，保证消费的幂等性
 *
 * @author wangsheng
 * @date 2019/7/10
 */
@AllArgsConstructor
public class MsgConsumeHandler extends ChannelInboundHandlerAdapter {

  private Client client;

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    Message message = (Message) msg;
    // 只针对push消息处理
    if (message.getType() == Message.PUSH) {
      PushMessage pushMessage = message.getPushMessage();
      // 回收Message对象
      message.recycle();

      int messageId = pushMessage.getMessageId();
      if (client.hasConsumed(messageId)) {
        // 如果消息已经被消费，则丢弃之
        return;
      }

      // 否则，缓存消息ID，并且调用消息处理方法
      client.putInCache(messageId);

      // 回送消息确认ACK
      // 直接利用原来的ack
      String clientId = client.getDeviceInfo().getDeviceId();
      Message ackMsg = Message.buildACK(clientId, messageId);
      ctx.writeAndFlush(ackMsg);

      // 调用用户自定义的消息处理接口
      client.getMessageProcessor().process(pushMessage);
    }

    super.channelRead(ctx, msg);
  }
}
