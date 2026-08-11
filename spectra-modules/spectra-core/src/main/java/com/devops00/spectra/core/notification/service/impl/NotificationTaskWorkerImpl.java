package com.devops00.spectra.core.notification.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.core.notification.constant.NotificationChannel;
import com.devops00.spectra.core.notification.javabean.entity.NotificationDelivery;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTask;
import com.devops00.spectra.core.notification.mapper.NotificationDeliveryMapper;
import com.devops00.spectra.core.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.core.notification.service.NotificationGateway;
import com.devops00.spectra.core.notification.service.NotificationTaskWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 待发送通知任务处理器实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTaskWorkerImpl implements NotificationTaskWorker {

    private static final int MAX_RETRY_COUNT = 3;

    private final NotificationTaskMapper notificationTaskMapper;
    private final NotificationDeliveryMapper notificationDeliveryMapper;
    private final List<NotificationGateway> notificationGateways;

    @Override
    public int processPending(int limit) {
        var safeLimit = Math.max(1, Math.min(limit, 100));
        var query = new LambdaQueryWrapper<NotificationTask>().eq(NotificationTask::getStatus, "PENDING")
                .le(NotificationTask::getScheduledAt, Instant.now()).orderByAsc(NotificationTask::getScheduledAt).last("LIMIT " + safeLimit);
        var tasks = notificationTaskMapper.selectList(query);
        tasks.forEach(this::processOne);
        return tasks.size();
    }

    @Transactional
    protected void processOne(NotificationTask task) {
        task.setStatus("PROCESSING");
        task.setUpdatedAt(Instant.now());
        if (notificationTaskMapper.updateById(task) != 1) {
            return;
        }
        var channel = NotificationChannel.valueOf(task.getChannel());
        if (channel == NotificationChannel.INBOX) {
            markInternalSent(task);
            return;
        }
        var gateway = notificationGateways.stream().filter(item -> item.supports(channel)).findFirst().orElse(null);
        if (gateway == null) {
            fail(task, "未配置通知渠道 Provider");
            return;
        }
        try {
            var messageId = gateway.send(channel, task.getRecipientAddress(), task.getTitle(), task.getContent());
            var delivery = new NotificationDelivery();
            delivery.setId(UUID.randomUUID());
            delivery.setTaskId(task.getId());
            delivery.setProviderCode(gateway.getClass().getSimpleName());
            delivery.setProviderMessageId(messageId);
            delivery.setStatus("SENT");
            delivery.setSentAt(Instant.now());
            delivery.setCreatedAt(Instant.now());
            notificationDeliveryMapper.insert(delivery);
            task.setStatus("SENT");
            task.setUpdatedAt(Instant.now());
            notificationTaskMapper.updateById(task);
        } catch (RuntimeException exception) {
            fail(task, safeMessage(exception));
        }
    }

    private void markInternalSent(NotificationTask task) {
        var delivery = new NotificationDelivery();
        delivery.setId(UUID.randomUUID());
        delivery.setTaskId(task.getId());
        delivery.setProviderCode("INBOX");
        delivery.setStatus("SENT");
        delivery.setSentAt(Instant.now());
        delivery.setCreatedAt(Instant.now());
        notificationDeliveryMapper.insert(delivery);
        task.setStatus("SENT");
        task.setUpdatedAt(Instant.now());
        notificationTaskMapper.updateById(task);
    }

    private void fail(NotificationTask task, String message) {
        var retryCount = task.getRetryCount() == null ? 0 : task.getRetryCount() + 1;
        task.setRetryCount(retryCount);
        task.setLastError(message);
        task.setStatus(retryCount >= MAX_RETRY_COUNT ? "FAILED" : "PENDING");
        task.setUpdatedAt(Instant.now());
        notificationTaskMapper.updateById(task);
    }

    private String safeMessage(RuntimeException exception) {
        var message = exception.getMessage();
        return message == null || message.length() > 1000 ? "通知 Provider 调用失败" : message;
    }
}
