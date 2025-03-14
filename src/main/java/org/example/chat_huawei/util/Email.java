package org.example.chat_huawei.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 邮件发送器
 */
@Component
public class Email {
    private static final Random rand = new Random();
    //发送邮件
    //自动装配邮件发送的Bean
    @Autowired
    JavaMailSender mailSender;

    //拿邮件发送者的账号
    @Value("${spring.mail.username}")
    String mailFrom;


    public void sendMail(String emailTarget,String code) {
        //邮件封装器
        SimpleMailMessage message = new SimpleMailMessage();
        //设置邮件标题
        message.setSubject("落魄山");
//       message.setSubject("[溧水区公安局]关于检测翻墙及浏览不良内容的警告");
        //设置邮件内容
//        message.setText("刘家豪先生您好，经监控和公安联网巡查发现，您近期存在频繁翻墙观看不良视频的行为，\" +\n" +
//                "                \"现已通知相关单位，请在2024年12月17日17点前到溧水区公安局接受批评教育。");
        message.setText("""
               您本次的验证码为 %s\s
               如果这不是您申请的验证码请忽略并注意账户安全!\s
               """.formatted(code));
        //设置接收人，可以多个
        message.setTo(emailTarget);
        //邮件发送人,跟配置文件中保持一致
        message.setFrom(mailFrom);

        //利用JavaMailSender发送
        mailSender.send(message);

    }

    //获取随机验证码
    public String getCode(){
        return String.valueOf(rand.nextInt(1000,10000));
    }

}
