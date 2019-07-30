package com.liewmanchoi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangsheng
 * @date 2019/7/21
 */
@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "push-server")
public class ServerConfig {
  String address;
  int port;
  int ioThreads;
}
