package com.liewmanchoi.dao;

import com.liewmanchoi.constant.RedisPrefix;
import java.util.HashMap;
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
 * @date 2019/7/25
 */
@Slf4j
@Repository
public class RedisDAO {
  @Autowired private RedisTemplate<String, String> redisTemplate;
  @Autowired private ValueOperations<String, Object> valueOperations;
  @Autowired private HashOperations<String, String, String> hashOperations;
  @Autowired private SetOperations<String, Object> setOperations;

  private String generateKey(String funcID, Object id) {
    return RedisPrefix.getRedisKey(funcID, id);
  }

  /** 查询路由表项 */
  public String getSocketAddress(String clientID) {
    String key = generateKey(RedisPrefix.ROUTE_PRE, clientID);
    return (String) valueOperations.get(key);
  }

  /** 增加消息表项 */
  public void addMessage(Long messageID, String title, String text) {
    String key = generateKey(RedisPrefix.MSG_PRE, messageID);
    Map<String, String> map = new HashMap<>(2);
    map.put("title", title);
    map.put("test", text);
    hashOperations.putAll(key, map);
  }

  /** 增加待确认消息表项 */
  public void addWaitACK(String clientID, Long messageID) {
    String key = generateKey(RedisPrefix.ACK_PRE, clientID);
    setOperations.add(key, messageID);
  }
}
