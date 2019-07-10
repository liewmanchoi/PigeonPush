package com.liewmanchoi.codec;

import com.liewmanchoi.domain.message.Message;
import com.liewmanchoi.serialize.ProtostuffSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wangsheng
 * @date 2019/7/9
 */
@Slf4j
@AllArgsConstructor
public class MessageDecoder extends ByteToMessageDecoder {

  private ProtostuffSerializer serializer;

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    byte[] bytes = new byte[in.readableBytes()];
    in.readBytes(bytes);
    Message message = serializer.deserialize(bytes, Message.class);
    out.add(message);
  }
}
