package com.liewmanchoi.config;

import com.liewmanchoi.domain.message.PushMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author wangsheng
 * @date 2019/7/14
 */
@Configuration
public class RedisConfig {
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
    jedisPoolConfig.setMaxTotal(5);
    jedisPoolConfig.setTestOnBorrow(true);
    jedisPoolConfig.setTestOnReturn(true);

    JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
    connectionFactory.setPoolConfig(jedisPoolConfig);
    connectionFactory.setUsePool(true);
    connectionFactory.setHostName("192.168.29.132");
    connectionFactory.setPort(6379);

    return connectionFactory;
  }

  @Bean(name = "stringTemplate")
  public RedisTemplate<String, String> stringTemplate() {
    RedisConnectionFactory factory = redisConnectionFactory();
    RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(factory);

    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new StringRedisSerializer());
    redisTemplate.afterPropertiesSet();

    return redisTemplate;
  }

  @Bean(name = "pushMessageTemplate")
  public RedisTemplate<String, PushMessage> pushMessageTemplate() {
    RedisConnectionFactory factory = redisConnectionFactory();
    RedisTemplate<String, PushMessage> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(factory);

    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(PushMessage.class));
    redisTemplate.afterPropertiesSet();

    return redisTemplate;
  }

  @Bean(name = "setTemplate")
  public RedisTemplate<String, Long> setTemplate() {
    RedisConnectionFactory factory = redisConnectionFactory();
    RedisTemplate<String, Long> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(factory);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Long.class));
    return redisTemplate;
  }

  @Bean(name = "stringValueOperations")
  public ValueOperations<String, String> stringValueOperations() {
    RedisTemplate<String, String> redisTemplate = stringTemplate();
    return redisTemplate.opsForValue();
  }

  @Bean(name = "pushMessageValueOperations")
  public ValueOperations<String, PushMessage> pushMessageValueOperations() {
    RedisTemplate<String, PushMessage> redisTemplate = pushMessageTemplate();
    return redisTemplate.opsForValue();
  }

  @Bean(name = "setOperations")
  public SetOperations<String, Long> longSetOperations() {
    RedisTemplate<String, Long> redisTemplate = setTemplate();
    return redisTemplate.opsForSet();
  }
}
