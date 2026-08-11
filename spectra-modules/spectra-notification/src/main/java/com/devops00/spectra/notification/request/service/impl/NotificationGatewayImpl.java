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

package com.devops00.spectra.notification.request.service.impl;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationChannelAvailability;
import com.devops00.spectra.common.notification.NotificationGateway;
import com.devops00.spectra.common.notification.NotificationRecipientDirectory;
import com.devops00.spectra.common.notification.NotificationReceipt;
import com.devops00.spectra.common.notification.NotificationRequest;
import com.devops00.spectra.notification.dispatch.mapper.NotificationTaskMapper;
import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.dispatch.service.NotificationSender;
import com.devops00.spectra.notification.preference.javabean.entity.NotificationUserPreferenceEntity;
import com.devops00.spectra.notification.preference.mapper.NotificationUserPreferenceMapper;
import com.devops00.spectra.notification.configuration.NotificationModuleProperties;
import com.devops00.spectra.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.notification.request.javabean.entity.NotificationRequestEntity;
import com.devops00.spectra.notification.request.mapper.NotificationRequestMapper;
import com.devops00.spectra.notification.request.policy.NotificationPolicy;
import com.devops00.spectra.notification.template.javabean.entity.NotificationTemplateEntity;
import com.devops00.spectra.notification.template.mapper.NotificationTemplateMapper;
import com.devops00.spectra.notification.template.service.NotificationTemplateRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 独立通知模块的统一入队实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationGatewayImpl implements NotificationGateway {

    private static final UUID SYSTEM_TENANT_ID = new UUID(0L, 0L);
    private static final Set<String> SENSITIVE_KEYS = Set.of("code", "captcha", "password", "token", "secret");

    private final NotificationRequestMapper requestMapper;
    private final NotificationTaskMapper taskMapper;
    private final NotificationTemplateMapper templateMapper;
    private final NotificationUserPreferenceMapper preferenceMapper;
    private final NotificationTemplateRenderer templateRenderer;
    private final NotificationPolicy policy;
    private final NotificationModuleProperties properties;
    private final NotificationRecipientDirectory recipientDirectory;
    private final NotificationPayloadProtector payloadProtector;
    private final List<NotificationSender> senders;

    @Override
    public NotificationChannelAvailability availability(NotificationChannel channel) {
        if (!properties.enabled()) {
            return new NotificationChannelAvailability(channel, false, "MODULE_DISABLED");
        }
        if (channel == null) {
            return new NotificationChannelAvailability(null, false, "CHANNEL_REQUIRED");
        }
        return senders.stream()
                .filter(sender -> sender.channel() == channel)
                .findFirst()
                .map(sender -> new NotificationChannelAvailability(channel, sender.available(),
                        sender.available() ? "AVAILABLE" : sender.unavailableReason()))
                .orElseGet(() -> new NotificationChannelAvailability(channel, false, "CHANNEL_NOT_REGISTERED"));
    }

    @Override
    @Transactional
    public NotificationReceipt enqueue(NotificationRequest request) {
        if (!properties.enabled()) {
            throw new DataSaveException("通知模块未启用");
        }
        validate(request);
        var channels = policy.resolve(request.purpose(), request.channels());
        var recipients = recipientDirectory.resolve(request.recipientUserIds());
        var existing = requestMapper.selectOne(new LambdaQueryWrapper<NotificationRequestEntity>()
                .eq(NotificationRequestEntity::getTenantId, SYSTEM_TENANT_ID)
                .eq(NotificationRequestEntity::getIdempotencyKey, request.idempotencyKey()));
        if (existing != null) {
            var count = taskMapper.selectCount(new LambdaQueryWrapper<NotificationTaskEntity>()
                    .eq(NotificationTaskEntity::getRequestId, existing.getId()));
            return new NotificationReceipt(existing.getId(), existing.getStatus(), Math.toIntExact(count), true);
        }

        var requestId = request.requestId() == null ? UUID.randomUUID() : request.requestId();
        var now = Instant.now();
        var entity = new NotificationRequestEntity();
        entity.setId(requestId);
        entity.setTenantId(SYSTEM_TENANT_ID);
        entity.setBusinessType(defaultValue(request.businessType(), "SYSTEM"));
        entity.setBusinessId(defaultValue(request.businessId(), requestId.toString()));
        entity.setIdempotencyKey(request.idempotencyKey());
        entity.setTemplateCode(request.templateGroupCode());
        entity.setPurpose(request.purpose().name());
        entity.setSourceModule(defaultValue(request.sourceModule(), "SYSTEM"));
        entity.setSourceDepartmentId(request.sourceDepartmentId());
        entity.setDataScopeKey(request.sourceDepartmentId() == null ? null : request.sourceDepartmentId().toString());
        entity.setPayload(request.parameters());
        entity.setSensitivePayload(payloadProtector.protectParameters(request.sensitiveParameters()));
        entity.setStatus("ACCEPTED");
        entity.setScheduledAt(request.scheduledAt() == null ? now : request.scheduledAt());
        entity.setExpiresAt(request.expiresAt());
        entity.setPriority(request.priority() == null ? 0 : request.priority());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        if (requestMapper.insert(entity) != 1) {
            throw new DataSaveException("创建通知请求失败");
        }

        var taskCount = 0;
        for (var recipient : recipients) {
            if (!recipient.active()) {
                log.warn("通知收件人不存在或已禁用: userId={}", recipient.userId());
                continue;
            }
            for (var channel : channels) {
                if (!shouldDeliver(request.purpose(), recipient.userId(), channel)) {
                    continue;
                }
                var address = recipient.addressFor(channel);
                if (channel != NotificationChannel.IN_APP && address == null) {
                    log.warn("通知收件人缺少已验证渠道地址: userId={}, channel={}", recipient.userId(), channel);
                    continue;
                }
                taskCount += createTask(request, requestId, recipient.userId(), channel, address, now);
            }
        }
        for (var directAddress : request.directAddresses()) {
            if (!channels.contains(directAddress.channel())) {
                continue;
            }
            taskCount += createTask(request, requestId, null, directAddress.channel(), directAddress.address(), now);
        }
        return new NotificationReceipt(requestId, "ACCEPTED", taskCount, false);
    }

    private int createTask(NotificationRequest request, UUID requestId, UUID recipientUserId,
            NotificationChannel channel, String address, Instant now) {
        if (taskMapper.selectCount(new LambdaQueryWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getRequestId, requestId)
                .eq(NotificationTaskEntity::getRecipientUserId, recipientUserId)
                .eq(NotificationTaskEntity::getChannel, channel.name())) > 0) {
            return 0;
        }
        var renderParameters = new HashMap<String, Object>(request.parameters());
        renderParameters.putAll(request.sensitiveParameters());
        var rendered = render(request, channel, renderParameters);
        var hasSensitivePayload = !request.sensitiveParameters().isEmpty();
        var task = new NotificationTaskEntity();
        task.setId(UUID.randomUUID());
        task.setRequestId(requestId);
        task.setTenantId(SYSTEM_TENANT_ID);
        task.setRecipientUserId(recipientUserId);
        task.setRecipientAddress(address == null ? null : payloadProtector.protectAddress(address));
        task.setChannel(channel.name());
        task.setPurpose(request.purpose().name());
        task.setTitle(hasSensitivePayload ? "安全通知" : rendered.title());
        task.setContent(hasSensitivePayload ? "敏感通知内容已加密" : rendered.content());
        task.setLink(request.link());
        task.setExtra(request.parameters());
        task.setSensitivePayload(hasSensitivePayload
                ? payloadProtector.protectParameters(Map.of("title", rendered.title(), "content", rendered.content()))
                : null);
        task.setScheduledAt(request.scheduledAt() == null ? now : request.scheduledAt());
        task.setExpiresAt(request.expiresAt());
        task.setStatus("PENDING");
        task.setRetryCount(0);
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        if (taskMapper.insert(task) != 1) {
            throw new DataSaveException("创建通知任务失败");
        }
        return 1;
    }

    private RenderedContent render(NotificationRequest request, NotificationChannel channel,
            Map<String, Object> parameters) {
        var template = templateMapper.selectOne(new LambdaQueryWrapper<NotificationTemplateEntity>()
                .eq(NotificationTemplateEntity::getTenantId, SYSTEM_TENANT_ID)
                .eq(NotificationTemplateEntity::getTemplateGroupCode, request.templateGroupCode())
                .eq(NotificationTemplateEntity::getChannel, channel.name())
                .eq(NotificationTemplateEntity::getEnabled, true)
                .isNull(NotificationTemplateEntity::getDeleted)
                .orderByDesc(NotificationTemplateEntity::getVersionNo)
                .last("LIMIT 1"));
        if (template != null) {
            templateRenderer.validate(template.getTitleTemplate(), parameters);
            templateRenderer.validate(template.getContentTemplate(), parameters);
            templateRenderer.validateHtml(template.getHtmlTemplate());
            return new RenderedContent(templateRenderer.render(template.getTitleTemplate(), parameters),
                    templateRenderer.render(template.getContentTemplate(), parameters));
        }
        var title = String.valueOf(parameters.getOrDefault("title", "通知"));
        var content = String.valueOf(parameters.getOrDefault("content", ""));
        templateRenderer.validate(title, parameters);
        templateRenderer.validate(content, parameters);
        title = templateRenderer.render(title, parameters);
        content = templateRenderer.render(content, parameters);
        if (!StringUtils.hasText(title)) {
            throw new DataSaveException("通知标题不能为空");
        }
        return new RenderedContent(title, content);
    }

    private boolean shouldDeliver(com.devops00.spectra.common.notification.NotificationPurpose purpose, UUID recipient,
            NotificationChannel channel) {
        if (policy.mandatory(purpose)) {
            return true;
        }
        if (recipient == null) {
            return true;
        }
        var preference = preferenceMapper.selectOne(new LambdaQueryWrapper<NotificationUserPreferenceEntity>()
                .eq(NotificationUserPreferenceEntity::getTenantId, SYSTEM_TENANT_ID)
                .eq(NotificationUserPreferenceEntity::getUserId, recipient)
                .eq(NotificationUserPreferenceEntity::getPurpose, purpose.name())
                .eq(NotificationUserPreferenceEntity::getChannel, channel.name()));
        if (preference == null) {
            return channel == NotificationChannel.IN_APP;
        }
        return Boolean.TRUE.equals(preference.getEnabled()) && !Boolean.TRUE.equals(preference.getDoNotDisturb());
    }

    private void validate(NotificationRequest request) {
        if (request == null
            || !StringUtils.hasText(request.idempotencyKey())
            || request.purpose() == null
            || (request.recipientUserIds().isEmpty() && request.directAddresses().isEmpty())
            || !StringUtils.hasText(request.templateGroupCode())) {
            throw new DataSaveException("通知请求参数不完整");
        }
        if (request.recipientUserIds().stream().anyMatch(java.util.Objects::isNull)) {
            throw new DataSaveException("通知收件人无效");
        }
        for (var directAddress : request.directAddresses()) {
            if (directAddress == null
                || directAddress.channel() == null
                || !StringUtils.hasText(directAddress.address())
                || directAddress.channel() == NotificationChannel.IN_APP
                || !policy.allowsDirectAddress(request.purpose())) {
                throw new DataSaveException("通知直接收件地址不合法");
            }
        }
        if (request.parameters()
                .keySet()
                .stream()
                .map(key -> key.toLowerCase(Locale.ROOT))
                .anyMatch(key -> SENSITIVE_KEYS.stream().anyMatch(key::contains))) {
            throw new DataSaveException("通知普通参数不能包含敏感字段");
        }
        if (StringUtils.hasText(request.link())
            && (!request.link().startsWith("/")
                || request.link().startsWith("//")
                || request.link().contains(".."))) {
            throw new DataSaveException("通知跳转链接不合法");
        }
    }

    private String defaultValue(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private record RenderedContent(String title, String content) {
    }
}
