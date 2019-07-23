package com.liewmanchoi.handler;

import com.liewmanchoi.domain.message.Message;
import com.liewmanchoi.server.Server;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wangsheng
 * @date 2019/7/21
 */
@Slf4j
@Sharable
public class MessageHandler extends ChannelInboundHandlerAdapter {
  private Server server;

  public MessageHandler(Server server) {
    this.server = server;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    Message message = (Message) msg;
    final String clientId = message.getClientId();

    switch (message.getType()) {
      case Message.PING:
        // 接收到PING消息
        // 1. 回复PONG消息
        ctx.channel().writeAndFlush(Message.buildPONG(clientId));
        // 2. RPC调用，拉取clientID的所有未读消息
        server.acceptPullRequest(clientId);
        break;
      case Message.ACK:
        // 接收到ACK消息，进行处理
        server.handleACK(clientId, message.getACKMessageID());
        break;
      default:
        log.warn(">>>   收到了其他类型的消息   <<<");
        break;
    }

    // 回收Message
    message.recycle();
    super.channelRead(ctx, msg);
  }
}
