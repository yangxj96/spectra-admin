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

import java.util.List;
import java.util.UUID;

/**
 * 同步通知发送回执，逐目标记录本次发送结果。
 *
 * @param results 每个收件人×渠道的发送结果
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
public record NotificationReceipt(List<SendResult> results) {

    /**
     * 归一化结果集合，避免调用方修改发送回执。
     */
    public NotificationReceipt {
        results = results == null ? List.of() : List.copyOf(results);
    }

    /**
     * 统计发送成功的目标数量。
     *
     * @return 成功数量
     */
    public long successCount() {
        return results.stream().filter(SendResult::success).count();
    }

    /**
     * 统计发送失败的目标数量。
     *
     * @return 失败数量
     */
    public long failureCount() {
        return results.size() - successCount();
    }

    /**
     * 判断是否存在失败目标。
     *
     * @return 存在失败时返回 {@code true}
     */
    public boolean failed() {
        return failureCount() > 0;
    }

    /**
     * 单个目标的同步发送结果。
     *
     * @param channel       通知渠道
     * @param recipientUser 收件用户 ID；直接地址场景为空
     * @param address       实际收件地址；站内信场景为空
     * @param messageId     发送历史主键
     * @param success       是否成功
     * @param errorCode     失败码；成功时为空
     * @param errorMessage  失败信息；成功时为空
     */
    public record SendResult(NotificationChannel channel, UUID recipientUser, String address, UUID messageId,
                             boolean success, String errorCode, String errorMessage) {
    }
}
