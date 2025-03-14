package org.example.chat_huawei.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.example.chat_huawei.entity.UserFriend;
import org.example.chat_huawei.mapper.UserFriendMapper;
import org.example.chat_huawei.service.IUserFriendApplyService;
import org.example.chat_huawei.service.IUserFriendService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 用户好友表 服务实现类
 * </p>
 *
 * @author lzj
 * @since 2024-12-9
 */
@Service
public class UserFriendServiceImpl extends ServiceImpl<UserFriendMapper, UserFriend> implements IUserFriendService {

    @Autowired
    private UserFriendMapper userFriendMapper;

    @Autowired
    private IUserFriendApplyService userFriendApplyService;

    @Override
    public boolean isFriend(int userId, int friendId) {
        QueryWrapper<UserFriend> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("friend_id", friendId);
        UserFriend userFriend = userFriendMapper.selectOne(wrapper);
        if(userFriend == null) {
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    @Transactional//声明为事务
    public boolean addFriend(int userId, int friendId,int id) {
        UserFriend userFriend = new UserFriend();
        userFriend.setUserId(userId);
        userFriend.setFriendId(friendId);
        //默认状态
        userFriend.setStatus("01");
        int insert = userFriendMapper.insert(userFriend);

        //双向
        UserFriend userFriend1 = new UserFriend();
        userFriend1.setUserId(friendId);
        userFriend1.setFriendId(userId);
        //默认状态
        userFriend1.setStatus("01");

        //删除该条申请记录
        int i = userFriendApplyService.deleteById(id);


        int insert1 = userFriendMapper.insert(userFriend1);

        return (insert1==1 && insert==1 && i==1);

    }

    @Override
    @Transactional
    public boolean deleteFriend(int userId, int friendId) {
        //单向删除
        QueryWrapper<UserFriend> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("friend_id", friendId);
        int delete = userFriendMapper.delete(wrapper);


        //另一边的修改好友状态
        UpdateWrapper<UserFriend> wrapper1 = new UpdateWrapper<>();
        wrapper1.eq("user_id", userId);
        wrapper1.eq("friend_id", friendId);
        wrapper1.set("status", "03");
        int update = userFriendMapper.update(wrapper1);
        return update==1 && delete==1;

    }

    @Override
    public List<UserFriend> getFriends(int userId,String status) {
        QueryWrapper<UserFriend> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("status", status);
        return userFriendMapper.selectList(wrapper);
    }
}
