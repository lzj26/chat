package org.example.chat_huawei.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.chat_huawei.entity.GroupUserRelation;


import java.util.List;


/**
 * <p>
 * 群组与用户关系表 服务类
 * </p>
 *
 * @author lzj
 * @since 2024-10-21
 */
public interface IGroupUserRelationService extends IService<GroupUserRelation> {

    //往群组用户关联表添加记录,传入群组id，跟用户id添加
    int addGroupUserRelation(int grouId,int userId);

    //根据获取群组关联表中该群组的所有成员id列表
    List<GroupUserRelation> getUserIdList(int groupId);

}
