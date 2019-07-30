package com.liewmanchoi;

import com.liewmanchoi.config.ZookeeperConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author wangsheng
 * @date 2019/7/15
 */
@SpringBootApplication
@EnableConfigurationProperties(ZookeeperConfig.class)
public class RoutApp {

  public static void main(String[] args) {
    SpringApplication.run(RoutApp.class);
  }
}
