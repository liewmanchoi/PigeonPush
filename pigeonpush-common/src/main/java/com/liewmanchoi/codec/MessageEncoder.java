package com.liewmanchoi.codec;

import com.liewmanchoi.domain.message.Message;
import com.liewmanchoi.serialize.ProtostuffSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 编码器：Message -> byte[] <br>
 * 注意事项：只用于Message类型的消息，则Message#type字段直接放入到字节流中，真正序列化的对象只有PMessage <br>
 * 好处：PING/PONG只需要发送1个字节，PMessage对象可以使用对象池复用回收 <br>
 *
 * @author wangsheng
 * @date 2019/7/9
 */
@Slf4j
@AllArgsConstructor
public class MessageEncoder extends MessageToByteEncoder {
  private ProtostuffSerializer serializer;

  @Override
  protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
    Message message = (Message) msg;
    out.writeBytes(serializer.serialize(message));
    log.info("序列化成功");
  }
}
