package com.liewmanchoi.server;

import com.liewmanchoi.PullService;
import com.liewmanchoi.codec.MessageDecoder;
import com.liewmanchoi.codec.MessageEncoder;
import com.liewmanchoi.config.ServerConfig;
import com.liewmanchoi.constant.FrameConstant;
import com.liewmanchoi.constant.NetConstant;
import com.liewmanchoi.domain.message.Message;
import com.liewmanchoi.handler.AuthHandler;
import com.liewmanchoi.handler.DisconnectHandler;
import com.liewmanchoi.handler.MessageHandler;
import com.liewmanchoi.serialize.ProtostuffSerializer;
import com.liewmanchoi.service.ACKService;
import com.liewmanchoi.service.AuthService;
import com.liewmanchoi.service.RegisterService;
import com.liewmanchoi.service.RouteService;
import com.liewmanchoi.util.IpUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 推送服务器
 *
 * @author wangsheng
 * @date 2019/7/20
 */
@Slf4j
@Component
public class Server {
  @Getter private final AttributeKey<String> attributeKey = AttributeKey.valueOf("clientID");
  @Autowired private ServerConfig serverConfig;
  @Autowired private RegisterService registerService;
  @Autowired private RouteService routeService;
  @Autowired private AuthService authService;
  @Autowired private ACKService ackService;
  /** 远程调用PullService(delivery模块) */
  @org.apache.dubbo.config.annotation.Reference(
      version = "1.0.0",
      interfaceClass = PullService.class)
  private PullService pullService;

  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;
  private ChannelFuture future;
  /** 通道关系表 */
  private Map<String, SocketChannel> channelMap;
  /** 编解码器 */
  private ProtostuffSerializer serializer;
  /** 本机ip地址 */
  private String ipAddress;

  @PostConstruct
  public void start() {
    boolean supportEpoll = Epoll.isAvailable();
    bossGroup = supportEpoll ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
    workerGroup =
        supportEpoll
            ? new EpollEventLoopGroup(serverConfig.getIoThreads())
            : new NioEventLoopGroup(serverConfig.getIoThreads());
    channelMap = new ConcurrentHashMap<>(10000);
    serializer = new ProtostuffSerializer();
    ipAddress = getIpAddress();

    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap
        .group(bossGroup, workerGroup)
        .channel(supportEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        .option(ChannelOption.SO_BACKLOG, 128)
        .option(ChannelOption.SO_REUSEADDR, true)
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        .childOption(ChannelOption.SO_SNDBUF, 32 * 1024)
        .childHandler(initPipeline());

    // 绑定端口
    future = bootstrap.bind(serverConfig.getPort()).syncUninterruptibly();
    log.info("服务器绑定端口[{}]", serverConfig.getPort());

    // 向Zookeeper集群注册服务器
    registerService.register(ipAddress, future.channel().eventLoop());
  }

  private ChannelInitializer<SocketChannel> initPipeline() {
    return new ChannelInitializer<SocketChannel>() {
      @Override
      protected void initChannel(SocketChannel ch) {
        ch.pipeline()
            .addLast(
                "IdleStateHandler",
                new IdleStateHandler(
                    NetConstant.HEARTBEAT_TIMEOUT * NetConstant.HEARTBEAT_TIMEOUT_MAX_TIMES, 0, 0))
            // 出站拦截器
            .addLast(
                "LengthFieldPrepender",
                new LengthFieldPrepender(
                    FrameConstant.LENGTH_FIELD_LENGTH, FrameConstant.LENGTH_ADJUSTMENT))
            .addLast("MessageEncoder", new MessageEncoder(serializer))
            // 入站拦截器
            .addLast(
                "LengthFieldBasedFrameDecoder",
                new LengthFieldBasedFrameDecoder(
                    FrameConstant.MAX_FRAME_LENGTH,
                    FrameConstant.LENGTH_FIELD_OFFSET,
                    FrameConstant.LENGTH_FIELD_LENGTH,
                    FrameConstant.LENGTH_ADJUSTMENT,
                    FrameConstant.INITIAL_BYTES_TO_STRIP))
            .addLast("MessageDecoder", new MessageDecoder(serializer))
            // 鉴权拦截器
            .addLast("AuthHandler", new AuthHandler(Server.this))
            // 连接断开/异常处理
            .addLast("DisconnectHandler", new DisconnectHandler(Server.this))
            // 处理PING/ACK
            .addLast("MessageHandler", new MessageHandler(Server.this));
      }
    };
  }

  public boolean checkToken(String clientID, String token) {
    return authService.checkToken(clientID, token);
  }

  private String getIpAddress() {
    String address;
    if (serverConfig.getAddress() != null) {
      address = serverConfig.getAddress();
    } else {
      address = IpUtil.getLocalAddress().getHostAddress();
    }

    return address;
  }

  /** 在收到SDK发送的PING消息时，远程调用delivery模块的pullMessage方法，拉取未确认消息 */
  public void acceptPullRequest(String clientID) {
    log.info(">>>   开启远程调用PullService#pullMessage，拉取客户端[{}]的待确认消息   <<<", clientID);
    pullService.pullMessage(clientID);
  }

  public void handleACK(String clientID, Long messageID) {
    if (clientID == null || messageID == null) {
      return;
    }

    log.info(">>>   处理消息确认ACK请求   <<<");
    ackService.handleACK(clientID, messageID);
  }

  public void afterEstablishConnection(String clientID, SocketChannel channel) {
    // 连接建立后，需要完成下列步骤
    // 1. 增加通道关系
    channelMap.put(clientID, channel);
    // 2. 增加路由关系
    routeService.addRoute(clientID);

    log.info(">>>   与[{}]连接建立后的附加工作完成   <<<", clientID);
  }

  /** 关闭clientID对应的连接并清理相应表项 */
  public void closeConnection(String clientID) {
    // 关闭对应的长连接，需要完成以下步骤
    // 1. 删除token表的密钥
    authService.removeToken(clientID);
    // 2. 删除路由表表项
    routeService.removeRoute(clientID);
    // 3. 删除通道关系表表项
    channelMap.remove(clientID);
    // 4. 关闭channel
    SocketChannel channel = channelMap.get(clientID);
    if (channel != null && channel.isOpen()) {
      channel
          .close()
          .addListener(
              (ChannelFutureListener) future -> log.info(">>>   关闭与[{}]的连接   <<<", clientID));
    }

    log.info(">>>   连接关闭后的清理工作完成   <<<");
  }

  public void push(Message message) {
    if (message.getType() != Message.PUSH) {
      // 如果推送的消息类型不是PUSH，直接返回
      log.warn(">>>   推送消息类型有误   <<<");

      // 除非正常发送（序列化编码时Message对象会被回收），否则都需要另行回收
      message.recycle();
      return;
    }

    final String clientID = message.getClientId();
    final SocketChannel channel = channelMap.get(clientID);
    if (channel == null || !channel.isActive()) {
      // 如果连接状态错误，关闭连接
      log.warn(">>>   与客户端[{}]的连接错误   <<<", clientID);
      closeConnection(clientID);
      // 回收Message对象
      message.recycle();
      return;
    }

    // 发送消息
    channel.writeAndFlush(message);
    log.info(">>>   向客户端[{}]推送消息[{}]   <<<", clientID, message.getPushMessageID());
  }

  @PreDestroy
  public void close() {
    log.warn(">>>   开始关闭服务器   <<<");
    if (bossGroup != null) {
      bossGroup.shutdownGracefully();
    }

    if (workerGroup != null) {
      workerGroup.shutdownGracefully();
    }

    if (future != null) {
      future
          .channel()
          .close()
          .addListener((ChannelFutureListener) future -> log.info(">>>   推送服务器关闭   <<<"));
    }
  }
}
