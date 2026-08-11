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
import com.devops00.spectra.notification.dispatch.javabean.bo.ChannelSendResult;
import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationDeliveryEntity;
import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.dispatch.mapper.NotificationDeliveryMapper;
import com.devops00.spectra.notification.dispatch.mapper.NotificationTaskMapper;
import com.devops00.spectra.notification.dispatch.service.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** PostgreSQL Outbox 任务 Worker；通过 CAS 避免多个 Worker 同时处理同一任务。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTaskWorker {

    private static final int MAX_RETRY_COUNT = 3;

    private final NotificationTaskMapper taskMapper;
    private final NotificationDeliveryMapper deliveryMapper;
    private final List<NotificationSender> senders;

    /** 周期领取到期任务；停用 Worker 不影响消息中心读取。 */
    @Scheduled(fixedDelayString = "${spectra.notification.worker.fixed-delay-ms:5000}")
    public void scheduledProcess() {
        processPending(50);
    }

    /** 供测试和运维手工触发的任务处理入口。 */
    public int processPending(int limit) {
        var safeLimit = Math.max(1, Math.min(limit, 100));
        var tasks = taskMapper.selectList(new LambdaQueryWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getStatus, "PENDING")
                .le(NotificationTaskEntity::getScheduledAt, Instant.now())
                .orderByAsc(NotificationTaskEntity::getScheduledAt)
                .last("LIMIT " + safeLimit));
        tasks.forEach(this::processOne);
        return tasks.size();
    }

    @Transactional
    protected void processOne(NotificationTaskEntity task) {
        var now = Instant.now();
        var claimed = taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getId, task.getId())
                .eq(NotificationTaskEntity::getStatus, "PENDING")
                .set(NotificationTaskEntity::getStatus, "PROCESSING")
                .set(NotificationTaskEntity::getUpdatedAt, now));
        if (claimed != 1) {
            return;
        }
        var sender = senders.stream().filter(item -> item.channel().name().equals(task.getChannel())).findFirst().orElse(null);
        if (sender == null) {
            finish(task, new ChannelSendResult("BLOCKED", "NONE", null, "CHANNEL_NOT_CONFIGURED"));
            return;
        }
        try {
            finish(task, sender.send(task));
        } catch (RuntimeException exception) {
            var message = exception.getMessage() == null ? "通知渠道调用失败" : exception.getMessage();
            finishFailure(task, message.length() > 1000 ? message.substring(0, 1000) : message);
        }
    }

    private void finish(NotificationTaskEntity task, ChannelSendResult result) {
        var delivery = new NotificationDeliveryEntity();
        delivery.setId(UUID.randomUUID());
        delivery.setTaskId(task.getId());
        delivery.setProviderCode(result.providerCode());
        delivery.setProviderMessageId(result.providerMessageId());
        delivery.setStatus(result.status());
        delivery.setResponseSummary(result.summary());
        delivery.setSentAt("SENT".equals(result.status()) ? Instant.now() : null);
        delivery.setCreatedAt(Instant.now());
        deliveryMapper.insert(delivery);
        taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>().eq(NotificationTaskEntity::getId, task.getId())
                .eq(NotificationTaskEntity::getStatus, "PROCESSING")
                .set(NotificationTaskEntity::getStatus, result.status())
                .set(NotificationTaskEntity::getUpdatedAt, Instant.now()));
    }

    private void finishFailure(NotificationTaskEntity task, String message) {
        var retryCount = task.getRetryCount() == null ? 1 : task.getRetryCount() + 1;
        var status = retryCount >= MAX_RETRY_COUNT ? "FAILED" : "PENDING";
        taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>().eq(NotificationTaskEntity::getId, task.getId())
                .eq(NotificationTaskEntity::getStatus, "PROCESSING")
                .set(NotificationTaskEntity::getRetryCount, retryCount)
                .set(NotificationTaskEntity::getLastError, message)
                .set(NotificationTaskEntity::getStatus, status)
                .set(NotificationTaskEntity::getUpdatedAt, Instant.now()));
        log.warn("通知任务处理失败: taskId={}, retryCount={}, status={}", task.getId(), retryCount, status);
    }
}
