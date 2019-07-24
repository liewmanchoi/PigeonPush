package com.liewmanchoi.controller;

import com.alibaba.fastjson.JSON;
import com.liewmanchoi.domain.message.PushMessage;
import com.liewmanchoi.domain.response.WebResponse;
import com.liewmanchoi.domain.response.WebResponse.CODE;
import com.liewmanchoi.entity.RequestEntity;
import com.liewmanchoi.service.api.MessageConverter;
import com.liewmanchoi.service.impl.ProducerService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangsheng
 * @date 2019/7/23
 */
@Slf4j
@RestController
@RequestMapping(path = "/v1")
public class PushController {

  @Autowired private MessageConverter messageConverter;
  @Autowired private ProducerService producerService;

  @PostMapping(
      value = "/push_message",
      produces = "application/json",
      consumes = "application/json")
  public String push(@RequestBody RequestEntity entity) {
    // 构造响应体
    WebResponse response = new WebResponse();

    if (entity == null || !entity.isValid()) {
      response.setCode(CODE.FAILURE);
      response.getData().put("message", "提交的json字符串有误");

      log.warn(">>>   用户提交的数据有误   <<<");
      return JSON.toJSONString(response);
    }

    // 如果消息合格，转换为PushMessage对象
    List<PushMessage> messageList = messageConverter.convert(entity);

    log.info(">>>   向消息队列发送数据   <<<");
    producerService.push(messageList);

    response.setCode(CODE.OK);
    response.getData().put("message", "成功提交推送请求");
    return JSON.toJSONString(response);
  }
}
