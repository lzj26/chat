package org.example.chat_huawei.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.example.chat_huawei.entity.GroupInformation;


import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lzj
 * @since 2024-10-15
 */
public interface IGroupInformationService extends IService<GroupInformation> {
    //查询有无该群组,返回id
    GroupInformation getGroupByName(String groupName);

    //创建群组,传入实体类创建
    int addGroup(GroupInformation groupInformation);

    GroupInformation getGroupById(int groupId);

    //根据群组名获取对应id
    int getId(String groupName);

    //查询所有群组名返回
    List<String> getGroupNames();
}
