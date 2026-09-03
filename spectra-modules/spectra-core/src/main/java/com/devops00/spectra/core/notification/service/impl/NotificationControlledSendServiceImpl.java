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
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationAudienceDirectory;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationChannelAvailability;
import com.devops00.spectra.common.notification.NotificationGateway;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationReceipt;
import com.devops00.spectra.common.notification.NotificationRecipient;
import com.devops00.spectra.common.notification.NotificationRecipientDirectory;
import com.devops00.spectra.common.notification.NotificationRequest;
import com.devops00.spectra.common.utils.SHA256Utils;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.core.notification.javabean.entity.NotificationSendPreviewEntity;
import com.devops00.spectra.core.notification.javabean.domain.NotificationPreviewStatus;
import com.devops00.spectra.core.notification.javabean.domain.NotificationTemplateState;
import com.devops00.spectra.core.notification.javabean.entity.NotificationUserPreferenceEntity;
import com.devops00.spectra.core.notification.javabean.from.NotificationControlledSendApplyFrom;
import com.devops00.spectra.core.notification.javabean.from.NotificationControlledSendFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationControlledSendApplyVO;
import com.devops00.spectra.core.notification.javabean.vo.NotificationControlledSendPreviewVO;
import com.devops00.spectra.core.notification.javabean.vo.NotificationControlledSendSampleVO;
import com.devops00.spectra.core.notification.javabean.vo.NotificationControlledSendSkippedDetailVO;
import com.devops00.spectra.core.notification.javabean.vo.NotificationControlledSendTemplateVO;
import com.devops00.spectra.core.notification.mapper.NotificationSendPreviewMapper;
import com.devops00.spectra.core.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.core.notification.mapper.NotificationTemplateMapper;
import com.devops00.spectra.core.notification.mapper.NotificationUserPreferenceMapper;
import com.devops00.spectra.core.notification.service.NotificationControlledSendService;
import com.devops00.spectra.core.notification.utils.NotificationMaskingUtils;
import com.devops00.spectra.core.notification.strategy.NotificationDoNotDisturbPolicy;
import com.devops00.spectra.core.notification.strategy.NotificationPolicy;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 受控发送 Preview/Apply 实现。
 *
 * <p>Preview 只保存短时的非敏感请求快照和摘要，Apply 再次展开当前受众并通过统一 Gateway 入队。</p>
 */
@Service
@RequiredArgsConstructor
public class NotificationControlledSendServiceImpl implements NotificationControlledSendService {

    private static final int PREVIEW_MINUTES = 10;

    private static final int MAX_CANDIDATE_USERS = 5_000;

    private static final int MAX_SAMPLE_COUNT = 10;

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Set<String> SENSITIVE_KEYS = Set.of("code", "captcha", "password", "token", "secret");

    private final NotificationSendPreviewMapper previewMapper;

    private final NotificationTaskMapper taskMapper;

    private final NotificationTemplateMapper templateMapper;

    private final NotificationUserPreferenceMapper preferenceMapper;

    private final NotificationGateway notificationGateway;

    private final NotificationAudienceDirectory audienceDirectory;

    private final NotificationRecipientDirectory recipientDirectory;

    private final NotificationPolicy policy;

    private final NotificationTemplateRenderer templateRenderer;

    private final SecurityContextAccessor securityContextAccessor;

    private final ObjectMapper objectMapper;

    private final TimeMapper timeMapper;

    @Override
    @Transactional
    public NotificationControlledSendPreviewVO preview(NotificationControlledSendFrom params) {
        var operatorId = requireOperator();
        var prepared = prepare(params);
        var evaluation = evaluate(prepared);
        var token = token();
        var now = Instant.now();
        var entity = new NotificationSendPreviewEntity();
        entity.setId(UuidCreator.getTimeOrderedEpoch());
        entity.setOperatorUserId(operatorId);
        entity.setRequestHash(requestHash(params));
        entity.setPreviewTokenHash(hash(token));
        entity.setResolutionHash(evaluation.resolutionHash());
        entity.setRequestSnapshot(snapshot(params));
        entity.setExpiresAt(now.plusSeconds(PREVIEW_MINUTES * 60L));
        entity.setStatus(NotificationPreviewStatus.PREVIEWED.name());
        if (previewMapper.insert(entity) != 1) {
            throw new DataSaveException("创建通知发送 Preview 失败");
        }
        return new NotificationControlledSendPreviewVO(entity.getId(), token, entity.getRequestHash(),
                timeMapper.toLocalDateTime(entity.getExpiresAt()), evaluation.candidateUserCount(), evaluation.eligibleTaskCount(),
                evaluation.skippedTaskCount(), evaluation.skippedCounts(), evaluation.skippedDetails(),
                evaluation.channelAvailability(),
                prepared.templates(), evaluation.samples());
    }

    @Override
    @Transactional
    public NotificationControlledSendApplyVO apply(NotificationControlledSendApplyFrom params) {
        var operatorId = requireOperator();
        var entity = previewMapper.selectById(params.getPreviewId());
        if (entity == null || !operatorId.equals(entity.getOperatorUserId())) {
            throw new DataNotExistException("通知发送 Preview 不存在");
        }
        verifyToken(entity, params);
        if (NotificationPreviewStatus.APPLIED.name().equals(entity.getStatus())
                && entity.getAppliedRequestId() != null) {
            var taskCount = Math.toIntExact(
                    taskMapper.selectCount(new LambdaQueryWrapper<com.devops00.spectra.core.notification.javabean.entity.NotificationTaskEntity>()
                            .eq(com.devops00.spectra.core.notification.javabean.entity.NotificationTaskEntity::getNotificationRequestId,
                                    entity.getAppliedRequestId())));
            return new NotificationControlledSendApplyVO(
                    entity.getAppliedRequestId(), NotificationPreviewStatus.APPLIED.name(), taskCount, true);
        }
        var now = Instant.now();
        if (!NotificationPreviewStatus.PREVIEWED.name().equals(entity.getStatus())
                || entity.getExpiresAt() == null
                || !now.isBefore(entity.getExpiresAt())) {
            markExpired(entity);
            throw new DataException("通知发送 Preview 已过期或已消费");
        }
        if (!MessageDigest.isEqual(entity.getRequestHash().getBytes(StandardCharsets.US_ASCII),
                params.getRequestHash().getBytes(StandardCharsets.US_ASCII))) {
            throw new DataException("通知发送请求摘要不一致，请重新 Preview");
        }
        var request = readSnapshot(entity.getRequestSnapshot());
        var prepared = prepare(request);
        var evaluation = evaluate(prepared);
        if (!MessageDigest.isEqual(entity.getResolutionHash().getBytes(StandardCharsets.US_ASCII),
                evaluation.resolutionHash().getBytes(StandardCharsets.US_ASCII))) {
            throw new DataException("受众、数据范围或渠道状态已变化，请重新 Preview");
        }
        if (evaluation.eligibleTaskCount() == 0
                || evaluation.channelAvailability()
                        .values()
                        .stream()
                        .anyMatch(availability -> !availability.available())) {
            throw new DataException("当前没有可发送的有效受众或渠道");
        }
        if (previewMapper.update(null, new LambdaUpdateWrapper<NotificationSendPreviewEntity>()
                .eq(NotificationSendPreviewEntity::getId, entity.getId())
                .eq(NotificationSendPreviewEntity::getOperatorUserId, operatorId)
                .eq(NotificationSendPreviewEntity::getStatus, NotificationPreviewStatus.PREVIEWED.name())
                .isNull(NotificationSendPreviewEntity::getConsumedAt)
                .gt(NotificationSendPreviewEntity::getExpiresAt, now)
                .set(NotificationSendPreviewEntity::getStatus, NotificationPreviewStatus.APPLYING.name())
                .set(NotificationSendPreviewEntity::getConsumedAt, now)) != 1) {
            throw new DataException("通知发送 Preview 已被其他请求消费");
        }

        var gatewayRequest = new NotificationRequest(null, request.getIdempotencyKey(), request.getPurpose(),
                request.getChannels(), evaluation.candidateUserIds(), List.of(), prepared.templateGroupCode(),
                request.getParameters(), Map.of(), request.getBusinessType(), request.getBusinessId(),
                "NOTIFICATION_ADMIN", null, null, null, 0, request.getLink());
        NotificationReceipt receipt = notificationGateway.enqueue(gatewayRequest, request.getTemplateVersionIds());
        entity.setStatus(NotificationPreviewStatus.APPLIED.name());
        entity.setAppliedRequestId(receipt.requestId());
        if (previewMapper.updateById(entity) != 1) {
            throw new DataSaveException("保存通知发送 Apply 结果失败");
        }
        return new NotificationControlledSendApplyVO(receipt.requestId(), receipt.status(), receipt.taskCount(),
                receipt.idempotentReplay());
    }

    /**
     * 创建或构建目标数据（{@code prepare}）。
     */
    private PreparedRequest prepare(NotificationControlledSendFrom params) {
        validate(params);
        var templates = new EnumMap<NotificationChannel, NotificationControlledSendTemplateVO>(NotificationChannel.class);
        String templateGroupCode = null;
        for (var channel : params.getChannels().stream().distinct().toList()) {
            var templateId = params.getTemplateVersionIds().get(channel);
            var template = templateMapper.selectById(templateId);
            if (template == null
                    || !NotificationTemplateState.PUBLISHED.name().equals(template.getState())
                    || !channel.name().equals(template.getChannel())
                    || !params.getPurpose().name().equals(template.getPurpose())) {
                throw new DataException("模板版本已不存在、未发布或与用途渠道不匹配");
            }
            if (templateGroupCode == null) {
                templateGroupCode = template.getTemplateGroupCode();
            } else if (!templateGroupCode.equals(template.getTemplateGroupCode())) {
                throw new DataException("多个渠道必须使用同一模板组");
            }
            templateRenderer.validateAll(params.getParameters(), template.getTitleTemplate(),
                    template.getContentTemplate());
            templateRenderer.validateHtml(template.getHtmlTemplate());
            var vo = new NotificationControlledSendTemplateVO();
            vo.setTemplateId(template.getId());
            vo.setChannel(template.getChannel());
            vo.setVersionNo(template.getVersionNo());
            vo.setVersionDigest(template.getVersionDigest());
            vo.setTitle(templateRenderer.render(template.getTitleTemplate(), params.getParameters()));
            vo.setContent(templateRenderer.render(template.getContentTemplate(), params.getParameters()));
            vo.setHtml(templateRenderer.render(template.getHtmlTemplate(), params.getParameters()));
            templates.put(channel, vo);
        }
        return new PreparedRequest(templateGroupCode, templates, params);
    }

    /**
     * 处理内部业务逻辑（{@code evaluate}）。
     */
    private Evaluation evaluate(PreparedRequest prepared) {
        var request = prepared.request();
        var candidateUserIds = audienceDirectory.resolve(request.getAudience().toAudience());
        if (candidateUserIds.size() > MAX_CANDIDATE_USERS) {
            throw new DataException("受众规模超过 5000 个用户上限");
        }
        var recipients = recipientDirectory.resolve(candidateUserIds);
        var preferences = candidateUserIds.isEmpty()
                ? List.<NotificationUserPreferenceEntity>of()
                : preferenceMapper.selectList(new LambdaQueryWrapper<NotificationUserPreferenceEntity>()
                        .eq(NotificationUserPreferenceEntity::getPurpose, request.getPurpose().name())
                        .in(NotificationUserPreferenceEntity::getChannel,
                                request.getChannels().stream().map(Enum::name).toList())
                        .in(NotificationUserPreferenceEntity::getUserId, candidateUserIds));
        var preferenceMap = preferences.stream()
                .collect(Collectors.toMap(
                        preference -> preference.getUserId() + ":" + preference.getChannel(), preference -> preference,
                        (left, right) -> right));
        var availability = new EnumMap<NotificationChannel, NotificationChannelAvailability>(NotificationChannel.class);
        for (var channel : request.getChannels()) {
            availability.put(channel, notificationGateway.availability(channel));
        }
        var skipped = new LinkedHashMap<String, Integer>();
        var skippedByChannel = new LinkedHashMap<NotificationChannel, Map<String, Integer>>();
        var samples = new ArrayList<NotificationControlledSendSampleVO>();
        var eligible = 0;
        var fingerprints = new ArrayList<String>();
        for (var recipient : recipients) {
            for (var channel : request.getChannels()) {
                var reason = skipReason(recipient, channel, request.getPurpose(), availability.get(channel),
                        preferenceMap.get(recipient.userId() + ":" + channel.name()));
                fingerprints.add(recipient.userId() + ":" + channel.name() + ":" + reason);
                if (reason == null) {
                    eligible++;
                    if (samples.size() < MAX_SAMPLE_COUNT) {
                        samples.add(new NotificationControlledSendSampleVO(channel.name(),
                                NotificationMaskingUtils.maskAddressOrPlaceholder(channel == NotificationChannel.IN_APP
                                        ? "站内消息"
                                        : recipient.addressFor(channel))));
                    }
                } else {
                    skipped.merge(reason, 1, Integer::sum);
                    skippedByChannel.computeIfAbsent(channel, ignored -> new LinkedHashMap<>())
                            .merge(reason, 1, Integer::sum);
                }
            }
        }
        var total = recipients.size() * request.getChannels().size();
        fingerprints.sort(String::compareTo);
        return new Evaluation(candidateUserIds, candidateUserIds.size(), eligible, total - eligible, skipped,
                skippedDetails(skippedByChannel),
                availability, samples, hash(fingerprints));
    }

    /**
     * 转换、解析或规范化数据（{@code skippedDetails}）。
     */
    private List<NotificationControlledSendSkippedDetailVO> skippedDetails(
                                                                           Map<NotificationChannel, Map<String, Integer>> skippedByChannel) {
        var details = new ArrayList<NotificationControlledSendSkippedDetailVO>();
        skippedByChannel.forEach((channel, reasons) -> reasons
                .forEach((reason, count) -> details.add(new NotificationControlledSendSkippedDetailVO(channel, reason, count))));
        return List.copyOf(details);
    }

    /**
     * 处理内部业务逻辑（{@code skipReason}）。
     */
    private String skipReason(NotificationRecipient recipient, NotificationChannel channel,
                              NotificationPurpose purpose, NotificationChannelAvailability availability,
                              NotificationUserPreferenceEntity preference) {
        if (recipient == null || !recipient.active()) {
            return "OUT_OF_SCOPE_OR_INACTIVE";
        }
        if (availability == null || !availability.available()) {
            return "CHANNEL_UNAVAILABLE";
        }
        if (channel != NotificationChannel.IN_APP && recipient.addressFor(channel) == null) {
            return "MISSING_CHANNEL_ADDRESS";
        }
        if (!policy.mandatory(purpose)) {
            if (preference == null) {
                return channel == NotificationChannel.IN_APP ? null : "PREFERENCE_DISABLED_OR_QUIET";
            }
            if (!Boolean.TRUE.equals(preference.getEnabled())
                    || NotificationDoNotDisturbPolicy.isQuiet(Boolean.TRUE.equals(preference.getDoNotDisturb()),
                            Instant.now(), preference.getDoNotDisturbStart(), preference.getDoNotDisturbEnd(),
                            NotificationDoNotDisturbPolicy.resolveZone(recipient.timezone()))) {
                return "PREFERENCE_DISABLED_OR_QUIET";
            }
        }
        return null;
    }

    /**
     * 校验并确保数据满足当前约束（{@code validate}）。
     */
    private void validate(NotificationControlledSendFrom params) {
        if (params == null
                || params.getPurpose() == null
                || params.getChannels() == null
                || params.getChannels().isEmpty()
                || params.getTemplateVersionIds() == null
                || params.getAudience() == null
                || params.getParameters() == null) {
            throw new DataException("受控发送参数不完整");
        }
        if (!StringUtils.hasText(params.getIdempotencyKey()) || params.getIdempotencyKey().length() > 200) {
            throw new DataException("受控发送幂等键不合法");
        }
        var channels = policy.resolve(params.getPurpose(), params.getChannels());
        if (channels.size() != params.getChannels().stream().distinct().count()
                || !channels.containsAll(params.getChannels())
                || params.getTemplateVersionIds().size() != channels.size()
                || !params.getTemplateVersionIds().keySet().equals(Set.copyOf(channels))) {
            throw new DataException("受控发送渠道或模板版本不合法");
        }
        var audience = params.getAudience();
        var selected = size(audience.getUserIds()) + size(audience.getDepartmentIds()) + size(audience.getRoleIds());
        if (selected == 0 || selected > MAX_CANDIDATE_USERS) {
            throw new DataException("受众范围不能为空且不能超过 5000 个选择项");
        }
        if (params.getParameters().keySet().stream().anyMatch(this::sensitiveKey)) {
            throw new DataException("受控发送参数不能包含验证码、密码或令牌字段");
        }
    }

    /**
     * 处理内部业务逻辑（{@code size}）。
     */
    private int size(List<?> values) {
        return values == null ? 0 : values.size();
    }

    /**
     * 处理内部业务逻辑（{@code sensitiveKey}）。
     */
    private boolean sensitiveKey(String key) {
        var normalized = key == null ? "" : key.toLowerCase(Locale.ROOT);
        return SENSITIVE_KEYS.stream().anyMatch(normalized::contains);
    }

    /**
     * 校验并确保数据满足当前约束（{@code requireOperator}）。
     */
    private UUID requireOperator() {
        var operatorId = securityContextAccessor.currentUserId();
        if (operatorId == null) {
            throw new DataException("受控发送必须在登录用户上下文中执行");
        }
        return operatorId;
    }

    /**
     * 处理内部业务逻辑（{@code requestHash}）。
     */
    private String requestHash(NotificationControlledSendFrom params) {
        try {
            return hash(objectMapper.writeValueAsString(params));
        } catch (RuntimeException exception) {
            throw new DataSaveException("生成通知发送请求摘要失败", exception);
        }
    }

    /**
     * 处理内部业务逻辑（{@code snapshot}）。
     */
    private Map<String, Object> snapshot(NotificationControlledSendFrom params) {
        try {
            return objectMapper.readValue(
                    objectMapper.writeValueAsString(params),
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
        } catch (RuntimeException exception) {
            throw new DataSaveException("生成通知发送 Preview 快照失败", exception);
        }
    }

    /**
     * 查询或获取目标数据（{@code readSnapshot}）。
     */
    private NotificationControlledSendFrom readSnapshot(Map<String, Object> snapshot) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(snapshot), NotificationControlledSendFrom.class);
        } catch (RuntimeException exception) {
            throw new DataSaveException("读取通知发送 Preview 快照失败", exception);
        }
    }

    /**
     * 处理内部业务逻辑（{@code verifyToken}）。
     */
    private void verifyToken(NotificationSendPreviewEntity entity, NotificationControlledSendApplyFrom params) {
        if (!StringUtils.hasText(params.getPreviewToken())
                || !MessageDigest.isEqual(
                        entity.getPreviewTokenHash().getBytes(StandardCharsets.US_ASCII),
                        hash(params.getPreviewToken()).getBytes(StandardCharsets.US_ASCII))) {
            throw new DataException("通知发送 Preview token 无效");
        }
    }

    /**
     * 更新或推进目标状态（{@code markExpired}）。
     */
    private void markExpired(NotificationSendPreviewEntity entity) {
        if (!NotificationPreviewStatus.EXPIRED.name().equals(entity.getStatus())) {
            entity.setStatus(NotificationPreviewStatus.EXPIRED.name());
            previewMapper.updateById(entity);
        }
    }

    /**
     * 转换、解析或规范化数据（{@code token}）。
     */
    private String token() {
        var bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 判断条件是否满足（{@code hash}）。
     */
    private String hash(String value) {
        return SHA256Utils.hash(value);
    }

    /**
     * 判断条件是否满足（{@code hash}）。
     */
    private String hash(List<String> values) {
        return hash(String.join("|", values));
    }

    private record PreparedRequest(String templateGroupCode,
                                   Map<NotificationChannel, NotificationControlledSendTemplateVO> templates,
                                   NotificationControlledSendFrom request) {

        private PreparedRequest(String templateGroupCode,
                                Map<NotificationChannel, NotificationControlledSendTemplateVO> templates) {
            this(templateGroupCode, templates, null);
        }
    }

    private record Evaluation(List<UUID> candidateUserIds, int candidateUserCount, int eligibleTaskCount,
                              int skippedTaskCount, Map<String, Integer> skippedCounts,
                              List<NotificationControlledSendSkippedDetailVO> skippedDetails,
                              Map<NotificationChannel, NotificationChannelAvailability> channelAvailability,
                              List<NotificationControlledSendSampleVO> samples, String resolutionHash) {
    }
}
