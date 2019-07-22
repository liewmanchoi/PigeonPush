package com.liewmanchoi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangsheng
 * @date 2019/7/21
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "server")
public class ServerConfig {
  int port;
  int ioThreads;
}
