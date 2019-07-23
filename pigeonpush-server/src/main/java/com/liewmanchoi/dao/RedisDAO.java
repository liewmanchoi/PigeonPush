package com.liewmanchoi.dao;

import com.liewmanchoi.constant.RedisPrefix;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

/**
 * @author wangsheng
 * @date 2019/7/20
 */
@Slf4j
@Repository
public class RedisDAO {

  @Autowired private RedisTemplate<String, Object> redisTemplate;
  @Autowired private ValueOperations<String, Object> valueOperations;
  @Autowired private HashOperations<String, String, String> hashOperations;
  @Autowired private SetOperations<String, Object> setOperations;

  /** 查询token表 */
  public String getToken(String clientID) {
    String key = generateKey(RedisPrefix.AUTH_PRE, clientID);
    return (String) valueOperations.get(key);
  }

  /** 删除token表项 */
  public void removeToken(String clientID) {
    String key = generateKey(RedisPrefix.AUTH_PRE, clientID);
    redisTemplate.delete(key);
  }

  /** 增加路由表项 */
  public void addRoute(String clientID, String socketAddress) {

    String key = generateKey(RedisPrefix.ROUTE_PRE, clientID);
    valueOperations.set(key, socketAddress);
  }

  /** 删除路由表项 */
  public void removeRoute(String clientID) {
    String key = generateKey(RedisPrefix.ROUTE_PRE, clientID);
    redisTemplate.delete(key);
  }

  /** 查询消息体 */
  public Map<String, String> getMessageBody(int messageID) {
    String key = generateKey(RedisPrefix.MSG_PRE, messageID);
    return hashOperations.entries(key);
  }

  /** 删除待确认消息表项 */
  public void removeWaitACK(String clientID, int messageID) {
    String key = generateKey(RedisPrefix.ACK_PRE, clientID);
    setOperations.remove(key, messageID);
  }

  private String generateKey(String funcID, Object id) {
    return RedisPrefix.getRedisKey(funcID, id);
  }
}
