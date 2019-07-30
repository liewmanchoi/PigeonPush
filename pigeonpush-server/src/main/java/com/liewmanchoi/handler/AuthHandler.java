package com.liewmanchoi.handler;

import com.liewmanchoi.domain.message.Message;
import com.liewmanchoi.domain.message.Message.AuthState;
import com.liewmanchoi.server.Server;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;

/**
 * 动态鉴权拦截器
 *
 * @author wangsheng
 * @date 2019/7/21
 */
@Slf4j
@Sharable
public class AuthHandler extends ChannelInboundHandlerAdapter {
  private Server server;

  public AuthHandler(Server server) {
    this.server = server;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    // 鉴权模块，收到的消息必须为Message.AUTH_REQ
    Message message = (Message) msg;
    String clientID = message.getClientId();

    SocketChannel channel = (SocketChannel) ctx.channel();

    // 如果消息不是AUTH_REQ类型，直接关闭连接
    if (message.getType() != Message.AUTH_REQ) {
      log.warn(">>>   没有收到客户端[{}]发送的鉴权消息，连接将关闭 <<<", clientID);
      channel.close();
    } else {

      String token = message.getToken();

      if (!server.checkToken(clientID, token)) {
        // 如果鉴权不通过
        // 1. 发送鉴权失败信息给客户端
        ChannelFuture channelFuture =
            channel.writeAndFlush(Message.buildAuthRes(clientID, AuthState.FAILURE));
        channelFuture.addListener(
            (ChannelFutureListener)
                future -> {
                  if (future.isDone()) {
                    // 2. 直接关闭连接
                    channel
                        .close()
                        .addListener(
                            (ChannelFutureListener)
                                future1 -> log.warn(">>>   客户端[{}]鉴权未通过，连接已关闭 <<<", clientID));
                  }
                });
      } else {
        log.info(">>>   客户端[{}]鉴权通过 <<<", clientID);

        // 如果鉴权通过，则将clientID信息附加到channel上
        Attribute<String> attribute = channel.attr(server.getAttributeKey());
        attribute.set(clientID);
        // 开始连接建立后的步骤
        server.afterEstablishConnection(clientID, channel);
        // 动态删除本拦截器
        ctx.pipeline().remove(this);
        log.info(">>>   删除鉴权拦截器 <<<");
        // 发送AUTH_RES信息给客户端
        channel.writeAndFlush(Message.buildAuthRes(clientID, AuthState.SUCCESS));
      }
    }

    super.channelRead(ctx, msg);
  }
}
