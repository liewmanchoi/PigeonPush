# PigeonPush
Scaled message push service framework. 可扩展消息推送服务框架

## Redis中的数据表
在分析各个模块的功能之前，首先列举需要存储在Redis中供各个模块共用的数据表：
- token表：存储`clientId`与`token`的对应关系
  - 数据结构：字符串类型
  - 键：PIGEON:AUTH:`clientId`
  - 值：token字符串
- 路由表：存储`clientId`与推送服务器地址的对应关系，供消息分发模块向推送服务器下发消息时，确定应该调用推送服务器
  - 数据结构：字符串类型
  - 键：PIGEON:ROUTE:`clientId`
  - 值：对应的推送服务socket器地址
- 待确认消息表：存储`clientId`与未接收到ACK的推送消息`messageId`的对应关系
  - 数据结构：集合类型
  - 键：PIGEON:WAIT_ACK:`clientId`
  - 值：`messageId`集合
- 消息表：存储`messageId`与消息体的对应关系
  - 数据结构：散列类型
  - 键：PIGEON:MSG_BODY:`messageId`
  - 值：`title`: content; `text`: content

## pigeonpush-sdk模块
本模块作为客户端SDK使用，内置`okhttp`向route模块发送`HTTP`请求，然后向返回的push-server地址发起`TCP`长连接，主要功能点如下：
- 为了简单起见，将`WIFI`网卡的`MAC`地址作为设备唯一标识符，如果需要改进，可以更改为注册/鉴权模式；
- 客户端通过`okhttp`向route模块发送`HTTP`请求，如果请求成功，route模块将会返回`token`和push-server（推送服务器）地址；如果请求不成功，客户端将会反复发送`HTTP`请求直至成功；
- 客户端获得推送服务器地址之后，随即向服务器发起连接，可能会出现以下几种情形：
  - 连接成功：每次成功建立都要向`Channel`对应的`pipeline`中动态添加鉴权模块`AuthHandler`。然后立即向服务器发送包含有`clientId`和`token`的鉴权请求，如果验证成功则动态删除`AuthHandler`；验证失败则重新向route服务器发起`HTTP`请求；
  - 连接失败：按照一定时间间隔，进行重连操作；如果重连次数超过阈值，重新向route服务器发起`HTTP`请求；
 - 如果在一定的时间内，客户端都没有进行写操作（没有接收到信息），则主动向推送服务器发送心跳信息，这么做的目的是为了维持长连接的存活。如果若干次心跳连接都没有收到任何回应，客户端会主动断开连接，开启重连；
 - 接收推送信息：根据用户自定义的方法消费接收到的推送信息。
 
 ## pigeonpush-reglog模块
 本模块在收到SDK的http连接请求后，会随机产生一个用于后续步骤鉴权的`token`，同时也具有软负载中心的功能，在可用的推送服务器中选择一个返回给SDK。主要有以下两个功能要点：
 - SDK设备对应的`clientId`和`token`将会被成对存储在Redis的**token表**中，在后面的鉴权步骤中，推送服务器将会查询这些数据进行验证；
 - 为了实现软负载中心的功能，采用了Zookeeper作为服务注册中心，每一台推送服务器都是在Zookeeper上注册，reglog模块会监听Zookeeper上服务器的注册路径，从而获取最新准确的推送服务器socket地址列表。
 
 ## pigeon-server推送服务器模块
 推送服务器模块push-server和消息分发模块delivery同为推送系统的核心的两个模块，它的核心作用是与客户端SDK保持长连接，进行消息的推送与上传，主要功能点如下：
 - 鉴权：每次和SDK建立连接后，都要校验连接权限，具体方法是将SDK上传的`token`与Redis中存储的`token`值进行比对：
    - 如果两者不同，说明没有相应的权限，主动断开连接；
    - 如果两者相同，在路由表和通道关系表中建立对应的表项；
 - 推送消息：接受消息分发模块(delivery)的RPC调用，向SDK推送消息
    - 如果通道关系表中不存在该连接，首先删除对应的表项，然后主动关闭连接;
    - 如果消息体为空，主动向Redis查询`messageId`对应的消息体，填充消息后再进行消息推送；
 - **通道关系表**：由于推送服务器同时连接了大量的SDK，为了在给特定的SDK推送消息时能够找到对应的`Channel`，因此推送服务器使用`HashMap`存储`deviceId`与`Channel`的映射，后文中称之为通道关系表；
 - zookeeper注册：每台推送服务器上线后，都要向Zookeeper集群注册自己，这样reglog模块才能进行负载均衡；
 - 保持长连接：
    - 在接收到SDK发送的`PING`消息后，回复`PONG`；
    - 如果超过一定时间阈值都没有收到SDK的心跳信息`PING`，则主动断开连接，删除token表、通道关系表和路由表中对应的表项；
    - 如果连接被对端(SDK)主动关闭，删除token表、通道关系表和路由表中对应的表项；
 - 接收回执：在接收到SDK发送的ACK消息后，删除Redis**待确认消息**表中的相应记录；
 - 主动拉取消息：接收到SDK发送的`PING`消息后（说明此时连接处于空闲状态），发起对消息分发模块delivery的RPC调用。
 
 ## pigeon-delivery消息分发模块
 
 
 
 ## pigeon-notification消息接入模块
 
 ## 额外事项：
 - 对象池：由于`Message`对象会在系统中频繁创建与销毁，因此使用Netty自带的对象池能够显著提升系统性能，防止垃圾回收带来的STW(stop the world)停顿
    - 复用reuse时机：
        - 反序列化生成`Message`对象
        - 主动构造`Message`对象
    - 回收recycle时机：
        - 序列化`Message`对象为字节流后
        - `Message`对象已经被使用完成后（比如将`Message`对象转化成`PushMessage`对象后）


## TODO:
- [ ] 设备鉴权模式修改为注册/鉴权模式
- [ ] `HTTP`请求策略修改为失败若干次之后抛出异常 