package com.liewmanchoi.config;

import com.liewmanchoi.domain.message.PushMessage;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

/**
 * @author wangsheng
 * @date 2019/7/24
 */
@EnableKafka
@Configuration
public class KafkaConfig {
  @Value("${spring.kafka.consumer.bootstrap-servers}")
  private String serverAddresses;

  @Bean
  public Map<String, Object> consumerConfig() {
    Map<String, Object> config = new HashMap<>(3);
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddresses);
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, "PigeonConsumer");

    return config;
  }

  @Bean
  public ConsumerFactory<String, PushMessage> consumerFactory() {
    return new DefaultKafkaConsumerFactory<>(consumerConfig());
  }

  @Bean
  public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, PushMessage>>
      kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, PushMessage> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    // 允许批量消费消息
    factory.setBatchListener(true);
    return factory;
  }
}
