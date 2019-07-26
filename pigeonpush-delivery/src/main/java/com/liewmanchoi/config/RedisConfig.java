package com.liewmanchoi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author wangsheng
 * @date 2019/7/14
 */
@Configuration
public class RedisConfig {
  // TODO: 是否需要修改RedisTemplate的泛型参数和序列化方式？

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
    jedisPoolConfig.setMaxTotal(5);
    jedisPoolConfig.setTestOnBorrow(true);
    jedisPoolConfig.setTestOnReturn(true);

    JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
    connectionFactory.setPoolConfig(jedisPoolConfig);
    connectionFactory.setUsePool(true);
    connectionFactory.setHostName("192.168.29.131");
    connectionFactory.setPort(6379);

    return connectionFactory;
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(factory);

    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new StringRedisSerializer());
    redisTemplate.setHashKeySerializer(new StringRedisSerializer());
    redisTemplate.setHashValueSerializer(new StringRedisSerializer());
    redisTemplate.setEnableTransactionSupport(true);
    redisTemplate.afterPropertiesSet();

    return redisTemplate;
  }

  @Bean
  public ValueOperations<String, Object> valueOperations(
      RedisTemplate<String, Object> redisTemplate) {
    return redisTemplate.opsForValue();
  }

  @Bean
  public HashOperations<String, String, String> hashOperations(
      RedisTemplate<String, Object> redisTemplate) {
    return redisTemplate.opsForHash();
  }

  @Bean
  public SetOperations<String, Object> setOperations(RedisTemplate<String, Object> redisTemplate) {
    return redisTemplate.opsForSet();
  }
}
