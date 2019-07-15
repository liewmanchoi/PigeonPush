package com.liewmanchoi.service.impl;

import com.liewmanchoi.dao.RedisDAO;
import com.liewmanchoi.service.AppAuthService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wangsheng
 * @date 2019/7/14
 */
@Service
public class AppAuthServiceImpl implements AppAuthService {

  @Autowired
  private RedisDAO redisDAO;

  @Override
  public String getAndPersistKeyToken(String deviceId) {
    String token = UUID.randomUUID().toString();
    redisDAO.putToken(deviceId, token);

    return token;
  }
}
