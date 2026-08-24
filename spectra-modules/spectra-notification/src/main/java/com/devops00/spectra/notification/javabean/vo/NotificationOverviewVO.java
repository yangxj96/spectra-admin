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

import com.devops00.spectra.common.notification.NotificationChannelAvailability;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知运行概览；只返回聚合数据和脱敏错误摘要。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationOverviewVO {

    /** 数据生成时间。 */
    private LocalDateTime generatedAt;
    /** 统计窗口，单位为小时。 */
    private int rangeHours;
    /** 当前待处理任务数：PENDING 和 RETRYING。 */
    private long pendingTaskCount;
    /** 当前正在处理任务数。 */
    private long processingTaskCount;
    /** 最早待处理任务的计划时间。 */
    private LocalDateTime oldestPendingTaskAt;
    /** 当前失败或阻断任务数。 */
    private long failedTaskCount;
    /** 当前 UNKNOWN 任务数。 */
    private long unknownTaskCount;
    /** 窗口内投递尝试总数。 */
    private long deliveryCount;
    /** 窗口内接受或发送成功数。 */
    private long successfulDeliveryCount;
    /** 窗口内失败或阻断数。 */
    private long failedDeliveryCount;
    /** 窗口内 UNKNOWN 结果数。 */
    private long unknownDeliveryCount;
    /** 投递失败率，范围为 0 到 100。 */
    private double failureRate;
    /** 各渠道可用性和任务摘要。 */
    @Builder.Default
    private List<ChannelSummary> channels = List.of();
    /** 按小时聚合的投递趋势。 */
    @Builder.Default
    private List<TrendPoint> trend = List.of();
    /** 最近的脱敏投递错误。 */
    @Builder.Default
    private List<ErrorSummary> recentErrors = List.of();

    /**
     * 渠道运行摘要。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelSummary {

        /** 渠道可用性。 */
        private NotificationChannelAvailability availability;
        /** 当前待处理任务数。 */
        private long pendingTaskCount;
        /** 当前失败或阻断任务数。 */
        private long failedTaskCount;
        /** 当前 UNKNOWN 任务数。 */
        private long unknownTaskCount;
    }

    /**
     * 按小时聚合的投递趋势点。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {

        /** 小时桶起始时间。 */
        private LocalDateTime bucketAt;
        /** 投递尝试数。 */
        private long totalCount;
        /** 接受或发送成功数。 */
        private long successCount;
        /** 失败或阻断数。 */
        private long failedCount;
        /** UNKNOWN 结果数。 */
        private long unknownCount;
    }

    /**
     * 脱敏投递错误摘要。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorSummary {

        /** 错误发生时间。 */
        private LocalDateTime occurredAt;
        /** 通知渠道。 */
        private String channel;
        /** 投递结果状态。 */
        private String status;
        /** 标准化错误码。 */
        private String errorCode;
        /** 脱敏错误信息。 */
        private String message;
    }
}
