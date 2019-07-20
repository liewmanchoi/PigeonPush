package com.liewmanchoi.handler;

import com.liewmanchoi.client.Client;
import com.liewmanchoi.constant.NetConstant;
import com.liewmanchoi.domain.message.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wangsheng
 * @date 2019/7/9
 */
@Slf4j
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

  /**
   * 计数器
   */
  private int count = 0;
  private Client client;

  public HeartbeatHandler(Client client) {
    this.client = client;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    // 收到服务器发来的消息，则计数器清零
    count = 0;
    super.channelRead(ctx, msg);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    // 如果服务端主动从远端关闭连接，也要尝试重连
    client.doConnect();
    super.channelInactive(ctx);
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {
      if (count < NetConstant.HEARTBEAT_TIMEOUT_MAX_TIMES) {
        Channel channel = ctx.channel();
        log.warn("SDK在[{}]s内均没有收到数据，主动发送心跳至服务器[{}]", NetConstant.HEARTBEAT_TIMEOUT,
            channel.remoteAddress());
        // 发送PING消息
        // 消息必须由channel（而不是ctx）来发送，这样才能在pipeline中完整地流转
        channel.writeAndFlush(Message.buildPING(client.getDeviceInfo().getDeviceId()));
        // 递增计数器
        ++count;
      } else {
        // 超过阈值次数仍然没有收到服务端的响应，开始重连（重连的逻辑已经在ConnectionListener中处理好了）
        client.doConnect();
      }
    }

    super.userEventTriggered(ctx, evt);
  }
}
