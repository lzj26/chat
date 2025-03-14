package org.example.chat_huawei.netty.Server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.stream.ChunkedWriteHandler;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.example.chat_huawei.netty.Server.SSL.SslConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * 服务器启动类
 */
@Component
@EnableAsync//开启支持异步
public class ChatServer {

    @Value("${netty.server.port}")
    private int port;//启动的端口号

    @Resource
    SslConfig sslConfig;//ssl配置类

    @Autowired
    private  ApplicationContext applicationContext;//通过上下文每次获取新的处理器实例,处理器类也要添加@Scope("prototype")，指定创建模式为原型


    private EventLoopGroup bossGroup;//处理连接的线程池
    private EventLoopGroup workerGroup;//处理具体操作的线程池
    private ChannelFuture bind;//开启服务的对象
    //配置服务器

    @Async//异步支持，不然打包时会卡住进程
    public void start() throws CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException {
        //配置ssl
//        SslContext sslCtx = buildSslContext();


        //根据原理，我们需要两个进程boss,work
        //使用NioEventLoopGroup实现类即可
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        // 创建服务器端的启动对象
        ServerBootstrap bootstrap = new ServerBootstrap();
        //配置netty服务器的配置
        bootstrap.group(bossGroup, workerGroup)//指定主从进程
                .channel(NioServerSocketChannel.class)//指定为NIO的ServerSocketChannel
                .childHandler(new ChannelInitializer<SocketChannel>(){// 创建通道初始化对象
                    //注意，这里的SocketChannel是Netty包下的

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //流水线操作
                        //将ssl证书验证添加到流水线的第一个处理位置
//                        socketChannel.pipeline().addLast(sslCtx.newHandler(socketChannel.alloc()));

                        socketChannel.pipeline().addLast(new HttpServerCodec());//处理htpp的编解码
                        //将多个htpp请求聚合为一个
                        socketChannel.pipeline().addLast(new HttpObjectAggregator(65536));
                        socketChannel.pipeline().addLast(new ChunkedWriteHandler());//添加来支持大文件的传输

                        //websocket协议，使用/ws路径，也就是js中ip的后缀
                        socketChannel.pipeline().addLast(new WebSocketServerProtocolHandler("/ws"));
                        //添加
                        // 添加自定义的服务器处理器
                        socketChannel.pipeline().addLast(applicationContext.getBean(MyHandler.class));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)//设置连接队列的大小
                .childOption(ChannelOption.SO_KEEPALIVE, true);//设置保持连接选项

        //最后绑定端口，启动
        try {
            bind = bootstrap.bind(port).sync();
            bind.channel().closeFuture().sync();//结束释放资源
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //关闭netty服务的方法,释放资源,不然springboot项目一运行就开启netty，但是项目关闭这个不关闭.
    public void stop() {
        //释放资源
        bind.channel().closeFuture();

        bossGroup.shutdownGracefully();

        workerGroup.shutdownGracefully();

    }
    //手动建立netty的ssl配置
    private SslContext buildSslContext() throws  KeyStoreException, NoSuchAlgorithmException, CertificateException, java.security.cert.CertificateException, IOException {
        // 读取keystore文件
        KeyStore ks = KeyStore.getInstance(sslConfig.getKeyStoreType());
        try (InputStream is = new ClassPathResource(sslConfig.getKeyStorePath().substring(10)).getInputStream()) {
            ks.load(is, sslConfig.getKeyStorePassword().toCharArray());
        }

        // 创建KeyManagerFactory实例
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        try {
            kmf.init(ks, sslConfig.getKeyStorePassword().toCharArray());
            // 构建SslContext
            return SslContextBuilder.forServer(kmf)
                    //这个就是信任所有客户端
                    .trustManager(InsecureTrustManagerFactory.INSTANCE) // 如果需要验证客户端证书，要提供相应的信任管理器
                    .build();
        } catch (UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        }


    }

}