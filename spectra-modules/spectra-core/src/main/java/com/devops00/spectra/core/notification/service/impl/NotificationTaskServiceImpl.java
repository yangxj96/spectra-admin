package com.devops00.spectra.core.notification.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.core.notification.constant.NotificationChannel;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTask;
import com.devops00.spectra.core.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.core.notification.service.NotificationTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 通知任务拆分服务实现。 */
@Service
@RequiredArgsConstructor
public class NotificationTaskServiceImpl implements NotificationTaskService {

    private final NotificationTaskMapper notificationTaskMapper;

    @Override
    @Transactional
    public List<NotificationTask> split(UUID requestId, UUID tenantId, List<UUID> recipientUserIds, String channel, String address) {
        if (requestId == null || tenantId == null || recipientUserIds == null || recipientUserIds.isEmpty()
                || !StringUtils.hasText(channel)) {
            throw new DataSaveException("通知任务参数不完整");
        }
        try {
            NotificationChannel.valueOf(channel.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new DataSaveException("不支持的通知渠道");
        }
        var now = Instant.now();
        var result = new ArrayList<NotificationTask>();
        for (var recipientUserId : recipientUserIds.stream().distinct().toList()) {
            if (recipientUserId == null) {
                continue;
            }
            var task = new NotificationTask();
            task.setId(UUID.randomUUID());
            task.setRequestId(requestId);
            task.setTenantId(tenantId);
            task.setRecipientUserId(recipientUserId);
            task.setRecipientAddress(address);
            task.setChannel(channel.toUpperCase());
            task.setScheduledAt(now);
            task.setStatus("PENDING");
            task.setRetryCount(0);
            task.setCreatedAt(now);
            task.setUpdatedAt(now);
            if (notificationTaskMapper.insert(task) != 1) {
                throw new DataSaveException("创建通知任务失败");
            }
            result.add(task);
        }
        if (result.isEmpty()) {
            throw new DataSaveException("通知任务没有有效收件人");
        }
        return result;
    }
}
