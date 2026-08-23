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

package com.devops00.spectra.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.notification.javabean.domain.ChannelSendResult;
import com.devops00.spectra.notification.javabean.entity.NotificationDeliveryEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationRequestEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.mapper.NotificationDeliveryMapper;
import com.devops00.spectra.notification.mapper.NotificationRequestMapper;
import com.devops00.spectra.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.notification.observability.NotificationMetrics;
import com.devops00.spectra.notification.service.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * PostgreSQL Outbox 任务 Worker；支持并发领取、租约恢复和指数退避。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Slf4j
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
    private static final String WORKER_ID = "notification-worker";

    /**
     * 通知任务 Mapper。
     */
    private final NotificationTaskMapper taskMapper;
    /**
     * 投递记录 Mapper。
     */
    private final NotificationDeliveryMapper deliveryMapper;
    /**
     * 通知请求 Mapper。
     */
    private final NotificationRequestMapper requestMapper;
    /**
     * 已注册的渠道发送端。
     */
    private final List<NotificationSender> senders;

    /**
     * 可选指标门面；测试或精简运行时未注册 MeterRegistry 时保持 Worker 可用。
     */
    private NotificationMetrics metrics;

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
    @Scheduled(fixedDelayString = "${spectra.notification.worker.fixed-delay-ms:5000}")
    public void scheduledProcess() {
        processPending(50);
    }

    /**
     * 供测试和运维手工触发的任务处理入口。
     */
    @Transactional
    public int processPending(int limit) {
        var safeLimit = Math.max(1, Math.min(limit, 100));
        var now = Instant.now();
        recoverExpiredLeases(now);
        var tasks = taskMapper.selectPendingTasks(now, safeLimit);
        tasks.forEach(this::processOne);
        return tasks.size();
    }

    /**
     * 将超时的处理中任务恢复为可重试状态。
     */
    private void recoverExpiredLeases(Instant now) {
        var leaseDeadline = now.minusSeconds(Math.max(1L, processingTimeoutSeconds));
        taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getStatus, "PROCESSING")
                .lt(NotificationTaskEntity::getLockedAt, leaseDeadline)
                .set(NotificationTaskEntity::getStatus, "RETRYING")
                .set(NotificationTaskEntity::getScheduledAt, now)
                .set(NotificationTaskEntity::getNextRetryAt, now)
                .set(NotificationTaskEntity::getLastErrorCode, "WORKER_LEASE_EXPIRED")
                .set(NotificationTaskEntity::getLockedBy, null)
                .set(NotificationTaskEntity::getLockedAt, null));
    }

    /**
     * 领取并处理单个通知任务。
     */
    private void processOne(NotificationTaskEntity task) {
        var now = Instant.now();
        if (isExpired(task, now)) {
            markExpired(task, now);
            refreshRequestStatus(task.getNotificationRequestId());
            return;
        }
        var claimed = taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getId, task.getId())
                .in(NotificationTaskEntity::getStatus, List.of("PENDING", "RETRYING"))
                .le(NotificationTaskEntity::getScheduledAt, now)
                .set(NotificationTaskEntity::getStatus, "PROCESSING")
                .set(NotificationTaskEntity::getLockedBy, WORKER_ID)
                .set(NotificationTaskEntity::getLockedAt, now));
        if (claimed != 1) {
            return;
        }
        var sender = senders.stream()
                .filter(item -> item.channel().name().equals(task.getChannel()))
                .findFirst()
                .orElse(null);
        if (sender == null) {
            finish(task, new ChannelSendResult("BLOCKED", "NONE", null, "CHANNEL_NOT_CONFIGURED"));
            refreshRequestStatus(task.getNotificationRequestId());
            return;
        }
        try {
            var result = sender.send(task);
            finish(task, result);
            if (metrics != null) {
                metrics.recordTask(task.getChannel(), result.status(), task.getPurpose());
            }
        } catch (RuntimeException exception) {
            var safeMessage = sanitizeProviderSummary(exception.getMessage());
            finishFailure(task, safeMessage == null ? "PROVIDER_FAILURE" : safeMessage);
            if (metrics != null) {
                metrics.recordRetry(task.getChannel(), "PROVIDER_FAILURE");
            }
        }
        refreshRequestStatus(task.getNotificationRequestId());
    }

    /**
     * 记录渠道结果并将任务更新为对应终态。
     */
    private void finish(NotificationTaskEntity task, ChannelSendResult result) {
        var delivery = new NotificationDeliveryEntity();
        var completedAt = Instant.now();
        delivery.setNotificationTaskId(task.getId());
        delivery.setTemplateId(task.getTemplateId());
        delivery.setTemplateVersionNo(task.getTemplateVersionNo());
        delivery.setTemplateVersionDigest(task.getTemplateVersionDigest());
        delivery.setRenderedTitle(task.getTitle());
        delivery.setRenderedContent(task.getContent());
        delivery.setAttemptNo((task.getAttemptCount() == null ? 0 : task.getAttemptCount()) + 1);
        delivery.setProvider(result.providerCode());
        delivery.setProviderMessageId(result.providerMessageId());
        delivery.setStartedAt(completedAt);
        delivery.setCompletedAt(completedAt);
        delivery.setResultStatus(result.status());
        var safeSummary = sanitizeProviderSummary(result.summary());
        delivery.setErrorCode("SENT".equals(result.status()) ? null : safeSummary);
        delivery.setErrorMessageSanitized(safeSummary);
        delivery.setResponseSummary(safeSummary == null ? Map.of() : Map.of("summary", safeSummary));
        if (deliveryMapper.insert(delivery) != 1) {
            throw new DataSaveException("记录通知投递结果失败");
        }
        taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getId, task.getId())
                .eq(NotificationTaskEntity::getStatus, "PROCESSING")
                .set(NotificationTaskEntity::getStatus, result.status())
                .set(NotificationTaskEntity::getLockedBy, null)
                .set(NotificationTaskEntity::getLockedAt, null));
    }

    /**
     * 记录渠道异常，并按重试次数决定下次处理时间或失败终态。
     */
    private void finishFailure(NotificationTaskEntity task, String message) {
        var attemptCount = task.getAttemptCount() == null ? 1 : task.getAttemptCount() + 1;
        var terminal = attemptCount >= MAX_RETRY_COUNT;
        var status = terminal ? "FAILED" : "RETRYING";
        var nextAttempt = Instant.now().plusSeconds(retryDelaySeconds(attemptCount));
        taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getId, task.getId())
                .eq(NotificationTaskEntity::getStatus, "PROCESSING")
                .set(NotificationTaskEntity::getAttemptCount, attemptCount)
                .set(NotificationTaskEntity::getLastErrorCode, message)
                .set(NotificationTaskEntity::getStatus, status)
                .set(NotificationTaskEntity::getScheduledAt, nextAttempt)
                .set(NotificationTaskEntity::getNextRetryAt, nextAttempt)
                .set(NotificationTaskEntity::getLockedBy, null)
                .set(NotificationTaskEntity::getLockedAt, null));
        log.warn("通知任务处理失败: taskId={}, attemptCount={}, status={}", task.getId(), attemptCount, status);
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
        return safe.toUpperCase(Locale.ROOT).contains("REDACTED") ? safe : "PROVIDER_FAILURE";
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
                .in(NotificationTaskEntity::getStatus, List.of("PENDING", "RETRYING"))
                .set(NotificationTaskEntity::getStatus, "EXPIRED")
                .set(NotificationTaskEntity::getLockedBy, null)
                .set(NotificationTaskEntity::getLockedAt, null));
    }

    /**
     * 根据任务汇总状态刷新逻辑请求状态。
     */
    private void refreshRequestStatus(UUID requestId) {
        if (requestId == null) {
            return;
        }
        var tasks = taskMapper.selectList(new LambdaQueryWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getNotificationRequestId, requestId));
        if (tasks.isEmpty()) {
            return;
        }
        var hasOpen = tasks.stream()
                .anyMatch(task -> List.of("PENDING", "RETRYING", "PROCESSING")
                        .contains(task.getStatus()));
        var sentCount = tasks.stream().filter(task -> "SENT".equals(task.getStatus())).count();
        var terminalCount = tasks.stream()
                .filter(task -> List.of("SENT", "FAILED", "BLOCKED", "UNKNOWN", "EXPIRED", "CANCELLED")
                        .contains(task.getStatus()))
                .count();
        var status = hasOpen
                ? "DISPATCHING"
                : sentCount == tasks.size()
                        ? "SUCCEEDED"
                        : sentCount > 0
                                ? "PARTIAL"
                                : tasks.stream().allMatch(task -> "CANCELLED".equals(task.getStatus()))
                                        ? "CANCELLED"
                                        : tasks.stream().allMatch(task -> "EXPIRED".equals(task.getStatus()))
                                                ? "EXPIRED"
                                                : terminalCount == tasks.size() ? "FAILED" : "DISPATCHING";
        requestMapper.update(null, new LambdaUpdateWrapper<NotificationRequestEntity>()
                .eq(NotificationRequestEntity::getId, requestId)
                .notIn(NotificationRequestEntity::getStatus, List.of("CANCELLED", "EXPIRED"))
                .set(NotificationRequestEntity::getStatus, status)
                .set(NotificationRequestEntity::getUpdatedAt, Instant.now()));
    }
}
