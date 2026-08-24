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
import com.github.f4b6a3.uuid.UuidCreator;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.*;
import com.devops00.spectra.common.utils.SHA256Utils;
import com.devops00.spectra.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.notification.javabean.domain.NotificationRequestStatus;
import com.devops00.spectra.notification.javabean.domain.NotificationTaskStatus;
import com.devops00.spectra.notification.javabean.domain.NotificationTemplateState;
import com.devops00.spectra.notification.javabean.entity.NotificationRequestEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationTemplateEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationUserPreferenceEntity;
import com.devops00.spectra.notification.mapper.NotificationRequestMapper;
import com.devops00.spectra.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.notification.mapper.NotificationTemplateMapper;
import com.devops00.spectra.notification.mapper.NotificationUserPreferenceMapper;
import com.devops00.spectra.notification.observability.NotificationMetrics;
import com.devops00.spectra.notification.properties.NotificationModuleProperties;
import com.devops00.spectra.notification.sender.NotificationSender;
import com.devops00.spectra.notification.utils.NotificationMaskingUtils;
import com.devops00.spectra.notification.strategy.NotificationDoNotDisturbPolicy;
import com.devops00.spectra.notification.strategy.NotificationPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

/**
 * 独立通知模块的统一入队实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationGatewayImpl implements NotificationGateway {

    /**
     * 普通参数中禁止出现的敏感字段关键词。
     */
    private static final Set<String> SENSITIVE_KEYS = Set.of("code", "captcha", "password", "token", "secret");

    /**
     * 通知请求 Mapper。
     */
    private final NotificationRequestMapper requestMapper;
    /**
     * 通知任务 Mapper。
     */
    private final NotificationTaskMapper taskMapper;
    /**
     * 通知模板 Mapper。
     */
    private final NotificationTemplateMapper templateMapper;
    /**
     * 用户通知偏好 Mapper。
     */
    private final NotificationUserPreferenceMapper preferenceMapper;
    /**
     * 通知模板渲染器。
     */
    private final NotificationTemplateRenderer templateRenderer;
    /**
     * 通知用途策略。
     */
    private final NotificationPolicy policy;
    /**
     * 通知模块配置。
     */
    private final NotificationModuleProperties properties;
    /**
     * 收件人目录。
     */
    private final NotificationRecipientDirectory recipientDirectory;
    /**
     * 地址与敏感载荷保护器。
     */
    private final NotificationPayloadProtector payloadProtector;
    /**
     * 已注册的渠道发送端。
     */
    private final List<NotificationSender> senders;

    /**
     * 可选指标门面；测试或精简运行时未注册 MeterRegistry 时保持业务路径可用。
     */
    private NotificationMetrics metrics;

    @Autowired(required = false)
    public void setMetrics(NotificationMetrics metrics) {
        this.metrics = metrics;
    }

    /**
     * 查询指定渠道的配置与可用状态。
     */
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

    /**
     * 校验请求并创建通知请求及投递任务。
     */
    @Override
    @Transactional
    public NotificationReceipt enqueue(NotificationRequest request) {
        return enqueue(request, Map.of());
    }

    /**
     * 使用受控发送锁定的模板版本入队。
     */
    @Override
    @Transactional
    public NotificationReceipt enqueue(NotificationRequest request,
                                       Map<NotificationChannel, UUID> templateVersionIds) {
        if (!properties.enabled()) {
            throw new DataSaveException("通知模块未启用");
        }
        validate(request);
        var channels = policy.resolve(request.purpose(), request.channels());
        var lockedTemplateVersions = templateVersionIds == null
                ? Map.<NotificationChannel, UUID>of()
                : Map.copyOf(templateVersionIds);
        if (!lockedTemplateVersions.isEmpty()
                && channels.stream().anyMatch(channel -> !lockedTemplateVersions.containsKey(channel))) {
            throw new DataSaveException("受控发送模板版本未覆盖全部渠道");
        }
        var recipients = recipientDirectory.resolve(request.recipientUserIds());
        var existing = requestMapper.selectOne(new LambdaQueryWrapper<NotificationRequestEntity>()
                .eq(NotificationRequestEntity::getIdempotencyKey, request.idempotencyKey()));
        if (existing != null) {
            var count = taskMapper.selectCount(new LambdaQueryWrapper<NotificationTaskEntity>()
                    .eq(NotificationTaskEntity::getNotificationRequestId, existing.getId()));
            return new NotificationReceipt(existing.getId(), existing.getStatus(), Math.toIntExact(count), true);
        }

        var externalRequestId = request.requestId() == null ? UUID.randomUUID() : request.requestId();
        var now = Instant.now();
        var entity = new NotificationRequestEntity();
        entity.setBusinessType(defaultValue(request.businessType(), "SYSTEM"));
        entity.setBusinessId(defaultValue(request.businessId(), externalRequestId.toString()));
        entity.setExternalRequestId(externalRequestId.toString());
        entity.setIdempotencyKey(request.idempotencyKey());
        entity.setTemplateGroupCode(request.templateGroupCode());
        entity.setPurpose(request.purpose().name());
        entity.setSourceModule(defaultValue(request.sourceModule(), "SYSTEM"));
        entity.setInitiatorType("SERVICE");
        entity.setSourceDepartmentId(request.sourceDepartmentId());
        entity.setParameters(request.parameters());
        entity.setSensitiveParametersCiphertext(payloadProtector.protectParameters(request.sensitiveParameters()));
        entity.setStatus(NotificationRequestStatus.ACCEPTED.name());
        entity.setRecipientCount(recipients.size() + request.directAddresses().size());
        entity.setTaskCount(0);
        entity.setScheduledAt(request.scheduledAt() == null ? now : request.scheduledAt());
        entity.setExpiresAt(request.expiresAt());
        entity.setPriority(request.priority() == null ? 0 : request.priority());
        entity.setId(UuidCreator.getTimeOrderedEpoch());
        if (requestMapper.insert(entity) != 1) {
            throw new DataSaveException("创建通知请求失败");
        }
        UUID requestId = entity.getId();
        if (requestId == null) {
            throw new DataSaveException("通知请求主键生成失败");
        }
        if (metrics != null) {
            metrics.recordRequest(request.purpose().name(), NotificationRequestStatus.ACCEPTED.name());
        }

        var taskCount = 0;
        var templateSnapshot = new LinkedHashMap<String, Object>();
        for (var recipient : recipients) {
            if (!recipient.active()) {
                log.warn("通知收件人不存在或已禁用: userId={}", recipient.userId());
                continue;
            }
            for (var channel : channels) {
                if (!shouldDeliver(request.purpose(), recipient, channel)) {
                    continue;
                }
                var address = recipient.addressFor(channel);
                if (channel != NotificationChannel.IN_APP && address == null) {
                    log.warn("通知收件人缺少已验证渠道地址: userId={}, channel={}", recipient.userId(), channel);
                    continue;
                }
                taskCount += createTask(request, requestId, recipient.userId(), channel, address, now, templateSnapshot,
                        lockedTemplateVersions);
            }
        }
        for (var directAddress : request.directAddresses()) {
            if (!channels.contains(directAddress.channel())) {
                continue;
            }
            taskCount += createTask(request, requestId, null, directAddress.channel(), directAddress.address(), now,
                    templateSnapshot, lockedTemplateVersions);
        }
        requestMapper.update(null, new LambdaUpdateWrapper<NotificationRequestEntity>()
                .eq(NotificationRequestEntity::getId, requestId)
                .set(NotificationRequestEntity::getTaskCount, taskCount)
                .set(
                        NotificationRequestEntity::getTemplateSnapshot,
                        templateSnapshot,
                        "typeHandler=com.devops00.spectra.common.mybatis.PgJsonbTypeHandler"));
        return new NotificationReceipt(requestId, NotificationRequestStatus.ACCEPTED.name(), taskCount, false);
    }

    /**
     * 为单个收件人和渠道创建幂等投递任务。
     */
    private int createTask(NotificationRequest request, UUID requestId, UUID recipientUserId,
                           NotificationChannel channel, String address, Instant now,
                           Map<String, Object> templateSnapshot,
                           Map<NotificationChannel, UUID> templateVersionIds) {
        var recipientKeyHash = recipientKeyHash(recipientUserId, channel, address);
        if (taskMapper.selectCount(new LambdaQueryWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getNotificationRequestId, requestId)
                .eq(NotificationTaskEntity::getRecipientKeyHash, recipientKeyHash)
                .eq(NotificationTaskEntity::getChannel, channel.name())) > 0) {
            return 0;
        }
        var renderParameters = new HashMap<String, Object>(request.parameters());
        renderParameters.putAll(request.sensitiveParameters());
        var rendered = render(request, channel, renderParameters, templateVersionIds);
        recordTemplateSnapshot(templateSnapshot, channel, rendered);
        var hasSensitivePayload = !request.sensitiveParameters().isEmpty();
        var task = new NotificationTaskEntity();
        task.setNotificationRequestId(requestId);
        task.setReceiverUserId(recipientUserId);
        task.setRecipientKeyHash(recipientKeyHash);
        task.setRecipientMasked(NotificationMaskingUtils.maskAddress(address));
        task.setRecipientCiphertext(address == null ? null : payloadProtector.protectAddress(address));
        task.setChannel(channel.name());
        task.setPurpose(request.purpose().name());
        task.setTemplateId(rendered.templateId());
        task.setTemplateVersionNo(rendered.versionNo());
        task.setTemplateVersionDigest(rendered.versionDigest());
        task.setTitle(hasSensitivePayload ? "安全通知" : rendered.title());
        task.setContent(hasSensitivePayload ? "敏感通知内容已加密" : rendered.content());
        task.setLink(request.link());
        var taskParameters = new LinkedHashMap<String, Object>(request.parameters());
        if (rendered.providerTemplateCode() != null && !rendered.providerTemplateCode().isBlank()) {
            taskParameters.put("__provider_template_code", rendered.providerTemplateCode());
        }
        task.setExtra(taskParameters);
        if (hasSensitivePayload) {
            var protectedPayload = new LinkedHashMap<String, Object>();
            protectedPayload.put("title", rendered.title());
            protectedPayload.put("content", rendered.content());
            protectedPayload.put("parameters", request.sensitiveParameters());
            task.setSensitiveParametersCiphertext(payloadProtector.protectParameters(protectedPayload));
        } else {
            task.setSensitiveParametersCiphertext(null);
        }
        task.setPriority(request.priority() == null ? 0 : request.priority());
        task.setAttemptCount(0);
        task.setMaxAttempts(3);
        task.setScheduledAt(request.scheduledAt() == null ? now : request.scheduledAt());
        task.setNextRetryAt(task.getScheduledAt());
        task.setExpiresAt(request.expiresAt());
        task.setStatus(NotificationTaskStatus.PENDING.name());
        if (taskMapper.insert(task) != 1) {
            throw new DataSaveException("创建通知任务失败");
        }
        if (metrics != null) {
            metrics.recordTask(channel.name(), NotificationTaskStatus.PENDING.name(), request.purpose().name());
        }
        return 1;
    }

    /**
     * 优先使用渠道模板渲染内容，没有模板时回退到请求参数。
     */
    private RenderedContent render(NotificationRequest request, NotificationChannel channel,
                                   Map<String, Object> parameters,
                                   Map<NotificationChannel, UUID> templateVersionIds) {
        var query = new LambdaQueryWrapper<NotificationTemplateEntity>()
                .eq(NotificationTemplateEntity::getChannel, channel.name())
                .eq(NotificationTemplateEntity::getPurpose, request.purpose().name())
                .eq(NotificationTemplateEntity::getState, NotificationTemplateState.PUBLISHED.name())
                .isNull(NotificationTemplateEntity::getDeleted);
        var lockedTemplateId = templateVersionIds.get(channel);
        if (lockedTemplateId != null) {
            query.eq(NotificationTemplateEntity::getId, lockedTemplateId);
        } else {
            query.eq(NotificationTemplateEntity::getTemplateGroupCode, request.templateGroupCode())
                    .orderByDesc(NotificationTemplateEntity::getVersionNo)
                    .last("LIMIT 1");
        }
        var template = templateMapper.selectOne(query);
        if (lockedTemplateId != null && template == null) {
            throw new DataSaveException("受控发送模板版本已不可用");
        }
        if (template != null) {
            templateRenderer.validateParameterSecurity(template.getParameterSchema(), request.parameters(),
                    request.sensitiveParameters());
            templateRenderer.validateAll(parameters, template.getTitleTemplate(), template.getContentTemplate());
            templateRenderer.validateHtml(template.getHtmlTemplate());
            return new RenderedContent(template.getId(), template.getVersionNo(), template.getVersionDigest(),
                    template.getProviderTemplateCode(),
                    templateRenderer.render(template.getTitleTemplate(), parameters),
                    templateRenderer.render(template.getContentTemplate(), parameters));
        }
        var title = String.valueOf(parameters.getOrDefault("title", "通知"));
        var content = String.valueOf(parameters.getOrDefault("content", ""));
        templateRenderer.validateFallback(title, parameters);
        templateRenderer.validateFallback(content, parameters);
        title = templateRenderer.render(title, parameters);
        content = templateRenderer.render(content, parameters);
        if (!StringUtils.hasText(title)) {
            throw new DataSaveException("通知标题不能为空");
        }
        return new RenderedContent(null, null, null, null, title, content);
    }

    /**
     * 在逻辑请求上记录每个实际渠道使用的模板版本元数据。
     */
    private void recordTemplateSnapshot(Map<String, Object> snapshots, NotificationChannel channel,
                                        RenderedContent rendered) {
        if (rendered.templateId() == null) {
            return;
        }
        var snapshot = new LinkedHashMap<String, Object>();
        snapshot.put("template_id", rendered.templateId().toString());
        snapshot.put("version_no", rendered.versionNo());
        snapshot.put("version_digest", rendered.versionDigest());
        snapshot.put("provider_template_code", rendered.providerTemplateCode());
        snapshots.put(channel.name(), snapshot);
    }

    /**
     * 判断用户偏好是否允许向指定渠道投递。
     */
    private boolean shouldDeliver(NotificationPurpose purpose,
                                  NotificationRecipient recipient,
                                  NotificationChannel channel) {
        if (policy.mandatory(purpose)) {
            return true;
        }
        if (recipient == null || recipient.userId() == null) {
            return true;
        }
        var preference = preferenceMapper.selectOne(new LambdaQueryWrapper<NotificationUserPreferenceEntity>()
                .eq(NotificationUserPreferenceEntity::getUserId, recipient.userId())
                .eq(NotificationUserPreferenceEntity::getPurpose, purpose.name())
                .eq(NotificationUserPreferenceEntity::getChannel, channel.name()));
        if (preference == null) {
            return channel == NotificationChannel.IN_APP;
        }
        return Boolean.TRUE.equals(preference.getEnabled())
                && !NotificationDoNotDisturbPolicy.isQuiet(Boolean.TRUE.equals(preference.getDoNotDisturb()),
                        Instant.now(), preference.getDoNotDisturbStart(), preference.getDoNotDisturbEnd(),
                        NotificationDoNotDisturbPolicy.resolveZone(recipient.timezone()));
    }

    /**
     * 校验幂等、收件人、直接地址、敏感参数和跳转链接。
     */
    private void validate(NotificationRequest request) {
        if (request == null
                || !StringUtils.hasText(request.idempotencyKey())
                || request.purpose() == null
                || (request.recipientUserIds().isEmpty() && request.directAddresses().isEmpty())
                || !StringUtils.hasText(request.templateGroupCode())) {
            throw new DataSaveException("通知请求参数不完整");
        }
        if (request.recipientUserIds().stream().anyMatch(Objects::isNull)) {
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
                        || request.link().contains("..")
                        || request.link().contains("\\")
                        || request.link().toLowerCase(Locale.ROOT).contains("%2e")
                        || !isAllowedLink(request.link()))) {
            throw new DataSaveException("通知跳转链接不合法");
        }
    }

    /**
     * 只允许已登记的前端站内路由前缀；动态业务 ID 由前缀后的路径承载。
     */
    private boolean isAllowedLink(String link) {
        return properties.allowedLinkPrefixes()
                .stream()
                .anyMatch(prefix -> prefix.endsWith("/") ? link.startsWith(prefix) : link.equals(prefix));
    }

    /**
     * 返回有内容的原值，否则返回默认值。
     */
    private String defaultValue(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    /**
     * 生成不包含明文地址的稳定接收人键。
     */
    private String recipientKeyHash(UUID recipientUserId, NotificationChannel channel, String address) {
        var key = recipientUserId == null ? channel.name() + ":" + address : recipientUserId.toString();
        return SHA256Utils.hash(key);
    }

    /**
     * 渲染后的标题和正文。
     */
    private record RenderedContent(UUID templateId, Integer versionNo, String versionDigest,
                                   String providerTemplateCode, String title, String content) {
    }
}
