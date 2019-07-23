package com.liewmanchoi.service.impl;

import com.liewmanchoi.config.IDConfig;
import com.liewmanchoi.domain.message.PushMessage;
import com.liewmanchoi.entity.RequestEntity;
import com.liewmanchoi.service.api.IDService;
import com.liewmanchoi.service.api.MessageConverter;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {MessageConverterImpl.class, IDConfig.class, IDServiceImpl.class})
public class MessageConverterImplTest {
  @Autowired
  private IDConfig idConfig;
  @Autowired
  private IDService idService;

  @Autowired
  private MessageConverter converter;

  @Test
  public void convert() {
    RequestEntity entity = new RequestEntity();
    entity.setCid(Arrays.asList("a", "b", "c", "d"));
    entity.setText("我是内容");
    entity.setTitle("我是标题");

    List<PushMessage> messageList = converter.convert(entity);
    Assert.assertEquals(messageList.size(), 4);
    System.out.println(messageList);
  }
}
