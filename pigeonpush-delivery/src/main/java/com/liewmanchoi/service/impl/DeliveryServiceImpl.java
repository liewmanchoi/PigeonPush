package com.liewmanchoi.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.liewmanchoi.PushService;
import com.liewmanchoi.domain.message.PushMessage;
import com.liewmanchoi.service.api.DeliveryService;
import com.liewmanchoi.service.api.MessagePersistService;
import com.liewmanchoi.service.api.RouteService;
import com.liewmanchoi.service.api.WaitACKService;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 核心服务：消息分发服务
 *
 * @author wangsheng
 * @date 2019/7/25
 */
@Slf4j
@Service
public class DeliveryServiceImpl implements DeliveryService {
  /** 缓存最近已持久化消息的messageID，避免反复读取Redis */
  private Cache<Long, Object> cache =
      CacheBuilder.newBuilder()
          .initialCapacity(250)
          .maximumSize(2500)
          .expireAfterAccess(12, TimeUnit.HOURS)
          .build();

  /** 引用PushService，指定cluster策略为自定义的directCluster */
  @org.apache.dubbo.config.annotation.Reference(
      version = "1.0.0",
      interfaceClass = PushService.class,
      cluster = "directCluster")
  private PushService pushService;

  @Autowired private RouteService routeService;
  @Autowired private MessagePersistService messagePersistService;
  @Autowired private WaitACKService waitACKService;

  @Override
  public void deliver(List<PushMessage> messages, boolean isPull) {
    if (messages == null || messages.isEmpty()) {
      return;
    }

    for (PushMessage message : messages) {
      if (!message.isValid()) {
        // 消息无效，忽略之
        continue;
      }

      Long messageId = message.getMessageId();
      String clientId = message.getClientId();

      // 1. 持久化消息体
      if (cache.getIfPresent(messageId) == null) {
        // 使用持久化服务持久化消息
        messagePersistService.persistMessage(message);
        // 更新缓存
        cache.put(messageId, new Object());
      }
      // 2. 如果不是从待确认消息表中拉取，则增加待确认消息表项
      if (!isPull) {
        waitACKService.addWaitACK(clientId, messageId);
      }
      // 3. 获取对应的推送服务的地址
      String ipAddress = routeService.getRoute(clientId);
      if (ipAddress == null) {
        // 如果无法找到对应的ip地址，说明此时客户端没有上线或者发生了其他故障，直接忽略
        log.info(">>>   客户端[{}]没有上线或者发生了其他故障   <<<", clientId);
        continue;
      }
      // 4. 将ip地址放入到RpcContext中，使得Dubbo能够直接调用该推送服务器
      RpcContext.getContext().set("ip", ipAddress);
      // 5. 发起远程调用
      pushService.pushMessage(message);
    }
  }
}
