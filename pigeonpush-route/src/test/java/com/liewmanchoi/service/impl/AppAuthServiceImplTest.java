package com.liewmanchoi.service.impl;

import com.liewmanchoi.config.RedisConfig;
import com.liewmanchoi.dao.RedisDAO;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AppAuthServiceImpl.class, RedisDAO.class, RedisConfig.class})
public class AppAuthServiceImplTest {

  @Autowired
  AppAuthServiceImpl appAuthService;

  @Test
  public void getAndPersistKeyToken() {
    String deviceId = UUID.randomUUID().toString();
    appAuthService.getAndPersistKeyToken(deviceId);
  }
}
