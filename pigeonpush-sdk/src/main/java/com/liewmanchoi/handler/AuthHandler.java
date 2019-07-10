package com.liewmanchoi.handler;

import com.liewmanchoi.client.Client;
import com.liewmanchoi.constant.NetConstant;
import com.liewmanchoi.domain.message.Message;
import com.liewmanchoi.domain.message.Message.AuthState;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wangsheng
 * @date 2019/7/9
 */
@Slf4j
public class AuthHandler extends ChannelInboundHandlerAdapter {
  private Client client;

  public AuthHandler(Client client) {
    this.client = client;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    // 成功建立连接，则向推送服务器发送认证请求
    String clientId = client.getDeviceInfo().getDeviceId();
    String token = client.getDeviceInfo().getDeviceToken();
    Message message = Message.buildAuthReq(clientId, token);

    log.info("向推送服务器发送认证请求");
    ctx.writeAndFlush(message);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    // 读取消息
    Message message = (Message) msg;

    if (message.getType() == Message.AUTH_RES) {
      // 如果是认证响应消息
      int code = (Integer) message.getAttachment().get("code");
      if (code == AuthState.SUCCESS) {
        // 如果认证成功，则删除AuthHandler
        ctx.pipeline().remove(this);
      } else {
        // 如果认证失败，则重新请求鉴权服务
        log.error("认证失败，重新请求鉴权");
        client.authenticateAndConnect(client.getUrl());
      }
    }

    super.channelRead(ctx, msg);
  }
}
