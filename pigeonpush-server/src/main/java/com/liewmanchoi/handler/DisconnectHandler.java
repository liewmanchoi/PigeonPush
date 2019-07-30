package com.liewmanchoi.handler;

import com.liewmanchoi.server.Server;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务端心跳拦截器
 *
 * @author wangsheng
 * @date 2019/7/21
 */
@Slf4j
@Sharable
public class DisconnectHandler extends ChannelInboundHandlerAdapter {
  private Server server;

  public DisconnectHandler(Server server) {
    this.server = server;
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    // 如果事件由IdleStateHandler触发
    if (evt instanceof IdleStateEvent) {
      // 在超过一定的阈值一直没有接收到客户端发送心跳信息，直接关闭连接
      final String clientID = getClientID(ctx);
      log.warn(">>>   长时间没有收到客户端[{}]发送的心跳消息，连接将关闭   <<<", clientID);
      closeConnection(ctx);
    }

    super.userEventTriggered(ctx, evt);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    // 连接被对端关闭，则应该直接关闭连接
    final String clientID = getClientID(ctx);
    log.warn(">>>   与客户端[{}]的连接被关闭   <<<", clientID);
    closeConnection(ctx);

    super.channelInactive(ctx);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    // 如果发生了异常，直接关闭连接
    final String clientID = getClientID(ctx);
    log.warn(">>>   与客户端[{}]的连接发生异常，连接将关闭   <<<", clientID, cause);

    closeConnection(ctx);

    super.exceptionCaught(ctx, cause);
  }

  private String getClientID(ChannelHandlerContext ctx) {
    final Channel channel = ctx.channel();
    // 获取channel对应的clientID
    Attribute<String> attribute = channel.attr(server.getAttributeKey());
    return attribute.get();
  }

  private void closeConnection(ChannelHandlerContext ctx) {
    String clientID = getClientID(ctx);

    if (clientID != null) {
      server.closeConnection(clientID);
      return;
    }

    Channel channel = ctx.channel();
    if (channel != null && channel.isOpen()) {
      channel.close().syncUninterruptibly();
      log.info(">>>   关闭连接   <<<");
    }
  }
}
