package org.example.chat_huawei.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 离线消息表
 * </p>
 *
 * @author lzj
 * @since 2024-12-9
 */
@TableName("offline_message")
public class OfflineMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 消息主表的id
     */
    private Long messageId;

    /**
     * 对应接收者的id
     */
    private Integer userId;

    /**
     * 待接收人的用户名
     */
    private String username;

    /**
     * 该条消息的发送者
     */
    private String sourcename;

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

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSourcename() {
        return sourcename;
    }

    public void setSourcename(String sourcename) {
        this.sourcename = sourcename;
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
        return "OfflineMessage{" +
        "id = " + id +
        ", messageId = " + messageId +
        ", userId = " + userId +
        ", username = " + username +
        ", sourcename = " + sourcename +
        ", cstCreate = " + cstCreate +
        ", createBy = " + createBy +
        ", cstModify = " + cstModify +
        ", updateBy = " + updateBy +
        ", remarks = " + remarks +
        ", delFlag = " + delFlag +
        "}";
    }
}
