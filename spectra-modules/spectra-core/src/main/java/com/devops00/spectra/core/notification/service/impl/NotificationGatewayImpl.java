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

package com.devops00.spectra.core.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.f4b6a3.uuid.UuidCreator;
import com.devops00.spectra.common.audit.RequestCorrelationContext;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationChannelAvailability;
import com.devops00.spectra.common.notification.NotificationGateway;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationReceipt;
import com.devops00.spectra.common.notification.NotificationRecipient;
import com.devops00.spectra.common.notification.NotificationRecipientDirectory;
import com.devops00.spectra.common.notification.NotificationRequest;
import com.devops00.spectra.core.notification.security.NotificationDigest;
import com.devops00.spectra.core.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.core.notification.javabean.domain.NotificationRequestStatus;
import com.devops00.spectra.core.notification.javabean.domain.NotificationTaskStatus;
import com.devops00.spectra.core.notification.javabean.entity.NotificationRequestEntity;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTemplateEntity;
import com.devops00.spectra.core.notification.javabean.entity.NotificationUserPreferenceEntity;
import com.devops00.spectra.core.notification.mapper.NotificationRequestMapper;
import com.devops00.spectra.core.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.core.notification.mapper.NotificationTemplateMapper;
import com.devops00.spectra.core.notification.mapper.NotificationUserPreferenceMapper;
import com.devops00.spectra.core.notification.observability.NotificationMetrics;
import com.devops00.spectra.core.notification.properties.NotificationModuleProperties;
import com.devops00.spectra.core.notification.sender.NotificationSenderRegistry;
import com.devops00.spectra.core.notification.service.NotificationTaskBatchPlanner;
import com.devops00.spectra.core.notification.strategy.NotificationDoNotDisturbPolicy;
import com.devops00.spectra.core.notification.strategy.NotificationPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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
     * 通知任务批量规划器。
     */
    private final NotificationTaskBatchPlanner taskPlanner;
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
    private final NotificationSenderRegistry senderRegistry;

    /**
     * 可选指标门面；测试或精简运行时未注册 MeterRegistry 时保持业务路径可用。
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
        return senderRegistry.find(channel)
                .map(sender -> {
                    var available = sender.available();
                    return new NotificationChannelAvailability(channel, available,
                            available ? "AVAILABLE" : sender.unavailableReason());
                })
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
        entity.setPriority(normalizePriority(request.priority()));
        var correlationId = RequestCorrelationContext.current().correlationId();
        if (correlationId == null) {
            correlationId = RequestCorrelationContext.forTask(null).correlationId();
        }
        entity.setTraceId(correlationId);
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

        var templateSnapshot = new LinkedHashMap<String, Object>();
        var targets = collectTargets(request, channels, recipients);
        var renderedTemplates = renderTemplates(request, targets, lockedTemplateVersions, templateSnapshot);
        var drafts = taskPlanner.plan(request, requestId, now, entity.getCreatedBy(), targets, renderedTemplates);
        var taskCount = persistTasks(request, requestId, drafts);
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
     * 收集并去重实际需要投递的接收人和渠道目标。
     */
    private List<NotificationTaskBatchPlanner.TaskTarget> collectTargets(NotificationRequest request,
                                                                         List<NotificationChannel> channels,
                                                                         List<NotificationRecipient> recipients) {
        var targets = new LinkedHashMap<String, NotificationTaskBatchPlanner.TaskTarget>();
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
                var target = new NotificationTaskBatchPlanner.TaskTarget(recipient.userId(), channel, address);
                targets.putIfAbsent(targetKey(target), target);
            }
        }
        for (var directAddress : request.directAddresses()) {
            if (!channels.contains(directAddress.channel())) {
                continue;
            }
            var target = new NotificationTaskBatchPlanner.TaskTarget(null, directAddress.channel(),
                    directAddress.address());
            targets.putIfAbsent(targetKey(target), target);
        }
        return List.copyOf(targets.values());
    }

    /**
     * 一次加载批次实际使用的模板并渲染每个渠道一次。
     */
    private Map<NotificationChannel, NotificationTaskBatchPlanner.TemplateSnapshot> renderTemplates(
                                                                                                    NotificationRequest request,
                                                                                                    List<NotificationTaskBatchPlanner.TaskTarget> targets,
                                                                                                    Map<NotificationChannel, UUID> templateVersionIds,
                                                                                                    Map<String, Object> templateSnapshot) {
        if (targets.isEmpty()) {
            return Map.of();
        }
        var channels = targets.stream().map(NotificationTaskBatchPlanner.TaskTarget::channel).distinct().toList();
        var channelNames = channels.stream().map(NotificationChannel::name).toList();
        var lockedTemplateIds = new ArrayList<>(templateVersionIds.values());
        var templates = templateMapper.selectPublishedTemplates(request.templateGroupCode(), request.purpose().name(),
                channelNames, lockedTemplateIds);
        var templatesByChannel = new EnumMap<NotificationChannel, NotificationTemplateEntity>(NotificationChannel.class);
        for (var template : templates == null ? List.<NotificationTemplateEntity>of() : templates) {
            try {
                templatesByChannel.putIfAbsent(NotificationChannel.valueOf(template.getChannel()), template);
            } catch (IllegalArgumentException exception) {
                throw new DataSaveException("通知模板渠道不合法", exception);
            }
        }
        var renderedByChannel = new EnumMap<NotificationChannel, NotificationTaskBatchPlanner.TemplateSnapshot>(
                NotificationChannel.class);
        for (var channel : channels) {
            var template = templatesByChannel.get(channel);
            var renderParameters = new HashMap<String, Object>(request.parameters());
            renderParameters.putAll(request.sensitiveParameters());
            var rendered = render(request, renderParameters, template,
                    templateVersionIds.containsKey(channel));
            renderedByChannel.put(channel, rendered);
            recordTemplateSnapshot(templateSnapshot, channel, rendered);
        }
        return Map.copyOf(renderedByChannel);
    }

    /**
     * 批量过滤已有幂等任务并写入剩余任务。
     */
    private int persistTasks(NotificationRequest request, UUID requestId,
                             List<NotificationTaskBatchPlanner.TaskDraft> drafts) {
        if (drafts.isEmpty()) {
            return 0;
        }
        var candidates = drafts.stream().map(NotificationTaskBatchPlanner.TaskDraft::task).toList();
        var existing = taskMapper.selectExistingTasks(requestId, candidates);
        var existingKeys = new HashSet<String>();
        for (var task : existing == null ? List.<NotificationTaskEntity>of() : existing) {
            existingKeys.add(taskKey(task.getRecipientKeyHash(), task.getChannel()));
        }
        var tasks = drafts.stream()
                .filter(draft -> !existingKeys.contains(draft.idempotencyKey()))
                .map(NotificationTaskBatchPlanner.TaskDraft::task)
                .toList();
        if (tasks.isEmpty()) {
            return 0;
        }
        if (taskMapper.insertBatch(tasks) != tasks.size()) {
            throw new DataSaveException("创建通知任务失败");
        }
        if (metrics != null) {
            for (var task : tasks) {
                metrics.recordTask(task.getChannel(), NotificationTaskStatus.PENDING.name(), request.purpose().name());
            }
        }
        return tasks.size();
    }

    /**
     * 优先使用已加载的渠道模板渲染内容，没有模板时回退到请求参数。
     */
    private NotificationTaskBatchPlanner.TemplateSnapshot render(NotificationRequest request,
                                                                 Map<String, Object> parameters,
                                                                 NotificationTemplateEntity template,
                                                                 boolean lockedTemplate) {
        if (lockedTemplate && template == null) {
            throw new DataSaveException("受控发送模板版本已不可用");
        }
        if (template != null) {
            templateRenderer.validateParameterSecurity(template.getParameterSchema(), request.parameters(),
                    request.sensitiveParameters());
            templateRenderer.validateAll(parameters, template.getTitleTemplate(), template.getContentTemplate());
            templateRenderer.validateHtml(template.getHtmlTemplate());
            return new NotificationTaskBatchPlanner.TemplateSnapshot(template.getId(), template.getVersionNo(),
                    template.getVersionDigest(),
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
        return new NotificationTaskBatchPlanner.TemplateSnapshot(null, null, null, null, title, content);
    }

    /**
     * 在逻辑请求上记录每个实际渠道使用的模板版本元数据。
     */
    private void recordTemplateSnapshot(Map<String, Object> snapshots, NotificationChannel channel,
                                        NotificationTaskBatchPlanner.TemplateSnapshot rendered) {
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
     * 转换、解析或规范化数据（{@code normalizePriority}）。
     */
    private int normalizePriority(Integer priority) {
        return priority == null ? 0 : priority;
    }

    /**
     * 返回与任务唯一索引一致的接收人和渠道组合键。
     */
    private String taskKey(String recipientKeyHash, String channel) {
        return recipientKeyHash + "\u0000" + channel;
    }

    /**
     * 返回与任务幂等键一致的目标去重键。
     */
    private String targetKey(NotificationTaskBatchPlanner.TaskTarget target) {
        var key = target.recipientUserId() == null
                ? target.channel().name() + ":" + target.address()
                : target.recipientUserId().toString();
        return taskKey(NotificationDigest.hash(key), target.channel().name());
    }
}
