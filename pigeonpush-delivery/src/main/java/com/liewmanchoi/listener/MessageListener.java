package com.liewmanchoi.listener;

import com.liewmanchoi.constant.KafkaConstant;
import com.liewmanchoi.domain.message.PushMessage;
import com.liewmanchoi.service.api.DeliveryService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * @author wangsheng
 * @date 2019/7/25
 */
@Slf4j
@Service
public class MessageListener {
  @Autowired private DeliveryService deliveryService;

  @KafkaListener(
      topics = {KafkaConstant.TOPIC},
      groupId = "PigeonConsumer")
  public void listen(@Payload List<PushMessage> messages) {
    // 下发从消息队列中获取的消息
    log.info(">>>   从消息队列中获取得到了[{}]条消息   <<<", messages.size());
    deliveryService.deliver(messages, false);
  }
}
