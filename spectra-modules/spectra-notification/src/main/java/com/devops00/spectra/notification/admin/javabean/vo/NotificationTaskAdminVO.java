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

package com.devops00.spectra.notification.admin.javabean.vo;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

/** 脱敏通知任务管理视图。 */
@Data
public class NotificationTaskAdminVO {

    private UUID id;
    private UUID requestId;
    private UUID recipientUserId;
    private String recipientAddress;
    private String channel;
    private String purpose;
    private String status;
    private Integer retryCount;
    private String lastError;
    private LocalDateTime scheduledAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
