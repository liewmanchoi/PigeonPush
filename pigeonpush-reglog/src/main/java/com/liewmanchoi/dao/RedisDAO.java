package com.liewmanchoi.dao;

import com.liewmanchoi.constant.RedisPrefix;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author wangsheng
 * @date 2019/7/14
 */
@Slf4j
@Repository
public class RedisDAO {

  private final TimeUnit TOKEN_EXPIRED_TIME_UNIT = TimeUnit.HOURS;
  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  public void putToken(String clientId, String token) {
    String key = generateKey(clientId);

    int TOKEN_TIMEOUT = 1;
    redisTemplate.opsForValue().set(key, token, TOKEN_TIMEOUT, TOKEN_EXPIRED_TIME_UNIT);
  }

  public String getToken(String clientId) {
    String key = generateKey(clientId);
    return redisTemplate.opsForValue().get(key);
  }

  public void removeToken(String clientId) {
    String key = generateKey(clientId);
    redisTemplate.delete(key);
  }

  public boolean containsId(String clientId) {
    String key = generateKey(clientId);
    return redisTemplate.hasKey(key);
  }

  public boolean checkToken(String clientId, String token) {
    String key = generateKey(clientId);

    return token.equals(redisTemplate.opsForValue().get(key));
  }

  private String generateKey(String clientId) {
    return RedisPrefix.getRedisKey(RedisPrefix.AUTH_PRE, clientId);
  }
}
