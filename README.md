# PigeonPush
Scaled message push service framework. 可扩展消息推送服务框架

## pigeonpush-sdk模块
本模块作为客户端SDK使用，内置`okhttp`向route模块发送`HTTP`请求，然后向返回的push-server地址发起`TCP`长连接，主要功能点如下：
- 为了简单起见，将`WIFI`网卡的`MAC`地址作为设备唯一标识符，如果需要改进，可以更改为注册/鉴权模式；
- 客户端通过`okhttp`向route模块发送`HTTP`请求，如果请求成功，route模块将会返回`deviceToken`和push-server（推送服务器）地址；如果请求不成功，客户端将会反复发送`HTTP`请求直至成功；
- 客户端获得推送服务器地址之后，随即向服务器发起连接，可能会出现以下几种情形：
  - 连接成功：每次成功建立都要向`Channel`对应的`pipeline`中动态添加鉴权模块`AuthHandler`。然后立即向服务器发送包含有`deviceId`和`deviceToken`的鉴权请求，如果验证成功则动态删除`AuthHandler`；验证失败则重新向route服务器发起`HTTP`请求；
  - 连接失败：按照一定时间间隔，进行重连操作；如果重连次数超过阈值，重新向route服务器发起`HTTP`请求；
 - 如果在一定的时间内，客户端都没有进行写操作（没有接收到信息），则主动向推送服务器发送心跳信息，这么做的目的是为了维持长连接。如果若干次心跳连接都没有收到任何回应，客户端会主动断开连接，开启重连；
 - 接收推送信息：根据用户自定的方法消费接收到的推送信息。
 
 ## pigeonpush-reglog模块
 本模块在收到SDK的http连接请求后，会随机产生一个用于后续步骤鉴权的`keyToken`，同时也具有软负载中心的功能，在可用的推送服务器中选择一个返回给SDK。主要有以下两个功能要点：
 - SDK设备对应的`deviceId`和`keyToken`将会被成对存储在Redis中，在后面的鉴权步骤中，推送服务器将会查询这些数据进行验证
 - 为了实现软负载中心的功能，采用了Zookeeper作为服务注册中心，每一台推送服务器都是在Zookeeper上注册，reglog模块会监听Zookeeper上服务器的注册路径，从而获取最新准确的推送服务器socket地址列表
 
 ## pigeon-server推送服务器模块

## TODO:
- [ ] 设备鉴权模式修改为注册/鉴权模式
- [ ] `HTTP`请求策略修改为失败若干次之后抛出异常 