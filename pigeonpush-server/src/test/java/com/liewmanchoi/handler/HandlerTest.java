package com.liewmanchoi.handler;

import com.liewmanchoi.codec.MessageDecoder;
import com.liewmanchoi.codec.MessageEncoder;
import com.liewmanchoi.constant.FrameConstant;
import com.liewmanchoi.domain.message.Message;
import com.liewmanchoi.domain.message.PushMessage;
import com.liewmanchoi.serialize.ProtostuffSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author wangsheng
 * @date 2019/7/22
 */
public class HandlerTest {
  private EmbeddedChannel channel = new EmbeddedChannel();
  private ProtostuffSerializer serializer = new ProtostuffSerializer();

  {
    channel
        .pipeline()
        .addLast(
            new LengthFieldPrepender(
                FrameConstant.LENGTH_FIELD_LENGTH, FrameConstant.LENGTH_ADJUSTMENT))
        .addLast(new MessageEncoder(serializer))
        .addLast(
            new LengthFieldBasedFrameDecoder(
                FrameConstant.MAX_FRAME_LENGTH,
                FrameConstant.LENGTH_FIELD_OFFSET,
                FrameConstant.LENGTH_FIELD_LENGTH,
                FrameConstant.LENGTH_ADJUSTMENT,
                FrameConstant.INITIAL_BYTES_TO_STRIP))
        .addLast(new MessageDecoder(serializer));
  }

  @Test
  public void test1() {
    // 构建消息
    String clientID = UUID.randomUUID().toString();
    PushMessage pushMessage = new PushMessage();
    pushMessage.setClientId(clientID);
    pushMessage.setMessageId(1);
    pushMessage.setTitle("测试");
    pushMessage.setText("测试必定会成功");

    Message push1 = Message.buildPush(pushMessage);
    pushMessage.setText("老天保佑测试成功");

    Message push2 = Message.buildACK("帅气的我", 8);
    System.out.println(push1);
    System.out.println(push2);
    // 发送再接收

    Assert.assertTrue(channel.writeOutbound(push1, push2));
    channel.finish();

    ByteBuf object = channel.readOutbound();
    System.out.println(object.readInt());
    System.out.println(object.toString());
    System.out.println(((ByteBuf) channel.readOutbound()).readableBytes());

    object = channel.readOutbound();
    System.out.println(object.readInt());
    System.out.println(object.toString());
    System.out.println(((ByteBuf) channel.readOutbound()).readableBytes());
    //    channel.writeInbound(object);
    //
    //    Message result = channel.readInbound();
    //    System.out.println(result);
  }

  @Test
  public void protostuffTest() {
    // 构建消息
    String clientID = UUID.randomUUID().toString();
    PushMessage pushMessage = new PushMessage();
    pushMessage.setClientId(clientID);
    pushMessage.setMessageId(1);
    pushMessage.setTitle("测试");
    pushMessage.setText("测试必定会成功");

    Message push = Message.buildPush(pushMessage);

    byte[] bytes = serializer.serialize(push);

    Message result = serializer.deserialize(bytes, Message.class);
    System.out.println("original: >>>" + push);
    System.out.println("result: >>>" + result);

    push.recycle();
    result.recycle();
  }
}
