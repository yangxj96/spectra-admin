package com.devops00.spectra.core.notification.javabean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.UUID;

/// 消息设置响应VO
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/19
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSettingVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /// 主键ID
    private UUID id;

    /// 用户ID
    private UUID userId;

    /// 是否接收系统通知
    private Boolean systemEnabled;

    /// 是否接收工作流通知
    private Boolean workflowEnabled;

    /// 是否接收OA通知
    private Boolean oaEnabled;

    /// 是否接收站内信
    private Boolean innerMailEnabled;

    /// 是否接收待审批通知
    private Boolean approvalEnabled;

    /// 免打扰模式
    private Boolean doNotDisturb;

    /// 免打扰开始时间
    private LocalTime doNotDisturbStart;

    /// 免打扰结束时间
    private LocalTime doNotDisturbEnd;
}
