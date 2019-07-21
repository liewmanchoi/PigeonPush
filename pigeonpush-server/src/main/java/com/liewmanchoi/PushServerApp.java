package com.liewmanchoi;

import com.liewmanchoi.config.ZookeeperConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author wangsheng
 * @date 2019/7/20
 */
@SpringBootApplication
@EnableConfigurationProperties(ZookeeperConfig.class)
public class PushServerApp {
  // TODO: server启动代码
}
