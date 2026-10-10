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

import java.util.Map;

/**
 * 可在没有登录线程上下文时发送通知的内部运行身份及其固定用途。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/09
 */
public enum NotificationSystemActor {

    WORKFLOW_TASK("WORKFLOW", Map.of(NotificationPurpose.WORKFLOW_TODO, NotificationTemplateCode.WORKFLOW_TASK_TODO,
            NotificationPurpose.WORKFLOW_RESULT, NotificationTemplateCode.WORKFLOW_TASK_RESULT)),
    OA_WORKFLOW_RESULT("OA", Map.of(NotificationPurpose.OA_NOTICE, NotificationTemplateCode.OA_APPLICATION_STATUS)),
    OA_CONTRACT_REMINDER("OA", Map.of(NotificationPurpose.OA_REMINDER,
            NotificationTemplateCode.OA_CONTRACT_MILESTONE_REMINDER)),
    SERVICE_MONITOR_ALERT("spectra-core", Map.of(NotificationPurpose.SYSTEM_NOTICE,
            NotificationTemplateCode.SYSTEM_SERVICE_MONITOR_ALERT));

    private final String sourceModule;

    private final Map<NotificationPurpose, String> templates;

    NotificationSystemActor(String sourceModule, Map<NotificationPurpose, String> templates) {
        this.sourceModule = sourceModule;
        this.templates = templates;
    }

    /** 仅允许该运行身份已登记的来源、用途和模板，且收件人必须是明确用户 ID。 */
    public boolean permits(NotificationRequest request) {
        return request != null
                && sourceModule.equals(request.sourceModule())
                && request.purpose() != null
                && request.templateGroupCode() != null
                && request.templateGroupCode().equals(templates.get(request.purpose()))
                && !request.recipientUserIds().isEmpty()
                && request.directAddresses().isEmpty();
    }

    /** 只有工作流任务能够按其明确的处理人登录名解析收件人。 */
    public boolean permitsLoginNameLookup() {
        return this == WORKFLOW_TASK;
    }
}
