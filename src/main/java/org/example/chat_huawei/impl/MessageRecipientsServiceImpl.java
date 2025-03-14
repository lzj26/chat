package org.example.chat_huawei.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;

import org.example.chat_huawei.entity.ChatMessages;
import org.example.chat_huawei.entity.MessageRecipients;
import org.example.chat_huawei.mapper.ChatMessagesMapper;
import org.example.chat_huawei.mapper.MessageRecipientsMapper;
import org.example.chat_huawei.service.IChatMessagesService;
import org.example.chat_huawei.service.IMessageRecipientsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lzj
 * @since 2024-10-21
 */
@Service
public class MessageRecipientsServiceImpl extends ServiceImpl<MessageRecipientsMapper, MessageRecipients> implements IMessageRecipientsService {

    @Resource
    private MessageRecipientsMapper messageRecipientsMapper;
    @Resource
    private IChatMessagesService chatMessagesService;

    @Override
    public int addIMessageRecipients(MessageRecipients messageRecipients) {
        return messageRecipientsMapper.insert(messageRecipients);
    }

    @Override
    public void getRecipientsByMessageId(long messageId, List<MessageRecipients> list) {
        QueryWrapper<MessageRecipients> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("message_id", messageId);
        List<MessageRecipients> messageRecipientsList = messageRecipientsMapper.selectList(queryWrapper);
        //将查询到的接收者列表拼接到list列表中
        list.addAll(messageRecipientsList);
    }

    @Override
    public int deleteIMessageRecipients(long id) {
       return messageRecipientsMapper.deleteById(id);
    }

    @Override
    @Transactional
    public List<ChatMessages> getChatMessagesLink(int userId, int friendId) {

        //user->friend
        QueryWrapper<MessageRecipients> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sender_id", userId);
        queryWrapper.eq("receive_id", friendId);
        queryWrapper.select("message_id");
        queryWrapper.orderByAsc("message_id");
        List<MessageRecipients> userMessageList = messageRecipientsMapper.selectList(queryWrapper);


        //friend->user
        QueryWrapper<MessageRecipients> friendQueryWrapper = new QueryWrapper<>();
        friendQueryWrapper.eq("sender_id", friendId);
        friendQueryWrapper.eq("receive_id", userId);
        queryWrapper.select("message_id");
        friendQueryWrapper.orderByAsc("message_id");
        List<MessageRecipients> friendMessageList = messageRecipientsMapper.selectList(friendQueryWrapper);

        //整合正序排序
        List<Long> messageRecipientsList = getMessageRecipientsList(userMessageList, friendMessageList);
        //按列表查询聊天信息
        return chatMessagesService.getChatMessagesByLink(messageRecipientsList);
    }


    @Override
    public List<HashMap<String, Integer>> getChatMessages(int userId) {
      return messageRecipientsMapper.getChatMessages(userId);
    }


//    @Override
//    public List<HashMap<String, Object>> getChatMessages(int userId) {
//        //先查询userId作为接收者的去重list
//        QueryWrapper<MessageRecipients> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("receive_id", userId);
//        queryWrapper.orderByDesc("id");
//
//        return List.of();
//    }


    //整合排序
   private List<Long> getMessageRecipientsList( List<MessageRecipients> userMessageList,List<MessageRecipients> friendMessageList) {
        //整合的list
        List<Long> messageRecipientsList = new ArrayList<>();
        //获取彼此的长度
        int userLength = userMessageList.size();
        int friendLength = friendMessageList.size();
        //分别设置索引
       int userIndex = 0;
       int friendIndex = 0;

        //一次遍历解决
        while(userIndex < userLength || friendIndex < friendLength) {
            //记录当前位置的信息主表大小
            long user=Long.MAX_VALUE;
            long friend=Long.MAX_VALUE;

            //赋值
            if(userIndex < userLength) {
                user=userMessageList.get(userIndex).getMessageId();
            }
            if(friendIndex < friendLength) {
                friend=friendMessageList.get(friendIndex).getMessageId();
            }
            //判断
            if(user < friend) {
                messageRecipientsList.add(user);
                userIndex++;
            }
            else {
                messageRecipientsList.add(friend);
                friendIndex++;
            }
        }
        return messageRecipientsList;
    }

}
