package com.liewmanchoi;

import com.liewmanchoi.config.ServerConfig;
import com.liewmanchoi.config.ZookeeperConfig;
import org.apache.dubbo.config.spring.context.annotation.EnableDubboConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author wangsheng
 * @date 2019/7/20
 */
@SpringBootApplication
@EnableDubboConfig
@EnableConfigurationProperties({ZookeeperConfig.class, ServerConfig.class})
public class PushServerApp {
  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(PushServerApp.class);
    application.setWebApplicationType(WebApplicationType.NONE);
    application.run(args);
  }
}
