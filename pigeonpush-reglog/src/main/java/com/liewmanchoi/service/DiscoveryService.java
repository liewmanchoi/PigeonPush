package com.liewmanchoi.service;

import com.liewmanchoi.config.ZookeeperConfig;
import com.liewmanchoi.constant.ZookeeperConstant;
import com.liewmanchoi.zookeeper.ZookeeperRegistry;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wangsheng
 * @date 2019/7/14
 */
@Slf4j
@Service
public class DiscoveryService {

  private ZookeeperRegistry zookeeperRegistry;
  private Set<InetSocketAddress> cache;

  @Autowired
  private ZookeeperConfig zookeeperConfig;

  @PostConstruct
  public void init() {
    // 初始化缓存
    cache = ConcurrentHashMap.newKeySet();

    // zk客户端初始化
    zookeeperRegistry = new ZookeeperRegistry();
    zookeeperRegistry.init(
        zookeeperConfig.getConnectString(),
        zookeeperConfig.getConnectionTimeoutMs(),
        zookeeperConfig.getSessionTimeoutMs(),
        zookeeperConfig.getBaseSleepTimeMs(),
        zookeeperConfig.getMaxRetries(),
        zookeeperConfig.getNamespace());

    log.info(">>>   Zookeeper客户端初始化完毕   <<<");

    // 添加回调函数，修改缓存
    zookeeperRegistry.discover(
        ZookeeperConstant.REG_PATH,
        (CuratorFramework client, PathChildrenCacheEvent event) -> {
          switch (event.getType()) {
            case CHILD_ADDED:
              addServer(event);
              break;
            case CHILD_REMOVED:
              removeServer(event);
              break;
            default:
              break;
          }
        });
  }

  /**
   * 获取现有的服务器列表
   *
   * @return 推送服务器地址列表
   */
  public List<InetSocketAddress> getAllServers(boolean refresh) {
    if (refresh) {
      refreshCache();
    }

    return new ArrayList<>(cache);
  }

  private void refreshCache() {
    try {
      List<String> servers =
          zookeeperRegistry.getZkCli().getChildren().forPath(ZookeeperConstant.REG_PATH);
      // 清除原来的缓存列表
      cache.clear();
      servers.forEach(s -> cache.add(toSocketAddress(s)));
    } catch (Exception e) {
      log.error("获取[{}]的子节点的过程中发生异常", ZookeeperConstant.REG_PATH, e);
    }
  }

  private void addServer(PathChildrenCacheEvent event) {
    String path = event.getData().getPath();
    InetSocketAddress socketAddress = toSocketAddress(path);
    if (socketAddress != null) {
      cache.add(socketAddress);
      log.info("增加服务器: [{}]", socketAddress);
    }
  }

  private void removeServer(PathChildrenCacheEvent event) {
    String path = event.getData().getPath();
    InetSocketAddress socketAddress = toSocketAddress(path);
    if (socketAddress != null) {
      cache.remove(socketAddress);
      log.info("删除服务器: [{}]", socketAddress);
    }
  }

  /**
   * 将子节点事件event转换为InetSocketAddress
   *
   * @param path 节点路径
   * @return 服务器socket地址
   */
  private InetSocketAddress toSocketAddress(String path) {
    if (path == null || path.lastIndexOf('/') == -1) {
      log.warn("注册节点[{}]不是有效的socket地址", path);
      return null;
    }

    String address = path.substring(path.lastIndexOf('/') + 1);
    if (address.lastIndexOf(':') == -1) {
      log.warn("注册节点[{}]不是有效的socket地址", path);
      return null;
    }
    String host = address.substring(0, address.lastIndexOf(':'));
    int port = Integer.valueOf(address.substring(address.lastIndexOf(':') + 1));

    return new InetSocketAddress(host, port);
  }
}
