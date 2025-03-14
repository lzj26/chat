package org.example.chat_huawei.service;

import org.example.chat_huawei.entity.UserFriendApply;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户好友申请表 服务类
 * </p>
 *
 * @author lzj
 * @since 2024-12-9
 */
public interface IUserFriendApplyService extends IService<UserFriendApply> {

    //判断是否存在申请记录
    boolean isHaving(int userId, int friendId);

    //新增申请记录
    int add(int userId, int friendId);

    //查询待处理的好友申请
    List<UserFriendApply> findByFriendId( int friendId);

    //查询发起的好友申请
    List<UserFriendApply> findByUserId(int userId);

    //根据id删除
    int deleteById(int Id);

}
