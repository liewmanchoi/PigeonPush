package com.liewmanchoi.service;

import com.liewmanchoi.config.ZookeeperConfig;
import java.io.File;
import org.apache.curator.test.TestingServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ZookeeperConfig.class, DiscoveryService.class})
public class DiscoveryServiceTest {

  @Autowired
  private DiscoveryService discoveryService;
  @Autowired
  private ZookeeperConfig zookeeperConfig;

  @Test
  public void init() throws Exception {
    TestingServer server = new TestingServer(2181, new File("/PigeonPush/pigeon/push-server"));
    zookeeperConfig.setConnectString(server.getConnectString());

    discoveryService.init();
    while (true) {
    }
  }

  @Test
  public void getAllServers() {
  }
}
