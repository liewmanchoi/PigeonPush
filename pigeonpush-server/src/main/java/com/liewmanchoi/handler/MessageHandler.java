package com.liewmanchoi.handler;

import com.liewmanchoi.domain.message.Message;
import com.liewmanchoi.server.Server;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wangsheng
 * @date 2019/7/21
 */
@Slf4j
@Sharable
public class MessageHandler extends SimpleChannelInboundHandler<Message> {
  private Server server;

  public MessageHandler(Server server) {
    this.server = server;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
    final String clientId = msg.getClientId();

    switch (msg.getType()) {
      case Message.PING:
        // 接收到PING消息
        // 1. 回复PONG消息
        log.info(">>>   接收到客户端[{}]发送的心跳消息   <<<", ctx.channel().remoteAddress());
        ctx.channel().writeAndFlush(Message.buildPONG(clientId));
        log.info(">>>   向客户端[{}]回复心跳消息   <<<", clientId);
        // 2. RPC调用，拉取clientID的所有未读消息
        server.acceptPullRequest(clientId);
        break;
      case Message.ACK:
        // 接收到ACK消息，进行处理
        log.info(">>>   接收到客户端[{}]发送的对消息[{}]的确认ACK   <<<", clientId, msg.getACKMessageID());
        server.handleACK(clientId, msg.getACKMessageID());
        break;
      default:
        log.warn(">>>   收到了其他类型的消息   <<<");
        break;
    }

    // 回收Message
    msg.recycle();
  }
}
