package com.liewmanchoi.handler;

import com.liewmanchoi.server.Server;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
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
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
  private Server server;

  public HeartbeatHandler(Server server) {
    this.server = server;
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    // 在超过一定的阈值一直没有接收到客户端发送心跳信息，直接关闭连接
    NioSocketChannel channel = (NioSocketChannel) ctx.channel();

    // 获取channel对应的clientID
    Attribute<String> attribute = channel.attr(server.getAttributeKey());
    String clientID = attribute.get();

    if (clientID != null) {
      server.shutdownConnection(clientID);
    } else {
      channel.shutdown();
    }
    log.warn(">>>   长时间没有收到客户端[{}]发送的心跳消息，关闭对应的连接   <<<", clientID);

    super.userEventTriggered(ctx, evt);
  }
}
