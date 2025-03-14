package org.example.chat_huawei.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.chat_huawei.entity.User;
import org.example.chat_huawei.service.IUserService;
import org.example.chat_huawei.util.Email;
import org.example.chat_huawei.util.Encryption;
import org.example.chat_huawei.util.Json;
import org.example.chat_huawei.util.Rest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
//@CrossOrigin(origins = "http://localhost:5173") // 指定允许的前端域名
public class LoginController {
    @Autowired
    Email email;

    @Autowired
    IUserService userService;
    /**
     * 获取验证码
     */
    @RequestMapping("/getCode")
    public String getCode(HttpServletRequest request, HttpSession session) {
        Map<String, Object> jsonMap = Json.getJsonMap(request);
        //获取邮箱
        String emailName = Json.getJsonString(jsonMap, "email");

        //查询邮箱是否存在
        User userByEmail = userService.getUserByEmail(emailName);
        if (userByEmail != null) {
            return Rest.fail("该邮箱已经存在").ToJsonString();
        }

        try{
            //生成随机验证码
            String code = email.getCode();
            //存储验证码到session中
            session.setAttribute("code", code);
            //发送
            email.sendMail(emailName, code);
            return Rest.success("发送验证码成功").ToJsonString();
        }
        catch(Exception e){
            e.printStackTrace();
            return Rest.fail("发送验证码失败").ToJsonString();
        }

    }

    /**
     * 注册
     */
    @RequestMapping("/register")
    public String register(HttpServletRequest request, HttpSession session) {
        Map<String, Object> jsonMap = Json.getJsonMap(request);
        //获取信息
        String emailName = Json.getJsonString(jsonMap, "email");
        String code = Json.getJsonString(jsonMap, "code");
        String password = Json.getJsonString(jsonMap, "password");
        String userName = Json.getJsonString(jsonMap, "userName");

        //验证session中的验证码
        if(session.getAttribute("code") != null && session.getAttribute("code").equals(code)){
            //用户新增
            int i = userService.addUser(userName, password, emailName);
            if(i != -1){
                return Rest.success("添加用户成功").ToJsonString();
            }else {
                return Rest.fail("添加用户失败").ToJsonString();
            }
        }
        else {
            return Rest.fail("验证码错误").ToJsonString();
        }

    }

    /**
     * 登录
     */
    @RequestMapping("/login")
    public String login(HttpServletRequest request) {
        Map<String, Object> jsonMap = Json.getJsonMap(request);
        //获取信息
        String password = Json.getJsonString(jsonMap, "password");
        String userName = Json.getJsonString(jsonMap, "userName");

        //检验
        User byName = userService.findByName(userName);
        if(byName != null){
            boolean b = Encryption.checkPassword(password, byName.getPassword());
            if(b){
                HashMap<String,Object> map = new HashMap<>();
                map.put("userName",userName);
                map.put("userId",byName.getId());
                return Rest.success(map,"登录成功").ToJsonString();
            }
        }
        return Rest.fail("用户名或者密码错误").ToJsonString();
    }
}
