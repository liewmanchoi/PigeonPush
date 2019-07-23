package com.liewmanchoi.service.impl;

import com.liewmanchoi.config.IDConfig;
import com.liewmanchoi.service.api.IDService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {IDConfig.class, IDServiceImpl.class})
public class IDServiceImplTest {
  @Autowired
  private IDConfig idConfig;

  @Autowired
  private IDService service;

  @Test
  public void generateID() throws InterruptedException {
    Map<Long, Object> map = new ConcurrentHashMap<>();

    int num = 100000;
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    CountDownLatch latch = new CountDownLatch(num);

    for (int i = 0; i < num; i++) {
      executorService.submit(() -> {

        long id = service.generateID();
        map.put(id, new Object());
        latch.countDown();
      });
    }

    latch.await();
    Assert.assertEquals(map.size(), num);
  }

  @Test
  public void simpleTest() {
    for (int i = 0; i < 10; i++) {
      System.out.println(service.generateID());
    }
  }
}