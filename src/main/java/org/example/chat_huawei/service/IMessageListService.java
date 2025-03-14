package org.example.chat_huawei.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.chat_huawei.entity.MessageList;


import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 消息列表 服务类
 * </p>
 *
 * @author lzj
 * @since 2024-09-13
 */
public interface IMessageListService extends IService<MessageList> {

   List<MessageList> getTest();

   List<MessageList> selectMessageListByChatId(int userId);

   //根据useId跟listId查询有无记录
   HashMap<String,MessageList> selectByuserIdAndListId(int userId,int listId);

   //新增记录
   int addMessageList(MessageList messageList);
}
