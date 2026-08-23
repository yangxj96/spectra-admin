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

import java.time.Instant;

/**
 * 通知运行概览中的脱敏错误摘要。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Data
public class NotificationOverviewErrorVO {

    /** 错误发生时间。 */
    private Instant occurredAt;
    /** 通知渠道。 */
    private String channel;
    /** 投递结果状态。 */
    private String status;
    /** 标准化错误码。 */
    private String errorCode;
    /** 脱敏错误信息。 */
    private String message;
}
