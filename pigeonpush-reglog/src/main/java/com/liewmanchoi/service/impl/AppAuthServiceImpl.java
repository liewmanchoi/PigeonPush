package com.liewmanchoi.service.impl;

import com.liewmanchoi.dao.RedisDAO;
import com.liewmanchoi.service.AppAuthService;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wangsheng
 * @date 2019/7/14
 */
@Slf4j
@Service
public class AppAuthServiceImpl implements AppAuthService {

  @Autowired
  private RedisDAO redisDAO;

  @Override
  public String getAndPersistKeyToken(String clientId) {
    String token = UUID.randomUUID().toString();
    redisDAO.putToken(clientId, token);

    log.info(">>>   向Redis存储[{}]对应的密钥   <<<", clientId);
    return token;
  }
}
