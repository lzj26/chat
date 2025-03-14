package org.example.chat_huawei.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;

import org.example.chat_huawei.entity.GroupInformation;
import org.example.chat_huawei.mapper.GroupInformationMapper;
import org.example.chat_huawei.service.IGroupInformationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lzj
 * @since 2024-10-15
 */
@Service
public class GroupInformationServiceImpl extends ServiceImpl<GroupInformationMapper, GroupInformation> implements IGroupInformationService {

    @Resource
    private GroupInformationMapper groupMapper;
    @Override
    public GroupInformation getGroupByName(String name) {
        QueryWrapper<GroupInformation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_name", name)
                .select("id");
        return groupMapper.selectOne(queryWrapper);
    }

    @Override
    public int addGroup(GroupInformation groupInformation) {
        return groupMapper.insert(groupInformation);
    }

    @Override
    public GroupInformation getGroupById(int groupId) {
        QueryWrapper<GroupInformation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", groupId);

        return groupMapper.selectOne(queryWrapper);
    }


    @Override
    public int getId(String groupName) {
        QueryWrapper<GroupInformation> queryWrapper = new QueryWrapper<>();
        QueryWrapper<GroupInformation> group = queryWrapper.eq("group_name", groupName);
        return groupMapper.selectOne(group).getId();
    }

    @Override
    public List<String> getGroupNames() {
        QueryWrapper<GroupInformation> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("group_name");
        List<GroupInformation> groupInformations = groupMapper.selectList(queryWrapper);
        List<String> groupNames = new ArrayList<>();
        if (groupInformations != null) {
            for (GroupInformation groupInformation : groupInformations) {
                groupNames.add(groupInformation.getGroupName());
            }
        }
        return groupNames;
    }

}
