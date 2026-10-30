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

package com.devops00.spectra.core.notification.javabean.from;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.core.notification.javabean.enums.NotificationMessageStatus;
import lombok.Data;

/**
 * 通知发送历史分页查询参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Data
public class NotificationHistoryQueryFrom {

    /**
     * 发送渠道。
     */
    private NotificationChannel channel;
    /**
     * 发送结果状态。
     */
    private NotificationMessageStatus status;
    /**
     * 模板编码、标题或收件地址关键字。
     */
    private String keyword;
    /**
     * 创建时间起点。
     */
    private String startTime;
    /**
     * 创建时间终点。
     */
    private String endTime;
}
