package com.liewmanchoi.controller;

import com.alibaba.fastjson.JSON;
import com.liewmanchoi.domain.response.WebResponse;
import com.liewmanchoi.domain.response.WebResponse.CODE;
import com.liewmanchoi.service.AppAuthService;
import com.liewmanchoi.service.DiscoveryService;
import com.liewmanchoi.service.LoadBalancer;
import java.net.InetSocketAddress;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangsheng
 * @date 2019/7/11
 */
@RestController
@Slf4j
public class ClientController {

  @Autowired private AppAuthService appAuthService;
  @Autowired private LoadBalancer loadBalancer;
  @Autowired private DiscoveryService discoveryService;

  @PostMapping(path = "/app/keyToken", produces = "application/json")
  public String getKeyToken(@RequestParam String clientId) {
    log.info(">>>   接收到[{}]的请求   <<<", clientId);

    // 获取并保存token至redis
    String keyToken = appAuthService.getAndPersistKeyToken(clientId);
    // 获取可用的推送服务器列表
    List<InetSocketAddress> addressList = discoveryService.getAllServers(false);
    // 获取负载均衡策略选取的服务器地址
    InetSocketAddress remoteAddress = loadBalancer.selectPushServer(addressList, clientId);

    // 构造响应对象
    WebResponse response = new WebResponse();
    if (remoteAddress == null) {
      // 如果没有可用的服务器，则设置失败
      response.setCode(CODE.FAILURE);
      response.getData().put("message", "没有可用的推送服务器");
    } else {
      response.setCode(CODE.OK);
      response.getData().put("keyToken", keyToken);
      response.getData().put("ipAddress", remoteAddress.getAddress().getHostAddress());
      response.getData().put("port", remoteAddress.getPort());
    }

    log.info(">>>   向客户端[{}]发送响应   <<<", clientId);

    return JSON.toJSONString(response);
  }
}
