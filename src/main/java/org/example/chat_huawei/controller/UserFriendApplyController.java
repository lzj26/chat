package org.example.chat_huawei.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.example.chat_huawei.entity.User;
import org.example.chat_huawei.entity.UserFriendApply;
import org.example.chat_huawei.service.IUserFriendApplyService;
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
import java.util.logging.Logger;

/**
 * <p>
 * 用户好友申请表 前端控制器
 * </p>
 *
 * @author lzj
 * @since 2024-12-9
 */

@RestController
@RequestMapping("/userFriendApply")
//@CrossOrigin(origins = "http://localhost:5173") // 指定允许的前端域名
public class UserFriendApplyController {
    Logger logger = Logger.getLogger(UserFriendApplyController.class.getName());
    @Autowired
    IUserFriendApplyService userFriendApplyService;
    @Autowired
    IUserService userService;
    @Autowired
    IUserFriendService userFriendService;
    /**
     * 好友申请记录新增
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/add")
    public String addSave(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //获取参数
        Map<String, Object> jsonMap = Json.getJsonMap(request);
        Integer userId = Json.getJsonInt(jsonMap, "userId");
        Integer friendId = Json.getJsonInt(jsonMap, "friendId");

        try {
            //检验是否存在
            User userInfo = userService.getUserByid(userId);
            if(userInfo == null) {
                return Rest.fail(400, "该用户不存在").ToJsonString();
            }

            User userInfo1 = userService.getUserByid(friendId);
            if(userInfo1 == null) {
                return Rest.fail(400, "对方不存在").ToJsonString();
            }

            //检验是否已经存在申请记录
            boolean having = userFriendApplyService.isHaving(userId, friendId);
            if(having) {
                return Rest.fail("已经存在好友申请记录").ToJsonString();
            }

            boolean friend = userFriendService.isFriend(userId, friendId);
            if(friend) {
                return Rest.fail("已经存在好友关系").ToJsonString();
            }

            //新增申请记录
            userFriendApplyService.add(userId, friendId);

        } catch (Exception e) {
            // 打印错误日志
           logger.info("添加用户好友申请失败");
            return Rest.fail( "添加用户好友申请失败").ToJsonString();
        }

        return Rest.success("添加用户好友申请成功").ToJsonString();
    }

    //查看待接受的好友申请
    @RequestMapping("/friendView")
    public String friendView(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> jsonMap = Json.getJsonMap(request);
        Integer userId = Json.getJsonInt(jsonMap, "userId");
        try {
            //检验是否存在
            User userInfo = userService.getUserByid(userId);
            if(userInfo == null) {
                return Rest.fail(400, "该用户不存在").ToJsonString();
            }

            //查询待处理的列表
            List<UserFriendApply> userFriendApplies = userFriendApplyService.findByFriendId(userId);
            HashMap<String,Object> result = new HashMap<>();
            if(userFriendApplies != null || userFriendApplies.size() > 0) {
                List<Map<String, Object>> tempMapList = new ArrayList<>();
                for (UserFriendApply userFriendApplyExt : userFriendApplies) {
                    Map<String, Object> tempMap = new HashMap<String, Object>();
                    tempMap.put("id", userFriendApplyExt.getId());
                    //获取对方用户名
                    User userByid = userService.getUserByid(userFriendApplyExt.getUserId());
                    String name= userByid.getUserName();
                    tempMap.put("userName", name);
                    tempMap.put("userId", userFriendApplyExt.getUserId());
                    tempMap.put("cstCreate", userFriendApplyExt.getCstCreate());
                    tempMap.put("avatar",userByid.getAvatarUrl());
                    tempMapList.add(tempMap);
                }
                result.put("dataList", tempMapList);
                result.put("count",tempMapList.size());
            }
            return Rest.success(result).ToJsonString();

        }catch (Exception e) {
            return Rest.fail("查询失败").ToJsonString();
        }

    }

    //查询用户发起的好友申请
    @RequestMapping("/userView")
    public String userView(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> jsonMap = Json.getJsonMap(request);
        Integer userId = Json.getJsonInt(jsonMap, "userId");

        try {
            //检验是否存在
            User userInfo = userService.getUserByid(userId);
            if(userInfo == null) {
                return Rest.fail(400, "该用户不存在").ToJsonString();
            }
            //获取发起的申请
            List<UserFriendApply> userFriendApplies = userFriendApplyService.findByUserId(userId);
            HashMap<String,Object> result = new HashMap<>();
            if(userFriendApplies != null || userFriendApplies.size() > 0) {
                List<Map<String, Object>> tempMapList = new ArrayList<>();
                for (UserFriendApply userFriendApplyExt : userFriendApplies) {
                    Map<String, Object> tempMap = new HashMap<String, Object>();
                    tempMap.put("id", userFriendApplyExt.getId());
                    //获取对方用户名
                    String name=userService.getUserByid(userFriendApplyExt.getFriendId()).getUserName();
                    tempMap.put("friendName", name);
                    tempMap.put("friendId", userFriendApplyExt.getFriendId());
                    tempMap.put("cstCreate", userFriendApplyExt.getCstCreate());
                    tempMapList.add(tempMap);
                }
                result.put("dataList", tempMapList);
                result.put("count",tempMapList.size());
            }
            return Rest.success(result).ToJsonString();

        }catch (Exception e) {
            return Rest.fail("查询失败").ToJsonString();
        }
    }

    //删除该条申请记录
    @RequestMapping("/delete")
    public String delete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> jsonMap = Json.getJsonMap(request);
        int id = Json.getJsonInt(jsonMap, "id");
        try{
            //执行删除
            int i = userFriendApplyService.deleteById(id);
            if(i == 1) {
                return Rest.success("删除好友记录成功").ToJsonString();
            }
            else  return Rest.fail("删除失败").ToJsonString();

        }
        catch (Exception e) {
            return Rest.fail("删除失败").ToJsonString();
        }
    }


}
