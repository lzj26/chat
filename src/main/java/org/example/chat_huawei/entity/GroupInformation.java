package org.example.chat_huawei.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 群组基本信息表
 * </p>
 *
 * @author lzj
 * @since 2024-12-9
 */
@TableName("group_information")
public class GroupInformation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 群组id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 群组名
     */
    private String groupName;

    /**
     * 创建时间
     */
    private LocalDateTime cstCreate;

    /**
     * 创建人用户ID
     */
    private Integer createBy;

    /**
     * 更新时间
     */
    private LocalDateTime cstModify;

    /**
     * 更新人用户ID
     */
    private Integer updateBy;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 删除标记位
     */
    private String delFlag;

    /**
     * 群组描述
     */
    private String groupDescription;

    /**
     * 群组头像路径或URL
     */
    private String groupAvatar;

    /**
     * 群组标签（逗号分隔）
     */
    private String tags;

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 管理员列表（逗号分隔的用户ID）
     */
    private String adminList;

    /**
     * 隐私设置（0: public, 1: private, 2: invite_only）
     */
    private Integer privacySetting;

    /**
     * 邀请码
     */
    private String invitationCode;

    /**
     * 群组公告
     */
    private String announcement;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public LocalDateTime getCstCreate() {
        return cstCreate;
    }

    public void setCstCreate(LocalDateTime cstCreate) {
        this.cstCreate = cstCreate;
    }

    public Integer getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Integer createBy) {
        this.createBy = createBy;
    }

    public LocalDateTime getCstModify() {
        return cstModify;
    }

    public void setCstModify(LocalDateTime cstModify) {
        this.cstModify = cstModify;
    }

    public Integer getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Integer updateBy) {
        this.updateBy = updateBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getGroupAvatar() {
        return groupAvatar;
    }

    public void setGroupAvatar(String groupAvatar) {
        this.groupAvatar = groupAvatar;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public String getAdminList() {
        return adminList;
    }

    public void setAdminList(String adminList) {
        this.adminList = adminList;
    }

    public Integer getPrivacySetting() {
        return privacySetting;
    }

    public void setPrivacySetting(Integer privacySetting) {
        this.privacySetting = privacySetting;
    }

    public String getInvitationCode() {
        return invitationCode;
    }

    public void setInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    @Override
    public String toString() {
        return "GroupInformation{" +
        "id = " + id +
        ", groupName = " + groupName +
        ", cstCreate = " + cstCreate +
        ", createBy = " + createBy +
        ", cstModify = " + cstModify +
        ", updateBy = " + updateBy +
        ", remarks = " + remarks +
        ", delFlag = " + delFlag +
        ", groupDescription = " + groupDescription +
        ", groupAvatar = " + groupAvatar +
        ", tags = " + tags +
        ", memberCount = " + memberCount +
        ", adminList = " + adminList +
        ", privacySetting = " + privacySetting +
        ", invitationCode = " + invitationCode +
        ", announcement = " + announcement +
        "}";
    }
}
