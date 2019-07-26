package com.liewmanchoi;

import org.apache.dubbo.config.spring.context.annotation.EnableDubboConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author wangsheng
 * @date 2019/7/26
 */
@SpringBootApplication
@EnableDubboConfig
public class DeliveryApp {
  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(DeliveryApp.class);
    application.setWebApplicationType(WebApplicationType.NONE);
    application.run(args);
  }
}
