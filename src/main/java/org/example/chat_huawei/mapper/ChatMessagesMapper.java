package org.example.chat_huawei.mapper;

import org.example.chat_huawei.entity.ChatMessages;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 消息主表 Mapper 接口
 * </p>
 *
 * @author lzj
 * @since 2024-12-9
 */
@Mapper
public interface ChatMessagesMapper extends BaseMapper<ChatMessages> {

}
