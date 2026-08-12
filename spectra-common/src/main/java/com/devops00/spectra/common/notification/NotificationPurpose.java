/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.common.notification;

/**
 * 通知用途，用于选择模板、执行渠道策略并应用用户通知偏好。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
public enum NotificationPurpose {

    /**
     * 用户登录验证码，属于不可被用户关闭的安全通知。
     */
    LOGIN_CODE,

    /**
     * 绑定或更换手机号时的验证码，属于不可被用户关闭的安全通知。
     */
    BIND_PHONE_CODE,

    /**
     * 绑定或更换邮箱时的验证码，属于不可被用户关闭的安全通知。
     */
    BIND_EMAIL_CODE,

    /**
     * 重置密码时的身份验证码，属于不可被用户关闭的安全通知。
     */
    RESET_PASSWORD_CODE,

    /**
     * 异地登录、凭据变更等账号安全事件告警。
     */
    SECURITY_ALERT,

    /**
     * 平台级系统公告、功能变更或运维通知。
     */
    SYSTEM_NOTICE,

    /**
     * 工作流待办任务到达通知。
     */
    WORKFLOW_TODO,

    /**
     * 工作流审批通过、驳回、撤回或结束等结果通知。
     */
    WORKFLOW_RESULT,

    /**
     * OA 公告、会议、合同、文档等业务事件通知。
     */
    OA_NOTICE,

    /**
     * OA 截止时间、待处理事项或业务里程碑提醒。
     */
    OA_REMINDER,

    /**
     * 用户之间或系统内部发送的普通站内消息。
     */
    INNER_MESSAGE
}
