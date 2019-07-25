package com.liewmanchoi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangsheng
 * @date 2019/7/23
 */
@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "cluster")
public class IDConfig {
  long dataCenterId;
  long workerId;
}
