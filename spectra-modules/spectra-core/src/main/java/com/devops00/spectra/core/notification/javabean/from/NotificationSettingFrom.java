package com.devops00.spectra.core.notification.javabean.from;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息设置更新入参
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSettingFrom {

    /**
     * 是否接收系统通知
     */
    private Boolean systemEnabled;

    /**
     * 是否接收工作流通知
     */
    private Boolean workflowEnabled;

    /**
     * 是否接收OA通知
     */
    private Boolean oaEnabled;

    /**
     * 是否接收站内信
     */
    private Boolean innerMailEnabled;

    /**
     * 是否接收待审批通知
     */
    private Boolean approvalEnabled;

    /**
     * 免打扰模式
     */
    private Boolean doNotDisturb;

    /**
     * 免打扰开始时间（如22:00:00）
     */
    private String doNotDisturbStart;

    /**
     * 免打扰结束时间（如08:00:00）
     */
    private String doNotDisturbEnd;
}
