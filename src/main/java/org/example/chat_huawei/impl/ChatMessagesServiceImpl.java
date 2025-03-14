package org.example.chat_huawei.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;

import org.example.chat_huawei.entity.ChatMessages;
import org.example.chat_huawei.mapper.ChatMessagesMapper;
import org.example.chat_huawei.service.IChatMessagesService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
public class ChatMessagesServiceImpl extends ServiceImpl<ChatMessagesMapper, ChatMessages> implements IChatMessagesService {

    @Resource
    private ChatMessagesMapper chatMessagesMapper;

    @Override
    public int addChatMessage(ChatMessages chatMessages) {
        return chatMessagesMapper.insert(chatMessages);
    }

    @Override
    public ChatMessages getMessageById(long id) {
        QueryWrapper<ChatMessages> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)
                .select("message","message_type");
        return chatMessagesMapper.selectOne(queryWrapper);
    }

    @Override
    public ChatMessages getTimeById(long chatId) {
        QueryWrapper<ChatMessages> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", chatId)
                .select("sent_at");
        return chatMessagesMapper.selectOne(queryWrapper);
    }

    @Override
    public List<ChatMessages> getChatMessagesByTime(String time) {
        QueryWrapper<ChatMessages> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("sent_at", time);//筛选更早时间的数据
        return chatMessagesMapper.selectList(queryWrapper);
    }

    @Override
    public int deleteChatMessageById(long id) {
       return chatMessagesMapper.deleteById(id);
    }

    @Override
    public List<ChatMessages> getChatMessagesByLink(List<Long> list) {
        if(list == null || list.size() == 0){
            return null;
        }
        return chatMessagesMapper.selectBatchIds(list);
    }
}
