package org.example.chat_huawei.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Select;
import org.example.chat_huawei.entity.ChatMessages;
import org.example.chat_huawei.entity.MessageRecipients;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lzj
 * @since 2024-10-21
 */
public interface IMessageRecipientsService extends IService<MessageRecipients> {
        //往接收者表中插入记录
    int addIMessageRecipients(MessageRecipients messageRecipients);

    //根据消息id往参数的list添加查询到的实体类
    void getRecipientsByMessageId(long messageId, List<MessageRecipients> list);

    //根据id删除
    int deleteIMessageRecipients(long id );

    //查询历史聊天记录
    List<ChatMessages> getChatMessagesLink(int userId, int friendId);

    //查询消息列表
    List<HashMap<String,Integer>> getChatMessages(int userId);
}
