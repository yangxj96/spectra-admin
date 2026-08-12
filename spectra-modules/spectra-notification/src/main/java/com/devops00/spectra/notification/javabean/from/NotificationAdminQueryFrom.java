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

package com.devops00.spectra.notification.javabean.from;

import lombok.Data;

import java.util.UUID;

/**
 * 通知管理端分页查询参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Data
public class NotificationAdminQueryFrom {

    /**
     * 逻辑通知请求 ID。
     */
    private UUID requestId;
    /**
     * 投递任务 ID。
     */
    private UUID taskId;
    /**
     * 收件用户 ID。
     */
    private UUID recipientUserId;
    /**
     * 任务或请求状态。
     */
    private String status;
    /**
     * 通知渠道。
     */
    private String channel;
    /**
     * 通知用途。
     */
    private String purpose;
    /**
     * 来源模块。
     */
    private String sourceModule;
    /**
     * 业务类型。
     */
    private String businessType;
    /**
     * 业务 ID。
     */
    private String businessId;
}
