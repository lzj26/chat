package org.example.chat_huawei.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.chat_huawei.entity.UserFriendApply;
import org.example.chat_huawei.mapper.UserFriendApplyMapper;
import org.example.chat_huawei.service.IUserFriendApplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户好友申请表 服务实现类
 * </p>
 *
 * @author lzj
 * @since 2024-12-9
 */
@Service
public class UserFriendApplyServiceImpl extends ServiceImpl<UserFriendApplyMapper, UserFriendApply> implements IUserFriendApplyService {

    @Autowired
    private UserFriendApplyMapper userFriendApplyMapper;

    @Override
    public boolean isHaving(int userId, int friendId) {
        QueryWrapper<UserFriendApply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("friend_id", friendId);
        UserFriendApply userFriendApply = userFriendApplyMapper.selectOne(queryWrapper);
        if(userFriendApply == null) {
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public int add(int userId, int friendId) {
        UserFriendApply userFriendApply = new UserFriendApply();
        userFriendApply.setUserId(userId);
        userFriendApply.setFriendId(friendId);
        return userFriendApplyMapper.insert(userFriendApply);
    }

    @Override
    public List<UserFriendApply> findByFriendId( int friendId) {
        QueryWrapper<UserFriendApply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("friend_id",friendId);
        return userFriendApplyMapper.selectList(queryWrapper);
    }

    @Override
    public List<UserFriendApply> findByUserId(int userId) {
        QueryWrapper<UserFriendApply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        return userFriendApplyMapper.selectList(queryWrapper);
    }

    @Override
    public int deleteById(int Id) {
        return userFriendApplyMapper.deleteById(Id);
    }
}
