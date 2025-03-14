package org.example.chat_huawei.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.example.chat_huawei.entity.GroupInformation;
import org.example.chat_huawei.entity.MessageList;
import org.example.chat_huawei.entity.User;
import org.example.chat_huawei.service.IGroupInformationService;
import org.example.chat_huawei.service.IMessageListService;
import org.example.chat_huawei.service.IUserService;
import org.example.chat_huawei.util.Json;
import org.example.chat_huawei.util.Rest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 消息列表 前端控制器
 * </p>
 *
 * @author lzj
 * @since 2024-09-13
 */
@RestController
@RequestMapping("/messageList")
public class MessageListController {

    @Autowired
    IMessageListService messageListService;

    @Autowired
    private IUserService userService;
    @Autowired
    private IGroupInformationService groupInformationService;

    @ResponseBody
    @RequestMapping("/test")
    public String test() {
        List<MessageList> test = messageListService.getTest();

        return "test";
    }


    /**
     * 查询当前的信息列表
     */
    @RequestMapping("/List")
    public String messageList(HttpServletRequest request){
        Map<String, Object> jsonMap = Json.getJsonMap(request);
        //获取用户id
        int userId=Json.getJsonInt(jsonMap, "userId");

        HashMap<String,Object> resultMap=new HashMap<>();
        List<HashMap<String,Object>> list=new ArrayList<>();
        //查询信息列表
        if(userId !=Integer.MIN_VALUE){
            List<MessageList> messageLists = messageListService.selectMessageListByChatId(userId);

            for (MessageList messageList : messageLists) {
                HashMap<String,Object> map=new HashMap<>();
                //获取类型
                String type=messageList.getType();
                //获取id
                int listId=messageList.getListId();

                map.put("message",messageList.getMessage());
                map.put("time",messageList.getTime());
                map.put("type",type);
                map.put("listId",listId);
                //聊天的对方信息
                if(type.equals("01")){//好友
                    User userByid = userService.getUserByid(listId);
                    map.put("targetName",userByid.getUserName());
                    map.put("avatar",userByid.getAvatarUrl());
                }else {//群组
                    GroupInformation groupById = groupInformationService.getGroupById(listId);
                    map.put("targetName",groupById.getGroupName());
                    map.put("avatar",groupById.getGroupAvatar());
                }
                list.add(map);
            }
            resultMap.put("list",list);
        }
        else {
            return Rest.fail("用户id为空").ToJsonString();
        }

        return Rest.success(resultMap,"获取用户列表信息成功").ToJsonString();
    }
}
