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
import java.util.Map;
import java.util.UUID;

/**
 * 脱敏通知请求管理视图。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Data
public class NotificationRequestAdminVO {

    /**
     * 通知请求 ID。
     */
    private UUID id;
    /**
     * 业务类型。
     */
    private String businessType;
    /**
     * 业务 ID。
     */
    private String businessId;
    /**
     * 模板编码。
     */
    private String templateCode;
    /**
     * 各渠道实际使用的模板版本摘要元数据。
     */
    private Map<String, Object> templateSnapshot;
    /**
     * 通知用途。
     */
    private String purpose;
    /**
     * 来源模块。
     */
    private String sourceModule;
    /**
     * 请求状态。
     */
    private String status;
    /**
     * 计划投递时间。
     */
    private LocalDateTime scheduledAt;
    /**
     * 过期时间。
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
    /**
     * 优先级。
     */
    private Integer priority;
}
