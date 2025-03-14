package org.example.chat_huawei.controller;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.chat_huawei.entity.GroupInformation;
import org.example.chat_huawei.entity.User;
import org.example.chat_huawei.service.IGroupInformationService;
import org.example.chat_huawei.service.IUserService;
import org.example.chat_huawei.util.Json;
import org.example.chat_huawei.util.Rest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author lzj
 * @since 2024-12-9
 */
@RestController
@RequestMapping("/user")
//@CrossOrigin(origins = "http://localhost:5173") // 指定允许的前端域名
public class UserController {

    @Autowired
    IUserService userService;
    @Autowired
    private IGroupInformationService groupInformationService;

    /**
     * 查询用户
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/findUser")
    public String findUser(HttpServletRequest request, HttpServletResponse response)  {
        //获取参数
        Map<String, Object> jsonMap = Json.getJsonMap(request);

        String keyWord = Json.getJsonString(jsonMap,"keyWord");


        //模糊查询
        List<User> userLink = userService.getUserLink(keyWord);
        //返回数据
        HashMap<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        if (userLink != null && userLink.size() > 0) {
            for (User user : userLink) {
                Map<String, Object> mapMap = new HashMap<>();
                mapMap.put("id", user.getId());
                mapMap.put("userName", user.getUserName());
                mapMap.put("userEmail", user.getEmail());
                mapMap.put("avatar",user.getAvatarUrl());
                list.add(mapMap);
            }
            resultMap.put("userLink", list);
            resultMap.put("count", list.size());

        }
        //返回
        Rest success = Rest.success(resultMap);
        String s = success.ToJsonString();

        System.out.println(s);
        return s;
//        return "hello";
    }

    /**
     * 根据id获取名称
     */
    @RequestMapping("/getNameById")
    public String getNameById(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> jsonMap = Json.getJsonMap(request);
        int userId = Json.getJsonInt(jsonMap,"userId");

        //检验是否存在
        User userInfo = userService.getUserByid(userId);
        if(userInfo == null) {
            return Rest.fail(400, "该用户不存在").ToJsonString();
        }

        User userByid = userService.getUserByid(userId);

        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("userName", userByid.getUserName());

        return Rest.success(resultMap).ToJsonString();
    }

    /**
     * 根据类型跟对应的id返回名称，头像等信息
     */

    @RequestMapping("/getInfo")
    public String getInfo(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> jsonMap = Json.getJsonMap(request);
        int id = Json.getJsonInt(jsonMap,"userId");
        String type = Json.getJsonString(jsonMap,"type");
        HashMap<String, Object> resultMap = new HashMap<>();
        //分类型查询
        if(type.startsWith("01")){
            User user = userService.getUserByid(id);
            resultMap.put("name", user.getUserName());
            resultMap.put("avtor", user.getAvatarUrl());
        }
        else {
            GroupInformation groupById = groupInformationService.getGroupById(id);
            resultMap.put("name", groupById.getGroupName());
            resultMap.put("avtor",groupById.getGroupAvatar());
        }
        return Rest.success(resultMap).ToJsonString();
    }


}
