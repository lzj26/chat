package org.example.chat_huawei.netty.Server;


import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 当springboot项目被关闭也停止netty服务
 */
@Component
public class EndServer implements DisposableBean {

    @Autowired
    private ChatServer chatServer;

    //关闭netty
    @Override
    public void destroy() throws Exception {
        chatServer.stop();
    }
}
