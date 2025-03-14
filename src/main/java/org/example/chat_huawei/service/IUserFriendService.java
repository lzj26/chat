package org.example.chat_huawei.service;

import org.example.chat_huawei.entity.UserFriend;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户好友表 服务类
 * </p>
 *
 * @author lzj
 * @since 2024-12-9
 */
public interface IUserFriendService extends IService<UserFriend> {

    //判断是否好友
    boolean isFriend(int userId, int friendId);

    //新增好友记录
    boolean addFriend(int userId, int friendId,int id);

    //删除好友记录
    boolean deleteFriend(int userId, int friendId);

    //查询用户好友
    List<UserFriend> getFriends(int userId,String status);


}
