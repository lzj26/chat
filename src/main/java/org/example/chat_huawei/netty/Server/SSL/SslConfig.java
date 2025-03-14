package org.example.chat_huawei.netty.Server.SSL;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 读取yml中的ssl配置
 */
@Configuration

public class SslConfig {
//    @Value("${server.ssl.key-store}")
    private String keyStorePath;

//    @Value("${server.ssl.key-store-password}")
    private String keyStorePassword;

//    @Value("${server.ssl.key-store-type}")
    private String keyStoreType;

//    @Value("${server.ssl.key-alias}")
    private String keyAlias;

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }
}
