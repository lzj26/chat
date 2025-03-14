package org.example.chat_huawei.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 接收者表
 * </p>
 *
 * @author lzj
 * @since 2024-12-9
 */
@TableName("message_recipients")
public class MessageRecipients implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息主表的id
     */
    private Long messageId;

    /**
     * 该条消息主表id对应接收者的id
     */
    private Integer receiveId;
    /**
     * 发送者id
     */
    private Integer senderId;
    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Integer getReceiveId() {
        return receiveId;
    }

    public void setReceiveId(Integer userId) {
        this.receiveId = userId;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return "MessageRecipients{" +
        "messageId = " + messageId +
        ",  = " + receiveId +
        ", isRead = " + isRead +
        ", id = " + id +
        ", cstCreate = " + cstCreate +
        ", createBy = " + createBy +
        ", cstModify = " + cstModify +
        ", updateBy = " + updateBy +
        ", remarks = " + remarks +
        ", delFlag = " + delFlag +
        "}";
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }
}
