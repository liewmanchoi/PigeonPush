package com.liewmanchoi.service.impl;

import com.liewmanchoi.domain.message.PushMessage;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * @author wangsheng
 * @date 2019/7/24
 */
@Service
public class ProducerService {
  @Value("PigeonPush")
  private String topic;

  @Autowired
  private KafkaTemplate<String, PushMessage> kafkaTemplate;

  public void push(List<PushMessage> messages) {
    if (messages == null || messages.isEmpty()) {
      return;
    }

    for (PushMessage message : messages) {
      kafkaTemplate.send(topic, message);
    }
  }
}
