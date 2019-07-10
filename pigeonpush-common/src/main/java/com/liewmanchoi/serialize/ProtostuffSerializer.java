package com.liewmanchoi.serialize;

import com.liewmanchoi.util.GlobalRecycler;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import lombok.extern.slf4j.Slf4j;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

/**
 * 基于ProtoStuff的序列化工具
 *
 * @author wangsheng
 * @date 2019/7/9
 */
@Slf4j
public class ProtostuffSerializer {
  @SuppressWarnings("unchecked")
  public <T> byte[] serialize(T object) {
    Class<T> clazz = (Class<T>) object.getClass();
    Schema<T> schema = RuntimeSchema.getSchema(clazz);
    LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    try {
      return ProtostuffIOUtil.toByteArray(object, schema, buffer);
    } finally {
      buffer.clear();
    }
  }

  public <T> T deserialize(byte[] bytes, Class<T> clazz) {
    T object;
    if (GlobalRecycler.recyclable(clazz)) {
      // 如果是PMessage对象，则复用对象池中的对象，不需要重新创建
      object = GlobalRecycler.reuse(clazz);
    } else {
      object = ObjenesisHolder.objenesis.newInstance(clazz);
    }

    Schema<T> schema = RuntimeSchema.getSchema(clazz);
    ProtostuffIOUtil.mergeFrom(bytes, object, schema);

    return object;
  }

  private static class ObjenesisHolder {
    // 使用静态类使用懒加载
    private static Objenesis objenesis = new ObjenesisStd(true);
  }
}
