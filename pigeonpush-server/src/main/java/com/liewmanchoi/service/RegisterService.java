package com.liewmanchoi.service;

import com.liewmanchoi.config.ServerConfig;
import com.liewmanchoi.config.ZookeeperConfig;
import com.liewmanchoi.util.IpUtil;
import com.liewmanchoi.zookeeper.ZookeeperRegistry;
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

  public void register() {
    String address = IpUtil.getLocalAddress().getHostAddress();
    String socketAddress = address + ":" + serverConfig.getPort();
    log.info("推送服务器的socket地址为[{}]", socketAddress);

    zookeeperRegistry.register(socketAddress);
  }
}
