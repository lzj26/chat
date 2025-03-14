package org.example.chat_huawei.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 群组与用户关系表
 * </p>
 *
 * @author lzj
 * @since 2024-12-9
 */
@TableName("group_user_relation")
public class GroupUserRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 群组ID
     */
    private Integer groupId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 添加时间
     */
    private LocalDateTime addTime;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public LocalDateTime getAddTime() {
        return addTime;
    }

    public void setAddTime(LocalDateTime addTime) {
        this.addTime = addTime;
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

    @Override
    public String toString() {
        return "GroupUserRelation{" +
        "id = " + id +
        ", groupId = " + groupId +
        ", userId = " + userId +
        ", addTime = " + addTime +
        ", cstCreate = " + cstCreate +
        ", createBy = " + createBy +
        ", cstModify = " + cstModify +
        ", updateBy = " + updateBy +
        ", remarks = " + remarks +
        ", delFlag = " + delFlag +
        "}";
    }
}
