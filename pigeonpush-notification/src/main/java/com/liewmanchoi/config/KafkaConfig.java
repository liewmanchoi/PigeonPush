package com.liewmanchoi.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

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
    return new NewTopic("PigeonPush", 3, (short) 2);
  }
}
