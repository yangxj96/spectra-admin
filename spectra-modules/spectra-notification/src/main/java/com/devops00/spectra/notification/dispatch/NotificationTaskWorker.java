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

package com.devops00.spectra.notification.dispatch;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.notification.javabean.domain.ChannelSendResult;
import com.devops00.spectra.notification.javabean.domain.ChannelSendStatus;
import com.devops00.spectra.notification.javabean.domain.NotificationTaskStatus;
import com.devops00.spectra.notification.javabean.entity.NotificationDeliveryEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.mapper.NotificationDeliveryMapper;
import com.devops00.spectra.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.notification.observability.NotificationMetrics;
import com.devops00.spectra.notification.sender.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * PostgreSQL Outbox 任务 Worker；支持并发领取、租约恢复和指数退避。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Service
@RequiredArgsConstructor
public class NotificationTaskWorker {

    /**
     * 单个任务允许的最大重试次数。
     */
    private static final int MAX_RETRY_COUNT = 3;
    /**
     * 指数退避的最大等待时间。
     */
    private static final long MAX_RETRY_DELAY_SECONDS = 3_600L;
    /**
     * Worker 锁定标识。
     */
    private static final String DEFAULT_WORKER_ID = "notification-worker";

    /**
     * Provider 适配器允许写入投递摘要的固定编码；未知文本仍使用通用失败摘要，避免敏感内容落库。
     */
    private static final Set<String> SAFE_PROVIDER_SUMMARIES = Set.of(
            "ACCEPTED", "CHANNEL_NOT_CONFIGURED", "MOCK_ACCEPTED", "PROVIDER_ACCEPTED",
            "PROVIDER_CONFIGURATION_INVALID", "PROVIDER_FAILURE", "PROVIDER_HEALTH_CHECK_FAILED",
            "PROVIDER_HTTP_REJECTED", "PROVIDER_NOT_CONFIGURED", "PROVIDER_NOT_REGISTERED",
            "PROVIDER_RATE_LIMITED", "PROVIDER_REJECTED", "PROVIDER_REQUEST_INTERRUPTED",
            "PROVIDER_REQUEST_UNAVAILABLE", "PROVIDER_SERVER_ERROR", "PROVIDER_UNKNOWN_RESULT",
            "RECIPIENT_ADDRESS_UNAVAILABLE", "站内信已写入收件箱");

    /**
     * 通知任务 Mapper。
     */
    private final NotificationTaskMapper taskMapper;
    /**
     * 投递记录 Mapper。
     */
    private final NotificationDeliveryMapper deliveryMapper;
    /**
     * 逻辑请求状态汇总器。
     */
    private final NotificationRequestStatusUpdater requestStatusUpdater;
    /**
     * 已注册的渠道发送端。
     */
    private final List<NotificationSender> senders;

    /**
     * 可选指标门面；测试或精简运行时未注册 MeterRegistry 时保持 Worker 可用。
     */
    private NotificationMetrics metrics;

    /**
     * 更新或推进目标状态（{@code setMetrics}）。
     */
    @Autowired(required = false)
    public void setMetrics(NotificationMetrics metrics) {
        this.metrics = metrics;
    }

    /**
     * 任务处于 {@code PROCESSING} 状态的最长允许时间，单位为秒。超时后任务可由 Worker 重新领取，避免异常中断导致永久卡住。
     */
    @Value("${spectra.notification.worker.processing-timeout-seconds:300}")
    private long processingTimeoutSeconds;

    /**
     * 通知投递失败后首次重试的基础延迟，单位为秒；后续重试在此基础上按尝试次数执行指数退避。
     */
    @Value("${spectra.notification.worker.retry-base-delay-seconds:5}")
    private long retryBaseDelaySeconds;

    /**
     * 周期领取到期任务；停用 Worker 不影响消息中心读取。
     */
    /**
     * 供测试和运维手工触发的任务处理入口。
     */
    public int processPending(int limit) {
        return processPending(limit, DEFAULT_WORKER_ID, true);
    }

    /**
     * 由统一 LOOP 调度器调用；实例身份来自调度上下文，避免多实例共享锁持有者。
     */
    public int processPending(int limit, String workerId, boolean recoverLeases) {
        var safeLimit = Math.max(1, Math.min(limit, 100));
        var now = Instant.now();
        if (recoverLeases) {
            recoverExpiredLeases(now);
        }
        var tasks = taskMapper.selectPendingTasks(now, safeLimit);
        tasks.forEach(task -> processOne(task, workerId));
        return tasks.size();
    }

    /**
     * 将超时的处理中任务恢复为可重试状态。
     */
    private void recoverExpiredLeases(Instant now) {
        var leaseDeadline = now.minusSeconds(Math.max(1L, processingTimeoutSeconds));
        taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getStatus, NotificationTaskStatus.PROCESSING.name())
                .lt(NotificationTaskEntity::getLockedAt, leaseDeadline)
                .set(NotificationTaskEntity::getStatus, NotificationTaskStatus.RETRYING.name())
                .set(NotificationTaskEntity::getScheduledAt, now)
                .set(NotificationTaskEntity::getNextRetryAt, now)
                .set(NotificationTaskEntity::getLastErrorCode, "WORKER_LEASE_EXPIRED")
                .set(NotificationTaskEntity::getLockedBy, null)
                .set(NotificationTaskEntity::getLockedAt, null));
    }

    /**
     * 领取并处理单个通知任务。
     */
    private void processOne(NotificationTaskEntity task, String workerId) {
        var now = Instant.now();
        if (isExpired(task, now)) {
            markExpired(task, now);
            requestStatusUpdater.refresh(task.getNotificationRequestId());
            return;
        }
        var claimed = taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getId, task.getId())
                .in(NotificationTaskEntity::getStatus,
                        List.of(NotificationTaskStatus.PENDING.name(), NotificationTaskStatus.RETRYING.name()))
                .le(NotificationTaskEntity::getScheduledAt, now)
                .set(NotificationTaskEntity::getStatus, NotificationTaskStatus.PROCESSING.name())
                .set(NotificationTaskEntity::getLockedBy, workerId)
                .set(NotificationTaskEntity::getLockedAt, now));
        if (claimed != 1) {
            return;
        }
        var sender = senders.stream()
                .filter(item -> item.channel().name().equals(task.getChannel()))
                .findFirst()
                .orElse(null);
        if (sender == null) {
            finish(task, ChannelSendResult.blocked("NONE", null, "CHANNEL_NOT_CONFIGURED"));
            requestStatusUpdater.refresh(task.getNotificationRequestId());
            return;
        }
        try {
            var result = sender.send(task);
            finish(task, result);
            if (metrics != null) {
                metrics.recordTask(task.getChannel(), result.status().name(), task.getPurpose());
            }
        } catch (RuntimeException exception) {
            finish(task, ChannelSendResult.unknown("NONE", null, "PROVIDER_FAILURE"));
            if (metrics != null) {
                metrics.recordTask(task.getChannel(), NotificationTaskStatus.UNKNOWN.name(), task.getPurpose());
            }
        }
        requestStatusUpdater.refresh(task.getNotificationRequestId());
    }

    /**
     * 记录渠道结果；只有明确的限流结果允许按任务最大尝试次数退避重试。
     */
    private void finish(NotificationTaskEntity task, ChannelSendResult result) {
        var delivery = new NotificationDeliveryEntity();
        var completedAt = Instant.now();
        var attemptCount = (task.getAttemptCount() == null ? 0 : task.getAttemptCount()) + 1;
        delivery.setNotificationTaskId(task.getId());
        delivery.setTemplateId(task.getTemplateId());
        delivery.setTemplateVersionNo(task.getTemplateVersionNo());
        delivery.setTemplateVersionDigest(task.getTemplateVersionDigest());
        delivery.setRenderedTitle(task.getTitle());
        delivery.setRenderedContent(task.getContent());
        delivery.setAttemptNo(attemptCount);
        delivery.setProvider(result.providerCode());
        delivery.setProviderMessageId(result.providerMessageId());
        delivery.setStartedAt(completedAt);
        delivery.setCompletedAt(completedAt);
        delivery.setResultStatus(result.status().name());
        var safeSummary = sanitizeProviderSummary(result.summary());
        delivery.setErrorCode(result.status() == ChannelSendStatus.SENT ? null : safeSummary);
        delivery.setErrorMessageSanitized(result.status() == ChannelSendStatus.SENT ? null : safeSummary);
        delivery.setResponseSummary(safeSummary == null ? Map.of() : Map.of("summary", safeSummary));
        if (deliveryMapper.insert(delivery) != 1) {
            throw new DataSaveException("记录通知投递结果失败");
        }
        var retryable = isRetryable(result, attemptCount, task.getMaxAttempts());
        var nextAttempt = Instant.now().plusSeconds(retryDelaySeconds(attemptCount));
        taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getId, task.getId())
                .eq(NotificationTaskEntity::getStatus, NotificationTaskStatus.PROCESSING.name())
                .set(NotificationTaskEntity::getAttemptCount, attemptCount)
                .set(NotificationTaskEntity::getLastErrorCode, result.status() == ChannelSendStatus.SENT ? null : safeSummary)
                .set(NotificationTaskEntity::getStatus,
                        retryable ? NotificationTaskStatus.RETRYING.name() : result.status().name())
                .set(NotificationTaskEntity::getScheduledAt, retryable ? nextAttempt : task.getScheduledAt())
                .set(NotificationTaskEntity::getNextRetryAt, retryable ? nextAttempt : task.getNextRetryAt())
                .set(NotificationTaskEntity::getLockedBy, null)
                .set(NotificationTaskEntity::getLockedAt, null));
    }

    /**
     * 仅对供应商明确返回的限流结果执行自动重试；UNKNOWN 不做无条件重试。
     */
    private boolean isRetryable(ChannelSendResult result, int attemptCount, Integer maxAttempts) {
        var allowedAttempts = maxAttempts == null || maxAttempts < 1 ? MAX_RETRY_COUNT : maxAttempts;
        return result.status() == ChannelSendStatus.FAILED
                && "PROVIDER_RATE_LIMITED".equals(result.summary())
                && attemptCount < allowedAttempts;
    }

    /**
     * 仅保留可用于重试分类的脱敏错误摘要，避免 Provider 异常携带验证码或其他敏感值落库。
     */
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

    /**
     * 根据重试次数计算指数退避时间。
     */
    private long retryDelaySeconds(int retryCount) {
        var base = Math.max(1L, retryBaseDelaySeconds);
        var multiplier = 1L << Math.min(Math.max(0, retryCount - 1), 10);
        return Math.min(MAX_RETRY_DELAY_SECONDS, base * multiplier);
    }

    /**
     * 判断任务是否已超过过期时间。
     */
    private boolean isExpired(NotificationTaskEntity task, Instant now) {
        return task.getExpiresAt() != null && !task.getExpiresAt().isAfter(now);
    }

    /**
     * 将尚未领取的过期任务标记为过期。
     */
    private void markExpired(NotificationTaskEntity task, Instant now) {
        taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getId, task.getId())
                .in(NotificationTaskEntity::getStatus,
                        List.of(NotificationTaskStatus.PENDING.name(), NotificationTaskStatus.RETRYING.name()))
                .set(NotificationTaskEntity::getStatus, NotificationTaskStatus.EXPIRED.name())
                .set(NotificationTaskEntity::getLockedBy, null)
                .set(NotificationTaskEntity::getLockedAt, null));
    }

}
