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

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

/**
 * 脱敏通知投递记录管理视图。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Data
public class NotificationDeliveryAdminVO {

    /** 投递记录 ID。 */
    private UUID id;
    /** 所属投递任务 ID。 */
    private UUID taskId;
    /** 渠道供应商编码。 */
    private String providerCode;
    /** 供应商返回的消息 ID。 */
    private String providerMessageId;
    /** 投递结果状态。 */
    private String status;
    /** 已脱敏的供应商响应摘要。 */
    private String responseSummary;
    /** 成功发送时间。 */
    private LocalDateTime sentAt;
    /** 创建时间。 */
    private LocalDateTime createdAt;
}
