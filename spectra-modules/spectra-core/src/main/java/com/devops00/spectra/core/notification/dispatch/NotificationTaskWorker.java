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
import com.devops00.spectra.common.audit.RequestCorrelationContext;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.core.notification.javabean.domain.ChannelSendResult;
import com.devops00.spectra.core.notification.javabean.domain.NotificationTaskStatus;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.core.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.core.notification.observability.NotificationMetrics;
import com.devops00.spectra.core.notification.sender.NotificationSenderRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
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
     * Worker 锁定标识。
     */
    private static final String DEFAULT_WORKER_ID = "notification-worker";

    /**
     * 通知任务 Mapper。
     */
    private final NotificationTaskMapper taskMapper;
    /**
     * 通知结果记录器；Delivery 插入和 Task 迁移在同一事务内完成。
     */
    private final NotificationTaskResultRecorder resultRecorder;
    /**
     * 逻辑请求状态汇总器。
     */
    private final NotificationRequestStatusUpdater requestStatusUpdater;
    /**
     * 已注册的渠道发送端。
     */
    private final NotificationSenderRegistry senderRegistry;

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
     * 处理一批待投递任务，供周期调度器、测试和运维手工触发调用。
     * Worker 停用时不会影响消息中心读取。
     *
     * @param limit 本次最多处理的任务数
     * @return 本次处理的任务数
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
        refreshRequests(taskMapper.expireOverdueTasks(now, safeLimit));
        if (recoverLeases) {
            recoverExpiredLeases(now);
        }
        var tasks = taskMapper.selectPendingTasks(now, safeLimit);
        tasks.forEach(task -> processOne(task, workerId));
        return tasks.size();
    }

    /**
     * 将租约超时任务收敛为 UNKNOWN；外部发送结果未知时不允许自动重发。
     */
    private void recoverExpiredLeases(Instant now) {
        var leaseDeadline = now.minusSeconds(Math.max(1L, processingTimeoutSeconds));
        refreshRequests(taskMapper.expireLeases(leaseDeadline, 100));
    }

    private void refreshRequests(List<UUID> requestIds) {
        if (requestIds == null || requestIds.isEmpty()) {
            return;
        }
        requestIds.stream().distinct().forEach(requestStatusUpdater::refresh);
    }

    /**
     * 领取并处理单个通知任务。
     */
    private void processOne(NotificationTaskEntity task, String workerId) {
        RequestCorrelationContext.runWithMdc(
                RequestCorrelationContext.forTask(task.getId() == null ? null : task.getId().toString()),
                () -> processOneWithContext(task, workerId));
    }

    /**
     * 处理上下文相关数据。
     */
    private void processOneWithContext(NotificationTaskEntity task, String workerId) {
        var now = Instant.now();
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
        task.setStatus(NotificationTaskStatus.PROCESSING.name());
        task.setLockedBy(workerId);
        task.setLockedAt(now);

        var sender = senderRegistry.find(parseChannel(task.getChannel())).orElse(null);
        ChannelSendResult result;
        if (sender == null) {
            result = ChannelSendResult.blocked("NONE", null, "CHANNEL_NOT_CONFIGURED");
        } else {
            try {
                result = sender.send(task);
            } catch (RuntimeException exception) {
                log.warn("通知 Provider 发送异常，结果按 UNKNOWN 记录: taskId={}", task.getId(), exception);
                result = ChannelSendResult.unknown("NONE", null, "PROVIDER_UNKNOWN_RESULT");
            }
        }
        try {
            resultRecorder.record(task, result);
            if (metrics != null) {
                metrics.recordTask(task.getChannel(), result.status().name(), task.getPurpose());
            }
            requestStatusUpdater.refresh(task.getNotificationRequestId());
        } catch (RuntimeException exception) {
            log.error("通知结果持久化失败，任务保留租约等待收敛: taskId={}", task.getId(), exception);
        }
    }

    /**
     * 将任务中保存的渠道编码解析为枚举；非法编码返回 null，由 Registry 统一视为未注册渠道。
     *
     * @param value 任务中保存的渠道编码；可以为空
     * @return 对应的通知渠道；为空或非法时返回 null
     */
    private static NotificationChannel parseChannel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return NotificationChannel.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

}
