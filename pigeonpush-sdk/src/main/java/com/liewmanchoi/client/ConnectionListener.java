package com.liewmanchoi.client;

import com.liewmanchoi.constant.NetConstant;
import com.liewmanchoi.handler.AuthHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wangsheng
 * @date 2019/7/9
 */
@Slf4j
public class ConnectionListener implements ChannelFutureListener {
  private static int count = 0;
  private Client client;

  ConnectionListener(Client client) {
    this.client = client;
  }

  @Override
  public void operationComplete(ChannelFuture future) throws Exception {
    if (!future.isSuccess()) {
      log.error("连接到服务器[{}]失败", future.channel().remoteAddress());
      // 发起重连接
      final EventLoop loop = future.channel().eventLoop();
      if (count < NetConstant.HEARTBEAT_TIMEOUT_MAX_TIMES) {
        // 调度3s后进行重连
        loop.schedule(() -> client.doConnect(), NetConstant.RECONNECT_TIMEOUT, TimeUnit.SECONDS);
        ++count;
        log.warn("第[{}]次重连服务器...", count);
        return;
      }

      // 否则，重新发起鉴权，获得新的服务器地址
      loop.schedule(
          () -> client.authenticateAndConnect(client.getUrl()),
          NetConstant.REAUTHENTICATE_TIMEOUT,
          TimeUnit.SECONDS);
      log.warn("重新发起鉴权连接...");
      // 计数器清零
      count = 0;

      return;
    }

    // 如果成功建立连接，必须在MessageDecoder后添加AuthHandler用于握手认证
    ChannelPipeline pipeline = client.getFutureChannel().pipeline();

    // 首先验证"AuthHandler"是否存在
    if (pipeline.get(AuthHandler.class) == null) {
      pipeline.addAfter("MessageDecoder", "AuthHandler", new AuthHandler(client));
    }
  }


}
