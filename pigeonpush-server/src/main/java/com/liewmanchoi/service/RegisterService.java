package com.liewmanchoi.service;

import com.liewmanchoi.config.ServerConfig;
import com.liewmanchoi.config.ZookeeperConfig;
import com.liewmanchoi.zookeeper.ZookeeperRegistry;
import io.netty.channel.EventLoop;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wangsheng
 * @date 2019/7/21
 */
@Slf4j
@Service
public class RegisterService {
  private ZookeeperRegistry zookeeperRegistry;

  @Autowired private ZookeeperConfig zookeeperConfig;
  @Autowired private ServerConfig serverConfig;

  @PostConstruct
  public void init() {
    zookeeperRegistry = new ZookeeperRegistry();
    zookeeperRegistry.init(
        zookeeperConfig.getConnectString(),
        zookeeperConfig.getConnectionTimeoutMs(),
        zookeeperConfig.getSessionTimeoutMs(),
        zookeeperConfig.getBaseSleepTimeMs(),
        zookeeperConfig.getMaxRetries(),
        zookeeperConfig.getNamespace());
    log.info(">>>   Zookeeper客户端初始化完毕   <<<");
  }

  public void register(String ipAddress, EventLoop eventExecutor) {
    String socketAddress = ipAddress + ":" + serverConfig.getPort();
    log.info("推送服务器的socket地址为[{}]", socketAddress);

    boolean success = zookeeperRegistry.register(socketAddress);
    if (!success) {
      // 如果没能成功注册，则调度重新注册直至成功
      eventExecutor.schedule(() -> register(ipAddress, eventExecutor), 10, TimeUnit.SECONDS);
    }
  }
}
