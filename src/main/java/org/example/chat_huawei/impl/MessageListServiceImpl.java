package org.example.chat_huawei.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.chat_huawei.entity.MessageList;
import org.example.chat_huawei.mapper.MessageListMapper;
import org.example.chat_huawei.service.IMessageListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 消息列表 服务实现类
 * </p>
 *
 * @author lzj
 * @since 2024-09-13
 */
@Service
public class MessageListServiceImpl extends ServiceImpl<MessageListMapper, MessageList> implements IMessageListService {

    @Autowired
    private MessageListMapper messageListMapper;

    @Override
    public List<MessageList> getTest() {
       return messageListMapper.selectList(null);
    }

    @Override
    public List<MessageList> selectMessageListByChatId(int userId) {
        QueryWrapper<MessageList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("time");
        return messageListMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public HashMap<String, MessageList> selectByuserIdAndListId(int userId, int listId) {
        HashMap<String,MessageList> result=new HashMap<>();
        QueryWrapper<MessageList> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("list_id", listId);
        MessageList messageList = messageListMapper.selectOne(queryWrapper);
        if(messageList==null) return result;
        result.put("userId",messageList);
        QueryWrapper<MessageList> queryWrapper2=new QueryWrapper<>();
        queryWrapper2.eq("user_id", listId);
        queryWrapper2.eq("list_id", userId);
        MessageList messageList2 = messageListMapper.selectOne(queryWrapper2);
        result.put("listId",messageList);
        return result;
    }

    @Override
    public int addMessageList(MessageList messageList) {
        return messageListMapper.insert(messageList);
    }
}
