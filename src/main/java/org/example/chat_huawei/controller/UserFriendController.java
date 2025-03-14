package org.example.chat_huawei.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.chat_huawei.entity.User;
import org.example.chat_huawei.entity.UserFriend;
import org.example.chat_huawei.service.IUserFriendService;
import org.example.chat_huawei.service.IUserService;
import org.example.chat_huawei.util.Json;
import org.example.chat_huawei.util.Rest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户好友表 前端控制器
 * </p>
 *
 * @author lzj
 * @since 2024-12-9
 */
@RestController
@RequestMapping("/userFriend")
//@CrossOrigin(origins = "http://localhost:5173") // 指定允许的前端域名
public class UserFriendController {

    @Autowired
    IUserFriendService userFriendService;
    @Autowired
    IUserService userService;
    /**
     * 添加好友
     */
    @RequestMapping("add")
    public String add(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> jsonMap = Json.getJsonMap(request);
        int userId=Json.getJsonInt(jsonMap, "userId");
        int friendId=Json.getJsonInt(jsonMap, "friendId");
        int id=Json.getJsonInt(jsonMap, "id");

        try{
            boolean b = userFriendService.addFriend(userId, friendId,id);
            if(b) {
                return Rest.success("新增好友记录成功").ToJsonString();
            }
            else  return Rest.fail("新增失败").ToJsonString();
        }
        catch (Exception e){
            return Rest.fail("添加好友失败，原因："+e.getMessage()).ToJsonString();
        }

    }

    /**
     * 查询用户好友列表
     */
    @RequestMapping("/view")
    public String view(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> jsonMap = Json.getJsonMap(request);
        int userId=Json.getJsonInt(jsonMap, "userId");
        String status=Json.getJsonString(jsonMap, "status");

        //检验是否存在
        User userInfo = userService.getUserByid(userId);
        if(userInfo == null) {
            return Rest.fail(400, "该用户不存在").ToJsonString();
        }
        //查询好友列表
        List<UserFriend> userFriends = userFriendService.getFriends(userId, status);
        //返回数据
       HashMap<String, Object> resultMap = new HashMap<String, Object>();
        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        if (userFriends != null && userFriends.size() > 0) {
            for (UserFriend userFriend : userFriends) {
                Map<String, Object> friendMap = new HashMap<String, Object>();
                //根据对方id查询信息
                User user = userService.getUserByid(userFriend.getFriendId());
                //同时返回好友id方便转发信息不用查询数据库
                friendMap.put("friendId", userFriend.getFriendId());
                //查询对方用户名
                friendMap.put("friendName", user.getUserName());
                friendMap.put("friendEmail", user.getEmail());
                friendMap.put("avatar",user.getAvatarUrl());

                mapList.add(friendMap);
            }
            //往结果map中返回
            resultMap.put("userLink", mapList);
            resultMap.put("count", userFriends.size());

        }
        return Rest.success(resultMap).ToJsonString();

    }
}
