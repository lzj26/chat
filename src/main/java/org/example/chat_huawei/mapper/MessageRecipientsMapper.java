package org.example.chat_huawei.mapper;

import org.apache.ibatis.annotations.Select;
import org.example.chat_huawei.entity.MessageRecipients;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 接收者表 Mapper 接口
 * </p>
 *
 * @author lzj
 * @since 2024-12-9
 */
@Mapper
public interface MessageRecipientsMapper extends BaseMapper<MessageRecipients> {

    //查询消息列表
    @Select("select max(message_recipients.message_id),sender_id from message_recipients where receive_id=1 group by sender_id ;")
    List<HashMap<String,Integer>> getChatMessages(int userId);
}
