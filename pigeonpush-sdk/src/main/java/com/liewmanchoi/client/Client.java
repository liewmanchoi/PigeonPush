package com.liewmanchoi.client;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.liewmanchoi.api.MessageProcessor;
import com.liewmanchoi.codec.MessageDecoder;
import com.liewmanchoi.codec.MessageEncoder;
import com.liewmanchoi.constant.FrameConstant;
import com.liewmanchoi.constant.NetConstant;
import com.liewmanchoi.domain.response.HttpResponse;
import com.liewmanchoi.domain.response.HttpResponse.CODE;
import com.liewmanchoi.domain.user.DeviceInfo;
import com.liewmanchoi.exception.ClientException;
import com.liewmanchoi.exception.ClientException.ErrorEnum;
import com.liewmanchoi.handler.HeartbeatHandler;
import com.liewmanchoi.handler.MessageConsumeHandler;
import com.liewmanchoi.serialize.ProtostuffSerializer;
import com.liewmanchoi.util.SDKUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author wangsheng
 * @date 2019/7/8
 */
@Slf4j
public class Client {

  private EventLoopGroup workerGroup;
  /**
   * 多线程环境下必须保证可见性
   */
  @Getter
  private volatile Channel futureChannel;

  private volatile Bootstrap bootstrap;
  @Getter
  private DeviceInfo deviceInfo = new DeviceInfo();
  /**
   * 鉴权服务器所在地址
   */
  @Getter
  private String url;

  @Getter
  private MessageProcessor messageProcessor;

  private ProtostuffSerializer serializer;
  /**
   * 最近消息ID缓存，保证消息消费的幂等性
   */
  private Cache<Integer, Object> cache =
      CacheBuilder.newBuilder()
          .initialCapacity(250)
          .maximumSize(250)
          .expireAfterAccess(12, TimeUnit.HOURS)
          .build();

  public void run(String url, MessageProcessor messageProcessor) {
    init(url, messageProcessor);
    // 鉴权并发起TCP长连接
    authenticateAndConnect();
  }

  private void init(String url, MessageProcessor messageProcessor) {
    String deviceId = SDKUtil.getDeviceId();
    deviceInfo.setDeviceId(deviceId);
    if (deviceId == null) {
      log.error("获取deviceId失败");
      throw new ClientException(ErrorEnum.DEVICE_ID_FAILURE, "获取deviceId失败");
    }

    this.url = url;
    this.messageProcessor = messageProcessor;
    boolean isEpoll = Epoll.isAvailable();
    log.info("是否支持Epoll调用[{}]", isEpoll);
    serializer = new ProtostuffSerializer();

    workerGroup = isEpoll ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
    bootstrap = new Bootstrap();
    bootstrap
        .group(workerGroup)
        .channel(isEpoll ? EpollSocketChannel.class : NioSocketChannel.class)
        .option(ChannelOption.TCP_NODELAY, true)
        .option(ChannelOption.SO_KEEPALIVE, true)
        .handler(initPipeline());
  }

  public void authenticateAndConnect() {
    // 向/app/keyToken发送http请求，返回得到鉴权密钥clientToken和可供连接的服务器地址

    // 构建http客户端
    OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
    // 构造http请求体
    FormBody formBody = new FormBody.Builder().add("deviceId", deviceInfo.getDeviceId()).build();
    // 构建http post请求
    Request request = new Request.Builder().url(url + "/app/keyToken").post(formBody).build();

    log.info("向地址[{}]发送post鉴权请求", url + "/app/keyToken");
    // 发送请求
    Call call = okHttpClient.newCall(request);
    call.enqueue(
        new Callback() {
          @Override
          public void onFailure(Call call, IOException e) {
            log.error("向[{}]发送HTTP请求失败", request.url(), e);
            log.warn("开始向[{}]重新发送HTTP请求...", request.url());
            call.clone().enqueue(this);
            call.cancel();
          }

          @Override
          public void onResponse(Call call, Response response) throws IOException {
            final int SUCCESS = 200;

            if (response.code() != SUCCESS) {
              log.error("HTTP请求发生错误，错误码为[{}]", response.code());
              return;
            }

            if (response.body() == null) {
              log.error("HTTP请求响应体为空");
              return;
            }
            // 获取字符串形式的请求响应体
            String body = response.body().toString();
            // 解析成json对象
            HttpResponse httpResponse = JSON.parseObject(body, HttpResponse.class);
            /* JSON格式：
            { code: 0;
            data: {
              entity: {
                deviceId:
                deviceToken:
                serverAddress:
                }
              }
            }
             */
            if (httpResponse.getCode() == CODE.FAILURE) {
              log.error("请求执行失败，失败原因[{}]", httpResponse.getData().get("message"));
              return;
            }

            DeviceInfo deviceInfo = (DeviceInfo) httpResponse.getData().get("entity");
            // 获取deviceToken
            String deviceToken = deviceInfo.getDeviceToken();
            Client.this.deviceInfo.setDeviceToken(deviceToken);

            // 获取push server地址
            InetSocketAddress serverAddress = deviceInfo.getServerAddress();
            Client.this.deviceInfo.setServerAddress(serverAddress);

            // 获得推送服务器地址后，发起连接
            doConnect();
          }
        });
  }

  private ChannelInitializer initPipeline() {
    return new ChannelInitializer() {
      @Override
      protected void initChannel(Channel ch) throws Exception {
        ch.pipeline()
            .addLast("IdleStateHandler", new IdleStateHandler(NetConstant.HEARTBEAT_TIMEOUT, 0, 0))
            .addLast(
                "LengthFieldPrepender",
                new LengthFieldPrepender(
                    FrameConstant.LENGTH_FIELD_LENGTH, FrameConstant.LENGTH_ADJUSTMENT))
            .addLast("MessageEncoder", new MessageEncoder(serializer))
            // 入站解码器
            .addLast(
                "LengthFieldBasedFrameDecoder",
                new LengthFieldBasedFrameDecoder(
                    FrameConstant.MAX_FRAME_LENGTH,
                    FrameConstant.LENGTH_FIELD_OFFSET,
                    FrameConstant.LENGTH_FIELD_LENGTH,
                    FrameConstant.LENGTH_ADJUSTMENT,
                    FrameConstant.INITIAL_BYTES_TO_STRIP))
            .addLast("MessageDecoder", new MessageDecoder(serializer))
            // 成功建立连接后，ConnectionListener将会在此处添加AuthHandler用于鉴权认证，认证成功后，会自动删除
            // 处理心跳监控
            .addLast("HeartbeatHandler", new HeartbeatHandler(Client.this))
            .addLast(
                "MessageConsumerHandler", new MessageConsumeHandler(Client.this));
      }
    };
  }

  public void doConnect() {
    // 开始连接之前，关闭可能存在的旧的连接，以释放资源
    if (futureChannel != null && futureChannel.isOpen()) {
      log.warn("存在着未关闭的旧连接，开始关闭...");
      futureChannel.close().syncUninterruptibly();
    }

    log.info("开始连接服务器[{}]", deviceInfo.getServerAddress());
    ChannelFuture channelFuture = bootstrap.connect(deviceInfo.getServerAddress());
    // 设置channel
    this.futureChannel = channelFuture.channel();
    // 添加listener，如果失败，则发起重连
    channelFuture.addListener(new ConnectionListener(this));
  }

  public void close() {
    if (futureChannel != null && futureChannel.isOpen()) {
      log.info("正在关闭客户端SDK...");
      try {
        futureChannel.close().syncUninterruptibly();
      } finally {
        if (workerGroup != null
            && !workerGroup.isTerminated()
            && !workerGroup.isShutdown()
            && !workerGroup.isShuttingDown()) {
          workerGroup.shutdownGracefully();
        }
      }
    }
    log.info("成功关闭客户端SDK");
  }

  /**
   * 缓存消息ID
   *
   * @param messageId 消息ID
   */
  public void putInCache(int messageId) {
    cache.put(messageId, new Object());
  }

  /**
   * 判断消息是否曾被消费
   *
   * @param messageId 消息ID
   * @return boolean
   */
  public boolean hasConsumed(int messageId) {
    return cache.getIfPresent(messageId) != null;
  }
}
