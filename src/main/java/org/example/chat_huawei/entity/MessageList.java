package org.example.chat_huawei.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 消息列表
 * </p>
 *
 * @author lzj
 * @since 2024-09-13
 */
@TableName("message_list")
public class MessageList implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 用户信息列表id,可能是好友或者群组
     */
    private Integer listId;

    /**
     * 类型说明（01：好友，02：群组）
     */
    private String type;

    /**
     * 最新信息的维护时间
     */
    private LocalDateTime time;

    /**
     * 最新信息
     */
    private String message;

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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getListId() {
        return listId;
    }

    public void setListId(Integer listId) {
        this.listId = listId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
        return "MessageList{" +
        "id = " + id +
        ", userId = " + userId +
        ", listId = " + listId +
        ", type = " + type +
        ", time = " + time +
        ", message = " + message +
        ", cstCreate = " + cstCreate +
        ", createBy = " + createBy +
        ", cstModify = " + cstModify +
        ", updateBy = " + updateBy +
        ", remarks = " + remarks +
        ", delFlag = " + delFlag +
        "}";
    }
}
