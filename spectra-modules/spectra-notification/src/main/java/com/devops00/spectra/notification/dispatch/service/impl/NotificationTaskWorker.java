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

package com.devops00.spectra.notification.dispatch.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.notification.dispatch.javabean.bo.ChannelSendResult;
import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationDeliveryEntity;
import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.dispatch.mapper.NotificationDeliveryMapper;
import com.devops00.spectra.notification.dispatch.mapper.NotificationTaskMapper;
import com.devops00.spectra.notification.dispatch.service.NotificationSender;
import com.devops00.spectra.notification.request.javabean.entity.NotificationRequestEntity;
import com.devops00.spectra.notification.request.mapper.NotificationRequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** PostgreSQL Outbox 任务 Worker；支持并发领取、租约恢复和指数退避。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTaskWorker {

    private static final int MAX_RETRY_COUNT = 3;
    private static final long MAX_RETRY_DELAY_SECONDS = 3_600L;

    private final NotificationTaskMapper taskMapper;
    private final NotificationDeliveryMapper deliveryMapper;
    private final NotificationRequestMapper requestMapper;
    private final List<NotificationSender> senders;

    @Value("${spectra.notification.worker.processing-timeout-seconds:300}")
    private long processingTimeoutSeconds;

    @Value("${spectra.notification.worker.retry-base-delay-seconds:5}")
    private long retryBaseDelaySeconds;

    /** 周期领取到期任务；停用 Worker 不影响消息中心读取。 */
    @Scheduled(fixedDelayString = "${spectra.notification.worker.fixed-delay-ms:5000}")
    public void scheduledProcess() {
        processPending(50);
    }

    /** 供测试和运维手工触发的任务处理入口。 */
    @Transactional
    public int processPending(int limit) {
        var safeLimit = Math.max(1, Math.min(limit, 100));
        var now = Instant.now();
        recoverExpiredLeases(now);
        var tasks = taskMapper.selectList(new LambdaQueryWrapper<NotificationTaskEntity>()
                .in(NotificationTaskEntity::getStatus, List.of("PENDING", "RETRYING"))
                .le(NotificationTaskEntity::getScheduledAt, now)
                .and(wrapper -> wrapper.isNull(NotificationTaskEntity::getExpiresAt)
                        .or()
                        .gt(NotificationTaskEntity::getExpiresAt, now))
                .orderByAsc(NotificationTaskEntity::getScheduledAt)
                .last("FOR UPDATE SKIP LOCKED LIMIT " + safeLimit));
        tasks.forEach(this::processOne);
        return tasks.size();
    }

    private void recoverExpiredLeases(Instant now) {
        var leaseDeadline = now.minusSeconds(Math.max(1L, processingTimeoutSeconds));
        taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getStatus, "PROCESSING")
                .lt(NotificationTaskEntity::getUpdatedAt, leaseDeadline)
                .set(NotificationTaskEntity::getStatus, "RETRYING")
                .set(NotificationTaskEntity::getScheduledAt, now)
                .set(NotificationTaskEntity::getLastError, "Worker 租约超时，任务已恢复")
                .set(NotificationTaskEntity::getUpdatedAt, now));
    }

    private void processOne(NotificationTaskEntity task) {
        var now = Instant.now();
        if (isExpired(task, now)) {
            markExpired(task, now);
            refreshRequestStatus(task.getRequestId());
            return;
        }
        var claimed = taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getId, task.getId())
                .in(NotificationTaskEntity::getStatus, List.of("PENDING", "RETRYING"))
                .le(NotificationTaskEntity::getScheduledAt, now)
                .set(NotificationTaskEntity::getStatus, "PROCESSING")
                .set(NotificationTaskEntity::getUpdatedAt, now));
        if (claimed != 1) {
            return;
        }
        var sender = senders.stream()
                .filter(item -> item.channel().name().equals(task.getChannel()))
                .findFirst()
                .orElse(null);
        if (sender == null) {
            finish(task, new ChannelSendResult("BLOCKED", "NONE", null, "CHANNEL_NOT_CONFIGURED"));
            refreshRequestStatus(task.getRequestId());
            return;
        }
        try {
            finish(task, sender.send(task));
        } catch (RuntimeException exception) {
            var message = exception.getMessage() == null ? "通知渠道调用失败" : exception.getMessage();
            finishFailure(task, message.length() > 1000 ? message.substring(0, 1000) : message);
        }
        refreshRequestStatus(task.getRequestId());
    }

    private void finish(NotificationTaskEntity task, ChannelSendResult result) {
        var delivery = new NotificationDeliveryEntity();
        delivery.setId(UUID.randomUUID());
        delivery.setTenantId(task.getTenantId());
        delivery.setTaskId(task.getId());
        delivery.setProviderCode(result.providerCode());
        delivery.setProviderMessageId(result.providerMessageId());
        delivery.setStatus(result.status());
        delivery.setResponseSummary(result.summary());
        delivery.setSentAt("SENT".equals(result.status()) ? Instant.now() : null);
        delivery.setCreatedAt(Instant.now());
        if (deliveryMapper.insert(delivery) != 1) {
            throw new DataSaveException("记录通知投递结果失败");
        }
        taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getId, task.getId())
                .eq(NotificationTaskEntity::getStatus, "PROCESSING")
                .set(NotificationTaskEntity::getStatus, result.status())
                .set(NotificationTaskEntity::getUpdatedAt, Instant.now()));
    }

    private void finishFailure(NotificationTaskEntity task, String message) {
        var retryCount = task.getRetryCount() == null ? 1 : task.getRetryCount() + 1;
        var terminal = retryCount >= MAX_RETRY_COUNT;
        var status = terminal ? "FAILED" : "RETRYING";
        var nextAttempt = Instant.now().plusSeconds(retryDelaySeconds(retryCount));
        taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getId, task.getId())
                .eq(NotificationTaskEntity::getStatus, "PROCESSING")
                .set(NotificationTaskEntity::getRetryCount, retryCount)
                .set(NotificationTaskEntity::getLastError, message)
                .set(NotificationTaskEntity::getStatus, status)
                .set(NotificationTaskEntity::getScheduledAt, nextAttempt)
                .set(NotificationTaskEntity::getUpdatedAt, Instant.now()));
        log.warn("通知任务处理失败: taskId={}, retryCount={}, status={}", task.getId(), retryCount, status);
    }

    private long retryDelaySeconds(int retryCount) {
        var base = Math.max(1L, retryBaseDelaySeconds);
        var multiplier = 1L << Math.min(Math.max(0, retryCount - 1), 10);
        return Math.min(MAX_RETRY_DELAY_SECONDS, base * multiplier);
    }

    private boolean isExpired(NotificationTaskEntity task, Instant now) {
        return task.getExpiresAt() != null && !task.getExpiresAt().isAfter(now);
    }

    private void markExpired(NotificationTaskEntity task, Instant now) {
        taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getId, task.getId())
                .in(NotificationTaskEntity::getStatus, List.of("PENDING", "RETRYING"))
                .set(NotificationTaskEntity::getStatus, "EXPIRED")
                .set(NotificationTaskEntity::getUpdatedAt, now));
    }

    private void refreshRequestStatus(UUID requestId) {
        if (requestId == null) {
            return;
        }
        var tasks = taskMapper.selectList(new LambdaQueryWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getRequestId, requestId));
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
