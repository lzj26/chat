# chat
这是一个基于websocket+webrtc+spring boot框架的在线聊天项目



## 介绍

主要借助websocket实现文本，图片的聊天，接祖webrtc实现视频聊天的功能。

后端使用springboot框架，使用netty作为websocket的服务端来建立信令服务器。

前端使用vue搭建页面。使用vuex作为状态管理，将websocket以及webrtc连接对象放在vuex中当作类似的全局变量。

学习笔记为：



## 部署

netty的配置在netty.Server文件夹下，默认在本地的8443端口启动。

推荐配置：因为获取视频需要在https的环境下，建议拥有ssl证书，才可以使用视频聊天。目前我是部署到服务器上，网址为 `www.luoposhan.tech`.

后端的其他文件夹都是spring boot的经典部分。



前端vue主要配置在store.js文件部分。包括websocket，webrtc的处理逻辑。

其中需要注意的是websocket的对应建立连接的地址。



