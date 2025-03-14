package org.example.chat_huawei.mapper;

import org.example.chat_huawei.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author lzj
 * @since 2024-12-9
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
