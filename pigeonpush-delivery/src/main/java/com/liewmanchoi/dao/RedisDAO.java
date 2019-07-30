package com.liewmanchoi.dao;

import com.liewmanchoi.constant.RedisPrefix;
import com.liewmanchoi.domain.message.PushMessage;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

/**
 * @author wangsheng
 * @date 2019/7/25
 */
@Slf4j
@Repository
public class RedisDAO {
  @Autowired
  @Qualifier(value = "stringValueOperations")
  private ValueOperations<String, String> stringValueOperations;

  @Autowired
  @Qualifier(value = "pushMessageValueOperations")
  private ValueOperations<String, PushMessage> pushMessageValueOperations;

  @Autowired
  @Qualifier(value = "setOperations")
  private SetOperations<String, Long> longSetOperations;

  @Autowired
  @Qualifier(value = "setTemplate")
  private RedisTemplate<String, Long> longRedisTemplate;

  private String generateKey(String funcID, Object id) {
    return RedisPrefix.getRedisKey(funcID, id);
  }

  /** 查询路由表项 */
  public String getSocketAddress(String clientID) {
    String key = generateKey(RedisPrefix.ROUTE_PRE, clientID);
    return stringValueOperations.get(key);
  }

  /** 增加消息体表项 */
  public void addMessage(Long messageID, PushMessage pushMessage) {
    String key = generateKey(RedisPrefix.MSG_PRE, messageID);
    pushMessageValueOperations.set(key, pushMessage, 7L, TimeUnit.DAYS);
  }

  /** 增加待确认消息表项 */
  public void addWaitACK(String clientID, Long messageID) {
    String key = generateKey(RedisPrefix.ACK_PRE, clientID);
    // 设置过期时间
    longRedisTemplate.expire(key, 3,TimeUnit.DAYS);
    longSetOperations.add(key, messageID);
  }

  /** 获取clientID对应的所有待确认消息messageID列表 */
  public Set<Long> getWaitACKMessages(String clientID) {
    String key = generateKey(RedisPrefix.ACK_PRE, clientID);
    return longSetOperations.members(key);
  }
}
