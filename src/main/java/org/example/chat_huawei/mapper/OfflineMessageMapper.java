package org.example.chat_huawei.mapper;

import org.example.chat_huawei.entity.OfflineMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 离线消息表 Mapper 接口
 * </p>
 *
 * @author lzj
 * @since 2024-12-9
 */
@Mapper
public interface OfflineMessageMapper extends BaseMapper<OfflineMessage> {

}
