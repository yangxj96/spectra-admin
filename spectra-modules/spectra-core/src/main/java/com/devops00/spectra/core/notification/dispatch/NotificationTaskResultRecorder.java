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

package com.devops00.spectra.core.notification.dispatch;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.core.notification.javabean.domain.ChannelSendResult;
import com.devops00.spectra.core.notification.javabean.domain.ChannelSendStatus;
import com.devops00.spectra.core.notification.javabean.domain.NotificationTaskStatus;
import com.devops00.spectra.core.notification.javabean.entity.NotificationDeliveryEntity;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.core.notification.mapper.NotificationDeliveryMapper;
import com.devops00.spectra.core.notification.mapper.NotificationTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 以任务租约为 fence 原子记录一次投递结果。
 *
 * <p>Delivery 插入和 Task 状态迁移必须处于同一事务；旧 Worker 在新 Worker 领取后不能写入结果。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/28
 */
@Component
@RequiredArgsConstructor
public class NotificationTaskResultRecorder {

    private static final int MAX_RETRY_COUNT = 3;
    private static final long MAX_RETRY_DELAY_SECONDS = 3_600L;

    private static final Set<String> SAFE_PROVIDER_SUMMARIES = Set.of(
            "ACCEPTED", "CHANNEL_NOT_CONFIGURED", "MOCK_ACCEPTED", "PROVIDER_ACCEPTED",
            "PROVIDER_CONFIGURATION_INVALID", "PROVIDER_FAILURE", "PROVIDER_HEALTH_CHECK_FAILED",
            "PROVIDER_HTTP_REJECTED", "PROVIDER_NOT_CONFIGURED", "PROVIDER_NOT_REGISTERED",
            "PROVIDER_RATE_LIMITED", "PROVIDER_REJECTED", "PROVIDER_REQUEST_INTERRUPTED",
            "PROVIDER_REQUEST_UNAVAILABLE", "PROVIDER_SERVER_ERROR", "PROVIDER_UNKNOWN_RESULT",
            "RECIPIENT_ADDRESS_UNAVAILABLE", "站内信已写入收件箱");

    private final NotificationTaskMapper taskMapper;
    private final NotificationDeliveryMapper deliveryMapper;

    @Value("${spectra.notification.worker.retry-base-delay-seconds:5}")
    private long retryBaseDelaySeconds;

    /**
     * 记录投递结果并迁移任务状态。
     *
     * @param task   已领取任务快照，必须包含本次 claim 的 lockedBy/lockedAt
     * @param result 渠道发送结果
     */
    @Transactional
    public void record(NotificationTaskEntity task, ChannelSendResult result) {
        if (task.getLockedBy() == null || task.getLockedAt() == null) {
            throw new DataSaveException("通知任务缺少有效租约");
        }
        var claimAttempt = task.getAttemptCount() == null ? 0 : task.getAttemptCount();
        var attemptCount = claimAttempt + 1;
        var completedAt = Instant.now();
        var safeSummary = sanitizeProviderSummary(result.summary());
        deliveryMapper.insert(buildDelivery(task, result, completedAt, attemptCount, safeSummary));
        int updated = taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getId, task.getId())
                .eq(NotificationTaskEntity::getStatus, NotificationTaskStatus.PROCESSING.name())
                .eq(NotificationTaskEntity::getLockedBy, task.getLockedBy())
                .eq(NotificationTaskEntity::getLockedAt, task.getLockedAt())
                .eq(NotificationTaskEntity::getAttemptCount, claimAttempt)
                .set(NotificationTaskEntity::getAttemptCount, attemptCount)
                .set(NotificationTaskEntity::getLastErrorCode,
                        result.status() == ChannelSendStatus.SENT ? null : safeSummary)
                .set(NotificationTaskEntity::getStatus,
                        retryable(result, attemptCount, task.getMaxAttempts())
                                ? NotificationTaskStatus.RETRYING.name()
                                : result.status().name())
                .set(NotificationTaskEntity::getScheduledAt,
                        retryable(result, attemptCount, task.getMaxAttempts())
                                ? completedAt.plusSeconds(retryDelaySeconds(attemptCount))
                                : task.getScheduledAt())
                .set(NotificationTaskEntity::getNextRetryAt,
                        retryable(result, attemptCount, task.getMaxAttempts())
                                ? completedAt.plusSeconds(retryDelaySeconds(attemptCount))
                                : null)
                .set(NotificationTaskEntity::getLockedBy, null)
                .set(NotificationTaskEntity::getLockedAt, null));
        if (updated != 1) {
            throw new DataSaveException("通知任务租约已失效，拒绝写入旧结果");
        }
    }

    private NotificationDeliveryEntity buildDelivery(NotificationTaskEntity task, ChannelSendResult result,
                                                     Instant completedAt, int attemptCount, String safeSummary) {
        var delivery = new NotificationDeliveryEntity();
        delivery.setNotificationTaskId(task.getId());
        delivery.setTemplateId(task.getTemplateId());
        delivery.setTemplateVersionNo(task.getTemplateVersionNo());
        delivery.setTemplateVersionDigest(task.getTemplateVersionDigest());
        delivery.setRenderedTitle(task.getTitle());
        delivery.setRenderedContent(task.getContent());
        delivery.setAttemptNo(attemptCount);
        delivery.setProvider(result.providerCode());
        delivery.setProviderMessageId(result.providerMessageId());
        delivery.setStartedAt(task.getLockedAt());
        delivery.setCompletedAt(completedAt);
        delivery.setDurationMs(task.getLockedAt() == null
                ? null
                : Math.max(0L, Duration.between(task.getLockedAt(), completedAt).toMillis()));
        delivery.setResultStatus(result.status().name());
        delivery.setErrorCode(result.status() == ChannelSendStatus.SENT ? null : safeSummary);
        delivery.setErrorMessageSanitized(result.status() == ChannelSendStatus.SENT ? null : safeSummary);
        delivery.setResponseSummary(safeSummary == null ? Map.of() : Map.of("summary", safeSummary));
        return delivery;
    }

    private boolean retryable(ChannelSendResult result, int attemptCount, Integer maxAttempts) {
        var allowedAttempts = maxAttempts == null || maxAttempts < 1 ? MAX_RETRY_COUNT : maxAttempts;
        return result.status() == ChannelSendStatus.FAILED
                && "PROVIDER_RATE_LIMITED".equals(result.summary())
                && attemptCount < allowedAttempts;
    }

    private String sanitizeProviderSummary(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        var safe = value.trim()
                .replaceAll("(?i)(code|captcha|token|secret|password)\\s*[:=]\\s*[^,; ]+", "$1=[REDACTED]")
                .replaceAll("(?<!\\d)\\d{6}(?!\\d)", "[REDACTED]");
        safe = safe.length() > 200 ? safe.substring(0, 200) : safe;
        if (safe.toUpperCase(Locale.ROOT).contains("REDACTED")) {
            return safe;
        }
        return SAFE_PROVIDER_SUMMARIES.contains(safe.toUpperCase(Locale.ROOT))
                || SAFE_PROVIDER_SUMMARIES.contains(safe) ? safe : "PROVIDER_FAILURE";
    }

    private long retryDelaySeconds(int retryCount) {
        var base = Math.max(1L, retryBaseDelaySeconds);
        var multiplier = 1L << Math.min(Math.max(0, retryCount - 1), 10);
        return Math.min(MAX_RETRY_DELAY_SECONDS, base * multiplier);
    }
}
