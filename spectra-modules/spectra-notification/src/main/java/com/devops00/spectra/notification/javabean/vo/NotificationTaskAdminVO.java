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

package com.devops00.spectra.notification.javabean.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 脱敏通知任务管理视图。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Data
public class NotificationTaskAdminVO {

    /**
     * 投递任务 ID。
     */
    private UUID id;
    /**
     * 所属通知请求 ID。
     */
    private UUID requestId;
    /**
     * 锁定的模板版本 ID。
     */
    private UUID templateId;
    /**
     * 锁定的模板业务版本号。
     */
    private Integer templateVersionNo;
    /**
     * 锁定的模板内容摘要。
     */
    private String templateVersionDigest;
    /**
     * 收件用户 ID。
     */
    private UUID recipientUserId;
    /**
     * 已脱敏的收件地址。
     */
    private String recipientAddress;
    /**
     * 投递渠道。
     */
    private String channel;
    /**
     * 通知用途。
     */
    private String purpose;
    /**
     * 任务状态。
     */
    private String status;
    /**
     * 已重试次数。
     */
    private Integer retryCount;
    /**
     * 已脱敏的最近一次错误。
     */
    private String lastError;
    /**
     * 计划处理时间。
     */
    private LocalDateTime scheduledAt;
    /**
     * 任务过期时间。
     */
    private LocalDateTime expiresAt;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;
}
