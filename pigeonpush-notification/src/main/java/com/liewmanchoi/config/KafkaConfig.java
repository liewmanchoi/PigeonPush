package com.liewmanchoi.config;

import com.liewmanchoi.domain.message.PushMessage;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * @author wangsheng
 * @date 2019/7/24
 */
@Configuration
public class KafkaConfig {
  @Value("${spring.kafka.producer.bootstrap-servers}")
  private String serverAddresses;

  @Bean
  public KafkaAdmin kafkaAdmin() {
    Map<String, Object> configs = new HashMap<>(1);
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddresses);
    return new KafkaAdmin(configs);
  }

  @Bean
  public NewTopic topic() {
    return new NewTopic("PigeonPush", 3, (short) 3);
  }

  @Bean
  public Map<String, Object> producerConfig() {
    Map<String, Object> config = new HashMap<>(3);
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddresses);
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

    return config;
  }

  @Bean
  public ProducerFactory<String, PushMessage> producerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfig());
  }

  @Bean
  public KafkaTemplate<String, PushMessage> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }
}
