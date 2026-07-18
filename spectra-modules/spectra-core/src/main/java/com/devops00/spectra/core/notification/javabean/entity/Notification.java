package com.devops00.spectra.core.notification.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/// 系统通知消息表
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/19
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_notification")
public class Notification extends BaseEntity implements Serializable {

    /// 消息标题
    @TableField(value = "title")
    private String title;

    /// 消息内容
    @TableField(value = "content")
    private String content;

    /// 消息类型：system-系统通知, workflow-工作流通知, oa-OA通知, inner_mail-站内信, approval-待我审批
    @TableField(value = "type")
    private String type;

    /// 发送者ID（站内信场景）
    @TableField(value = "sender_id")
    private UUID senderId;

    /// 发送者名称（冗余字段，避免频繁JOIN）
    @TableField(value = "sender_name")
    private String senderName;

    /// 点击跳转路径
    @TableField(value = "link")
    private String link;

    /// 是否已读：true-已读, false-未读
    @TableField(value = "is_read")
    private Boolean isRead;

    /// 阅读时间
    @TableField(value = "read_at")
    private Instant readAt;

    /// 接收者ID（消息归属用户）
    @TableField(value = "receiver_id")
    private UUID receiverId;

    /// 扩展数据（JSON格式，如流程实例ID、会议ID等）
    @TableField(value = "extra")
    private String extra;
}
