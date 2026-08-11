package com.devops00.spectra.notification.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationGateway;
import com.devops00.spectra.common.notification.NotificationReceipt;
import com.devops00.spectra.common.notification.NotificationRequest;
import com.devops00.spectra.notification.domain.NotificationInboxEntity;
import com.devops00.spectra.notification.domain.NotificationRequestEntity;
import com.devops00.spectra.notification.domain.NotificationTaskEntity;
import com.devops00.spectra.notification.mapper.NotificationInboxMapper;
import com.devops00.spectra.notification.mapper.NotificationRequestMapper;
import com.devops00.spectra.notification.mapper.NotificationTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 独立通知模块的统一 Gateway 默认实现。 */
@Service
@RequiredArgsConstructor
public class NotificationGatewayImpl implements NotificationGateway {

    private static final UUID SYSTEM_TENANT_ID = new UUID(0L, 0L);

    private final NotificationRequestMapper requestMapper;
    private final NotificationTaskMapper taskMapper;
    private final NotificationInboxMapper inboxMapper;

    @Override
    @Transactional
    public NotificationReceipt enqueue(NotificationRequest request) {
        validate(request);
        var existing = requestMapper.selectOne(new LambdaQueryWrapper<NotificationRequestEntity>()
                .eq(NotificationRequestEntity::getTenantId, SYSTEM_TENANT_ID)
                .eq(NotificationRequestEntity::getIdempotencyKey, request.idempotencyKey()));
        if (existing != null) {
            var count = taskMapper.selectCount(new LambdaQueryWrapper<NotificationTaskEntity>()
                    .eq(NotificationTaskEntity::getRequestId, existing.getId()));
            return new NotificationReceipt(existing.getId(), existing.getStatus(), Math.toIntExact(count), true);
        }

        var requestId = request.requestId() == null ? UUID.randomUUID() : request.requestId();
        var entity = new NotificationRequestEntity();
        entity.setId(requestId);
        entity.setTenantId(SYSTEM_TENANT_ID);
        entity.setBusinessType(request.businessType());
        entity.setBusinessId(request.businessId());
        entity.setIdempotencyKey(request.idempotencyKey());
        entity.setTemplateCode(request.templateGroupCode());
        entity.setPayload(request.parameters());
        entity.setStatus("ACCEPTED");
        entity.setCreatedAt(Instant.now());
        if (requestMapper.insert(entity) != 1) {
            throw new DataSaveException("创建通知请求失败");
        }

        var title = String.valueOf(request.parameters().getOrDefault("title", "通知"));
        var content = String.valueOf(request.parameters().getOrDefault("content", ""));
        var taskCount = 0;
        for (var recipient : request.recipientUserIds()) {
            for (var channel : channels(request)) {
                var task = new NotificationTaskEntity();
                task.setId(UUID.randomUUID());
                task.setRequestId(requestId);
                task.setTenantId(SYSTEM_TENANT_ID);
                task.setRecipientUserId(recipient);
                task.setChannel(channel.name());
                task.setTitle(title);
                task.setContent(content);
                task.setScheduledAt(request.scheduledAt() == null ? Instant.now() : request.scheduledAt());
                task.setStatus("PENDING");
                task.setRetryCount(0);
                task.setCreatedAt(Instant.now());
                task.setUpdatedAt(Instant.now());
                if (taskMapper.insert(task) != 1) {
                    throw new DataSaveException("创建通知任务失败");
                }
                if (channel == NotificationChannel.IN_APP) {
                    var inbox = new NotificationInboxEntity();
                    inbox.setId(UUID.randomUUID());
                    inbox.setTenantId(SYSTEM_TENANT_ID);
                    inbox.setRecipientUserId(recipient);
                    inbox.setRequestId(requestId);
                    inbox.setTitle(title);
                    inbox.setContent(content);
                    inbox.setCreatedAt(Instant.now());
                    if (inboxMapper.insert(inbox) != 1) {
                        throw new DataSaveException("写入站内信失败");
                    }
                    task.setStatus("SENT");
                    taskMapper.updateById(task);
                }
                taskCount++;
            }
        }
        return new NotificationReceipt(requestId, "ACCEPTED", taskCount, false);
    }

    private List<NotificationChannel> channels(NotificationRequest request) {
        return request.channels().isEmpty() ? List.of(NotificationChannel.IN_APP) : request.channels().stream().distinct().toList();
    }

    private void validate(NotificationRequest request) {
        if (request == null || !StringUtils.hasText(request.idempotencyKey()) || request.purpose() == null
                || request.recipientUserIds().isEmpty() || !StringUtils.hasText(request.templateGroupCode())) {
            throw new DataSaveException("通知请求参数不完整");
        }
        if (request.recipientUserIds().stream().anyMatch(java.util.Objects::isNull)) {
            throw new DataSaveException("通知收件人无效");
        }
    }
}
