package com.liewmanchoi.controller;

import com.alibaba.fastjson.JSON;
import com.liewmanchoi.domain.response.WebResponse;
import com.liewmanchoi.domain.response.WebResponse.CODE;
import com.liewmanchoi.domain.user.DeviceInfo;
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

  @PostMapping(path = "/app/keyToken")
  public String getKeyToken(@RequestParam String deviceId) {
    // 获取并保存token至redis
    String token = appAuthService.getAndPersistKeyToken(deviceId);
    // 获取可用的推送服务器列表
    List<InetSocketAddress> addressList = discoveryService.getAllServers(false);
    // 获取负载均衡策略选取的服务器地址
    InetSocketAddress socketAddress = loadBalancer.selectPushServer(addressList, deviceId);

    // 构造响应对象
    WebResponse response = new WebResponse();
    if (socketAddress == null) {
      // 如果没有可用的服务器，则设置失败
      response.setCode(CODE.FAILURE);
      response.getData().put("message", "没有可用的推送服务器");
    } else {
      response.setCode(CODE.OK);
      DeviceInfo deviceInfo = new DeviceInfo();
      deviceInfo.setDeviceId(deviceId);
      deviceInfo.setDeviceToken(token);
      deviceInfo.setServerAddress(socketAddress);

      response.getData().put("entity", deviceInfo);
    }
    return JSON.toJSONString(response);
  }
}
