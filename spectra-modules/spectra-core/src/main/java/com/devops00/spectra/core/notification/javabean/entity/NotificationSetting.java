package com.devops00.spectra.core.notification.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.UUID;

/// 用户通知设置表
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/19
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_notification_setting")
public class NotificationSetting extends BaseEntity implements Serializable {

    /// 用户ID（一对一关联）
    @TableField(value = "user_id")
    private UUID userId;

    /// 是否接收系统通知：true-接收, false-不接收
    @TableField(value = "system_enabled")
    private Boolean systemEnabled;

    /// 是否接收工作流通知：true-接收, false-不接收
    @TableField(value = "workflow_enabled")
    private Boolean workflowEnabled;

    /// 是否接收OA通知：true-接收, false-不接收
    @TableField(value = "oa_enabled")
    private Boolean oaEnabled;

    /// 是否接收站内信：true-接收, false-不接收
    @TableField(value = "inner_mail_enabled")
    private Boolean innerMailEnabled;

    /// 是否接收待审批通知：true-接收, false-不接收
    @TableField(value = "approval_enabled")
    private Boolean approvalEnabled;

    /// 免打扰模式：true-开启, false-关闭
    @TableField(value = "do_not_disturb")
    private Boolean doNotDisturb;

    /// 免打扰开始时间（如22:00:00）
    @TableField(value = "do_not_disturb_start")
    private LocalTime doNotDisturbStart;

    /// 免打扰结束时间（如08:00:00）
    @TableField(value = "do_not_disturb_end")
    private LocalTime doNotDisturbEnd;
}
