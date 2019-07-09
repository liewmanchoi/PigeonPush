package com.liewmanchoi.serialize;

import com.liewmanchoi.domain.message.PMessage;
import com.liewmanchoi.util.PMessageRecycler;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于ProtoStuff的序列化工具
 *
 * @author wangsheng
 * @date 2019/7/9
 */
@Slf4j
public class ProtostuffSerializer {
  public byte[] serialize(PMessage pMessage) {
    Class<PMessage> clazz = PMessage.class;
    Schema<PMessage> schema = RuntimeSchema.getSchema(clazz);
    LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    try {
      return ProtostuffIOUtil.toByteArray(pMessage, schema, buffer);
    } finally{
      buffer.clear();
    }
  }

  public PMessage deserialize(byte[] bytes) {
    // 复用对象池中的对象，不需要重新创建
    PMessage pMessage = PMessageRecycler.reuse();
    Schema<PMessage> schema = RuntimeSchema.getSchema(PMessage.class);
    ProtostuffIOUtil.mergeFrom(bytes, pMessage, schema);

    return pMessage;
  }
}
