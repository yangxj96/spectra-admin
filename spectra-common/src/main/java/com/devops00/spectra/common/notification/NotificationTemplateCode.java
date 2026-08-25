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
 * 内置通知模板编码，业务调用方不再重复书写模板字符串。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
public final class NotificationTemplateCode {

    /** 登录验证码模板。 */
    public static final String SECURITY_LOGIN_CODE = "security.login-code";

    /** 绑定手机号验证码模板。 */
    public static final String SECURITY_BIND_PHONE_CODE = "security.bind-phone-code";

    /** 绑定邮箱验证码模板。 */
    public static final String SECURITY_BIND_EMAIL_CODE = "security.bind-email-code";

    /** 重置密码验证码模板。 */
    public static final String SECURITY_RESET_PASSWORD_CODE = "security.reset-password-code";

    /** 工作流待办模板。 */
    public static final String WORKFLOW_TASK_TODO = "workflow.task.todo";

    /** 工作流处理结果模板。 */
    public static final String WORKFLOW_TASK_RESULT = "workflow.task.result";

    /** OA 会议邀请模板。 */
    public static final String OA_MEETING_INVITATION = "oa.meeting.invitation";

    /** OA 公告发布模板。 */
    public static final String OA_NOTICE_PUBLISHED = "oa.notice.published";

    /** OA 文档发布模板。 */
    public static final String OA_DOCUMENT_PUBLISHED = "oa.document.published";

    /** OA 申请状态模板。 */
    public static final String OA_APPLICATION_STATUS = "oa.application.status";

    /** OA 合同履约节点提醒模板。 */
    public static final String OA_CONTRACT_MILESTONE_REMINDER = "oa.contract.milestone.reminder";

    /** 服务监控告警模板。 */
    public static final String SYSTEM_SERVICE_MONITOR_ALERT = "system.service-monitor-alert";

    /** 无动态参数的系统通知模板。 */
    public static final String SYSTEM_NOTICE = "system.notice";

    private NotificationTemplateCode() {
    }
}
