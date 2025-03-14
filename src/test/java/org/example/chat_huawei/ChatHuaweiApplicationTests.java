package org.example.chat_huawei;


import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.example.chat_huawei.util.Email;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

@SpringBootTest
class ChatHuaweiApplicationTests {

    @Autowired
    Email email;
    @Resource
    DataSource dataSource;

    @Test
    void contextLoads() {
        email.sendMail("2630543395@qq.com","14981");
    }


}
