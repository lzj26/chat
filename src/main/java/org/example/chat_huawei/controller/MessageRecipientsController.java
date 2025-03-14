package org.example.chat_huawei.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.chat_huawei.entity.ChatMessages;
import org.example.chat_huawei.entity.User;
import org.example.chat_huawei.service.IChatMessagesService;
import org.example.chat_huawei.service.IMessageRecipientsService;
import org.example.chat_huawei.service.IUserService;
import org.example.chat_huawei.util.FileDeal;
import org.example.chat_huawei.util.Json;
import org.example.chat_huawei.util.Rest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * 接收者表 前端控制器
 * </p>
 *
 * @author lzj
 * @since 2024-12-9
 */
@RestController
@RequestMapping("/messageRecipients")
//@CrossOrigin(origins = "http://localhost:5173") // 指定允许的前端域名
public class MessageRecipientsController {
    @Autowired
    IChatMessagesService chatMessagesService;
    @Autowired
    IMessageRecipientsService messageRecipientsService;

    @Autowired
    IUserService userService;
    /**
     * 查询用户跟特定好友的历史聊天记录
     */
    @RequestMapping("/getAllChats")
    public String getAllChats(HttpServletRequest request) throws IOException {
        Map<String, Object> map = Json.getJsonMap(request);

        int userId=Json.getJsonInt(map, "userId");
        int friendId=Json.getJsonInt(map, "friendId");

        //检验是否存在
        User userInfo = userService.getUserByid(userId);
        if(userInfo == null) {
            return Rest.fail(400, "该用户不存在").ToJsonString();
        }
        User userInfo1 = userService.getUserByid(friendId);
        if(userInfo1 == null) {
            return Rest.fail(400, "对方不存在").ToJsonString();
        }

        //查询历史聊天记录
        List<ChatMessages> chatMessagesLink = messageRecipientsService.getChatMessagesLink(userId, friendId);

        //返回数据
        HashMap<String,Object> result=new HashMap<>();
        if(chatMessagesLink !=null &&chatMessagesLink.size()>0) {
            List<HashMap<String,Object>> list=new ArrayList<>();
            for(ChatMessages chatMessages : chatMessagesLink) {
                HashMap<String,Object> item=new HashMap<>();
                //填充发送者跟接收者
                if(chatMessages.getSenderId()==userId) {
                    item.put("sendName",userInfo.getUserName());
                    item.put("receiveName",userInfo1.getUserName());
                }
                else {
                    item.put("sendName",userInfo1.getUserName());
                    item.put("receiveName",userInfo.getUserName());
                }
                //判断是不是图片先
                if(chatMessages.getMessage().startsWith("image/")){
                    //打开文件
                    File file=new File(chatMessages.getMessage());
                    if(file.exists()){//存在就转化
                        String s = FileDeal.encodeFileToBase64(file,chatMessages.getMessage());
                        item.put("message",s);
                    }
                    else {//路径不存在
                        item.put("message",chatMessages.getMessage());
                    }
                }
                else {
                    item.put("message",chatMessages.getMessage());
                }
                item.put("messageType",chatMessages.getMessageType());
                item.put("sendTime",chatMessages.getSentAt());
                item.put("id",chatMessages.getId());
                item.put("readStatus",chatMessages.getIsRead());

                list.add(item);
            }
            result.put("messages",list);
            result.put("count",list.size());
        }
        return Rest.success(result,"获取历史聊天信息成功").ToJsonString();
    }


}
