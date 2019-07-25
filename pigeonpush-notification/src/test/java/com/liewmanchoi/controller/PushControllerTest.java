package com.liewmanchoi.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alibaba.fastjson.JSON;
import com.liewmanchoi.config.IDConfig;
import com.liewmanchoi.config.KafkaConfig;
import com.liewmanchoi.domain.response.WebResponse;
import com.liewmanchoi.domain.response.WebResponse.CODE;
import com.liewmanchoi.entity.RequestEntity;
import com.liewmanchoi.service.impl.IDServiceImpl;
import com.liewmanchoi.service.impl.MessageConverterImpl;
import com.liewmanchoi.service.impl.ProducerService;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@WebMvcTest(PushController.class)
@ContextConfiguration(classes = {MessageConverterImpl.class, IDServiceImpl.class, IDConfig.class, ProducerService.class,
    KafkaConfig.class})
public class PushControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ProducerService producerService;

  @Test
  public void push() throws Exception {
    MockitoAnnotations.initMocks(this);


    WebResponse response = new WebResponse();
    response.setCode(CODE.OK);
    response.getData().put("message", "成功提交推送请求");

    RequestEntity entity = new RequestEntity();
    entity.setTitle("标题");
    entity.setText("正文");
    entity.setCid(Arrays.asList("abc", "edf", "dgk"));

    mockMvc.perform(MockMvcRequestBuilders.post("/v1/push_message").
        contentType(MediaType.APPLICATION_JSON).content(JSON.toJSONString(entity))).andExpect(status().isOk()).
        andExpect(content().string(JSON.toJSONString(response)));
  }
}
