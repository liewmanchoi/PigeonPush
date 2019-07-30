package com.liewmanchoi.dao;

import com.liewmanchoi.constant.RedisPrefix;
import com.liewmanchoi.domain.message.PushMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
  @Autowired
  @Qualifier(value = "stringTemplate")
  private RedisTemplate<String, String> stringRedisTemplate;

  @Autowired
  @Qualifier(value = "stringValueOperations")
  private ValueOperations<String, String> stringValueOperations;

  @Autowired
  @Qualifier(value = "pushMessageValueOperations")
  private ValueOperations<String, PushMessage> pushMessageValueOperations;

  @Autowired
  @Qualifier(value = "setOperations")
  private SetOperations<String, Long> longSetOperations;

  /** 查询token表 */
  public String getToken(String clientID) {
    String key = generateKey(RedisPrefix.AUTH_PRE, clientID);
    return stringValueOperations.get(key);
  }

  /** 删除token表项 */
  public void removeToken(String clientID) {
    String key = generateKey(RedisPrefix.AUTH_PRE, clientID);
    stringRedisTemplate.delete(key);
  }

  /** 增加路由表项 */
  public void addRoute(String clientID, String socketAddress) {

    String key = generateKey(RedisPrefix.ROUTE_PRE, clientID);
    stringValueOperations.set(key, socketAddress);
  }

  /** 删除路由表项 */
  public void removeRoute(String clientID) {
    log.warn(">>>   删除[{}]对应的路由表项   <<<", clientID);
    String key = generateKey(RedisPrefix.ROUTE_PRE, clientID);
    stringRedisTemplate.delete(key);
  }

  /** 查询消息体 */
  public PushMessage getMessageBody(Long messageID) {
    String key = generateKey(RedisPrefix.MSG_PRE, messageID);
    return pushMessageValueOperations.get(key);
  }

  /** 删除待确认消息表项 */
  public void removeWaitACK(String clientID, Long messageID) {
    String key = generateKey(RedisPrefix.ACK_PRE, clientID);
    longSetOperations.remove(key, messageID);
  }

  private String generateKey(String funcID, Object id) {
    return RedisPrefix.getRedisKey(funcID, id);
  }
}
