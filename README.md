# PegionPush
Scaled message push service framework. 可扩展消息推送服务框架

# pigeonpush-sdk模块
本模块作为客户端使用，利用`okhttp`向reglog模块发送`HTTP`请求，然后向返回的push-server地址发起`TCP`长连接，主要功能点如下：
- 为了简单起见，将`WIFI`网库的`MAC`地址作为设备唯一标识符，如果需要改进，可以更改为注册/鉴权模式；