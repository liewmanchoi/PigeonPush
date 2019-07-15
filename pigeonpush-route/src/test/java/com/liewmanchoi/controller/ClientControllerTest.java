package com.liewmanchoi.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alibaba.fastjson.JSON;
import com.liewmanchoi.domain.response.HttpResponse;
import com.liewmanchoi.domain.response.HttpResponse.CODE;
import com.liewmanchoi.domain.user.DeviceInfo;
import com.liewmanchoi.service.AppAuthService;
import com.liewmanchoi.service.DiscoveryService;
import com.liewmanchoi.service.LoadBalancer;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(ClientController.class)
public class ClientControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private AppAuthService appAuthService;
  @MockBean
  private LoadBalancer loadBalancer;
  @MockBean
  private DiscoveryService discoveryService;

  @Test
  public void getKeyToken() throws Exception {
    MockitoAnnotations.initMocks(this);

    List<InetSocketAddress> list = new ArrayList<>();
    String id = UUID.randomUUID().toString();
    String token = UUID.randomUUID().toString();
    when(appAuthService.getAndPersistKeyToken(id)).thenReturn(token);

    String sad = "193.198.0.";
    for (int i = 0; i < 100; ++i) {
      InetSocketAddress address1 = new InetSocketAddress(sad + i, i);
      list.add(address1);
    }

    when(discoveryService.getAllServers(false)).thenReturn(list);
    InetSocketAddress socketAddress = new InetSocketAddress("192.158.1.3", 8090);
    when(loadBalancer.selectPushServer(discoveryService.getAllServers(false), id))
        .thenReturn(socketAddress);

    // 构造响应对象
    HttpResponse response = new HttpResponse();
    response.setCode(CODE.OK);
    DeviceInfo deviceInfo = new DeviceInfo();
    deviceInfo.setDeviceId(id);
    deviceInfo.setDeviceToken(token);
    deviceInfo.setServerAddress(socketAddress);

    response.getData().put("entity", deviceInfo);

    String res = JSON.toJSONString(response);

    this.mockMvc.perform(post(String.format("/app/keyToken?deviceId=%s", id)))
        .andExpect(status().isOk()).andExpect(content().string(res));
  }
}
