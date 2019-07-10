package com.liewmanchoi.util;

import com.liewmanchoi.domain.message.Message;
import io.netty.util.Recycler;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wangsheng
 * @date 2019/7/9
 */
public class GlobalRecycler {
  private static Map<Class<?>, Recycler<?>> CACHE_MAP = new HashMap<>();

  static {
    CACHE_MAP.put(
        Message.class,
        new Recycler<Message>() {
          @Override
          protected Message newObject(Handle<Message> handle) {
            return new Message(handle);
          }
        });
  }

  public static boolean recyclable(Class<?> clazz) {
    return CACHE_MAP.containsKey(clazz);
  }

  @SuppressWarnings("unchecked")
  public static <T> T reuse(Class<?> clazz) {
    return (T) CACHE_MAP.get(clazz).get();
  }
}
