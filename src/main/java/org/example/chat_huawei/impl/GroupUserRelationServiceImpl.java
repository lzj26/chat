package org.example.chat_huawei.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;

import org.example.chat_huawei.entity.GroupUserRelation;
import org.example.chat_huawei.mapper.GroupUserRelationMapper;
import org.example.chat_huawei.service.IGroupUserRelationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 群组与用户关系表 服务实现类
 * </p>
 *
 * @author lzj
 * @since 2024-10-21
 */
@Service
public class GroupUserRelationServiceImpl extends ServiceImpl<GroupUserRelationMapper, GroupUserRelation> implements IGroupUserRelationService {

    @Resource
    private GroupUserRelationMapper groupUserRelationMapper;

    @Override
    public int addGroupUserRelation(int grouId, int userId) {
        GroupUserRelation groupUserRelation = new GroupUserRelation();
        groupUserRelation.setGroupId(grouId);
        groupUserRelation.setUserId(userId);
        return groupUserRelationMapper.insert(groupUserRelation);
    }

    @Override
    public List<GroupUserRelation> getUserIdList(int groupId) {
        QueryWrapper<GroupUserRelation> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", groupId)
                .select("user_id");
        return groupUserRelationMapper.selectList(wrapper);
    }
}
