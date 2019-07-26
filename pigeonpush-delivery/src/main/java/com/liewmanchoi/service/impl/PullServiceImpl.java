package com.liewmanchoi.service.impl;

import com.liewmanchoi.PullService;
import com.liewmanchoi.domain.message.PushMessage;
import com.liewmanchoi.service.api.DeliveryService;
import com.liewmanchoi.service.api.WaitACKService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消息拉取服务，获取clientID对应的所有待确认消息，然后分发
 *
 * @author wangsheng
 * @date 2019/7/26
 */
@Slf4j
@Service
@org.apache.dubbo.config.annotation.Service(
    version = "1.0.0",
    interfaceClass = PullService.class,
    timeout = 10000)
public class PullServiceImpl implements PullService {

  @Autowired private WaitACKService waitACKService;
  @Autowired private DeliveryService deliveryService;

  @Override
  public void pullMessage(String clientId) {
    if (clientId == null) {
      log.warn(">>>   输入的clientId参数为空   <<<");
      return;
    }

    // 1. 获取待确认的消息messageId列表
    Set<Long> messageIds = waitACKService.getMessageIds(clientId);
    if (messageIds == null || messageIds.isEmpty()) {
      log.info(">>>   [{}]对应的待确认消息列表为空   <<<", clientId);
      return;
    }

    // 2. 构造PushMessage列表
    List<PushMessage> pushMessages = new ArrayList<>(messageIds.size());
    for (Long messageId : messageIds) {
      PushMessage message = new PushMessage();
      message.setClientId(clientId);
      message.setMessageId(messageId);

      pushMessages.add(message);
    }

    // 3. 下发待确认消息列表
    deliveryService.deliver(pushMessages, true);
  }
}
