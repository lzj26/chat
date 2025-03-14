package org.example.chat_huawei.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.chat_huawei.entity.ChatMessages;


import java.util.List;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lzj
 * @since 2024-10-21
 */
public interface IChatMessagesService extends IService<ChatMessages> {
        //往历史消息表中插入数据
        int addChatMessage(ChatMessages chatMessages);

        //根据id返回消息跟消息类型
        ChatMessages getMessageById(long id);

        //根据id返回发送时间
        ChatMessages getTimeById(long chatId);

        //根据传入时间获取所有更早时间的数据
        List<ChatMessages> getChatMessagesByTime(String time);

        //根据id删除
        int deleteChatMessageById(long id);

        //根据列表查询
        List<ChatMessages> getChatMessagesByLink(List<Long> list);
}
