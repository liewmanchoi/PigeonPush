package com.liewmanchoi.service.impl;

import com.liewmanchoi.service.LoadBalancer;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ConsistentHashService.class})
public class ConsistentHashServiceTest {

  @Autowired
  LoadBalancer loadBalancer;

  @Test
  public void selectPushServer() {
    List<InetSocketAddress> list = null;
    String key = UUID.randomUUID().toString();
    TestCase.assertNull(loadBalancer.selectPushServer(list, key));

    list = new ArrayList<>();
    TestCase.assertNull(loadBalancer.selectPushServer(list, key));
    InetSocketAddress address = new InetSocketAddress("192.168.0.101", 3456);
    list.add(address);
    TestCase.assertEquals(address, loadBalancer.selectPushServer(list, key));

    String sad = "193.198.0.";
    for (int i = 0; i < 100; ++i) {
      InetSocketAddress address1 = new InetSocketAddress(sad + i, i);
      list.add(address1);
    }

    System.out.println(loadBalancer.selectPushServer(list, key));
  }
}
