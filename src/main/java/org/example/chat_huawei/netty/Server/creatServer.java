package org.example.chat_huawei.netty.Server;

import jakarta.annotation.Resource;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * 通过监听器建立netty服务器
 */
@Component//在bean环境下
public class creatServer implements ApplicationListener<ContextRefreshedEvent> {
    //实现ContextRefreshedEvent，就是在监听所有bean都初始化成功之后进行的操作
    @Resource
    private ChatServer chatServer;
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {//监听到时触发的动作
        System.out.println("netty服务器启动了");
        try {
            chatServer.start();//开启netty服务器
        } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
