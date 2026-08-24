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

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.utils.SHA256Utils;
import com.devops00.spectra.notification.javabean.domain.ChannelSendStatus;
import com.devops00.spectra.notification.javabean.domain.NotificationCallbackStatus;
import com.devops00.spectra.notification.javabean.domain.NotificationTaskStatus;
import com.devops00.spectra.notification.dispatch.NotificationRequestStatusUpdater;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderConfiguration;
import com.devops00.spectra.notification.javabean.entity.NotificationDeliveryEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.javabean.from.NotificationProviderCallbackFrom;
import com.devops00.spectra.notification.javabean.vo.NotificationProviderCallbackVO;
import com.devops00.spectra.notification.mapper.NotificationDeliveryMapper;
import com.devops00.spectra.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.notification.service.NotificationProviderAdminService;
import com.devops00.spectra.notification.service.NotificationProviderCallbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Provider 回执处理实现；只更新已存在的 Delivery，并以回调正文摘要实现重复回执幂等。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Service
@RequiredArgsConstructor
public class NotificationProviderCallbackServiceImpl implements NotificationProviderCallbackService {

    /**
     * 回调正文最大长度，避免匿名入口被大请求占用内存。
     */
    private static final int MAX_BODY_LENGTH = 32 * 1024;

    /**
     * 投递记录 Mapper。
     */
    private final NotificationDeliveryMapper deliveryMapper;

    /**
     * 投递任务 Mapper。
     */
    private final NotificationTaskMapper taskMapper;

    /**
     * 逻辑请求状态汇总器。
     */
    private final NotificationRequestStatusUpdater requestStatusUpdater;

    /**
     * Provider 配置读取服务。
     */
    private final NotificationProviderAdminService providerAdminService;

    /**
     * JSON 解析器。
     */
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public NotificationProviderCallbackVO handle(NotificationChannel channel, String signature, String body) {
        if (channel == null || channel == NotificationChannel.IN_APP) {
            throw new DataSaveException("外部 Provider 回执渠道不合法");
        }
        if (!StringUtils.hasText(body) || body.length() > MAX_BODY_LENGTH) {
            throw new DataSaveException("Provider 回执正文不合法");
        }
        var configuration = providerAdminService.resolve(channel);
        verifySignature(signature, body, configuration);
        var callback = parse(body);
        var providerMessageId = normalizeMessageId(callback.getMessageId());
        var resultStatus = normalizeStatus(callback.getStatus());
        var delivery = deliveryMapper.selectByProviderMessageId(configuration.providerType(), providerMessageId,
                channel.name());
        if (delivery == null) {
            throw new DataNotExistException("Provider 回执对应的投递不存在");
        }

        var eventDigest = digest(body);
        var summary = new LinkedHashMap<String, Object>();
        if (delivery.getResponseSummary() != null) {
            summary.putAll(delivery.getResponseSummary());
        }
        var previousDigest = summary.get("callback_event_digest");
        if (previousDigest != null
                && MessageDigest.isEqual(eventDigest.getBytes(StandardCharsets.US_ASCII),
                        String.valueOf(previousDigest).getBytes(StandardCharsets.US_ASCII))) {
            return new NotificationProviderCallbackVO(
                    NotificationCallbackStatus.DUPLICATE.name(), delivery.getResultStatus());
        }

        var receivedAt = Instant.now();
        var errorCode = safeErrorCode(callback.getErrorCode());
        summary.put("callback_event_digest", eventDigest);
        summary.put("callback_status", resultStatus.name());
        summary.put("callback_received_at", receivedAt.toString());
        if (errorCode != null) {
            summary.put("callback_error_code", errorCode);
        } else {
            summary.remove("callback_error_code");
        }
        delivery.setResultStatus(resultStatus.name());
        delivery.setCompletedAt(receivedAt);
        delivery.setErrorCode(resultStatus == ChannelSendStatus.FAILED ? errorCode : null);
        delivery.setErrorMessageSanitized(resultStatus == ChannelSendStatus.FAILED ? errorCode : null);
        delivery.setResponseSummary(summary);
        if (deliveryMapper.updateById(delivery) != 1) {
            throw new DataSaveException("更新 Provider 回执失败");
        }

        updateTask(delivery, resultStatus, errorCode);
        return new NotificationProviderCallbackVO(NotificationCallbackStatus.APPLIED.name(), resultStatus.name());
    }

    /**
     * 转换、解析或规范化数据（{@code parse}）。
     */
    private NotificationProviderCallbackFrom parse(String body) {
        try {
            var callback = objectMapper.readValue(body, NotificationProviderCallbackFrom.class);
            if (callback == null
                    || !StringUtils.hasText(callback.getMessageId())
                    || !StringUtils.hasText(callback.getStatus())) {
                throw new DataSaveException("Provider 回执字段不完整");
            }
            return callback;
        } catch (RuntimeException exception) {
            if (exception instanceof DataSaveException) {
                throw exception;
            }
            throw new DataSaveException("Provider 回执 JSON 不合法", exception);
        }
    }

    /**
     * 处理内部业务逻辑（{@code verifySignature}）。
     */
    private void verifySignature(String signature, String body, NotificationProviderConfiguration configuration) {
        if (configuration == null
                || !configuration.enabled()
                || !StringUtils.hasText(configuration.providerType())
                || !StringUtils.hasText(configuration.secret())) {
            throw new DataSaveException("Provider 回执验签配置不可用");
        }
        if (!StringUtils.hasText(signature)) {
            throw new DataSaveException("Provider 回执签名不能为空");
        }
        var expected = signature.trim().toLowerCase(Locale.ROOT);
        if (expected.startsWith("sha256=")) {
            expected = expected.substring("sha256=".length());
        }
        if (!expected.matches("[0-9a-f]{64}")) {
            throw new DataSaveException("Provider 回执签名格式不合法");
        }
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(configuration.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            var actual = HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
            if (!MessageDigest.isEqual(actual.getBytes(StandardCharsets.US_ASCII),
                    expected.getBytes(StandardCharsets.US_ASCII))) {
                throw new DataSaveException("Provider 回执签名校验失败");
            }
        } catch (DataSaveException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DataSaveException("Provider 回执签名校验失败", exception);
        }
    }

    /**
     * 转换、解析或规范化数据（{@code normalizeMessageId}）。
     */
    private String normalizeMessageId(String value) {
        var normalized = value == null ? null : value.trim();
        if (!StringUtils.hasText(normalized) || normalized.length() > 200) {
            throw new DataSaveException("Provider 消息 ID 不合法");
        }
        return normalized;
    }

    /**
     * 转换、解析或规范化数据（{@code normalizeStatus}）。
     */
    private ChannelSendStatus normalizeStatus(String value) {
        var normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ACCEPTED", "SENT", "DELIVERED" -> ChannelSendStatus.SENT;
            case "FAILED", "BOUNCED", "REJECTED" -> ChannelSendStatus.FAILED;
            case "UNKNOWN" -> ChannelSendStatus.UNKNOWN;
            default -> throw new DataSaveException("Provider 回执状态不支持");
        };
    }

    /**
     * 处理内部业务逻辑（{@code safeErrorCode}）。
     */
    private String safeErrorCode(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        var normalized = value.trim().toUpperCase(Locale.ROOT);
        return normalized.matches("[A-Z0-9_.:-]{1,100}") ? normalized : "PROVIDER_CALLBACK_ERROR";
    }

    /**
     * 转换、解析或规范化数据（{@code digest}）。
     */
    private String digest(String body) {
        return SHA256Utils.hash(body);
    }

    /**
     * 更新或推进目标状态（{@code updateTask}）。
     */
    private void updateTask(NotificationDeliveryEntity delivery, ChannelSendStatus resultStatus, String errorCode) {
        var task = taskMapper.selectById(delivery.getNotificationTaskId());
        if (task == null
                || List.of(NotificationTaskStatus.CANCELLED.name(), NotificationTaskStatus.EXPIRED.name())
                        .contains(task.getStatus())) {
            return;
        }
        taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getId, task.getId())
                .notIn(NotificationTaskEntity::getStatus,
                        List.of(NotificationTaskStatus.CANCELLED.name(), NotificationTaskStatus.EXPIRED.name()))
                .set(NotificationTaskEntity::getStatus, resultStatus.name())
                .set(NotificationTaskEntity::getLastErrorCode,
                        resultStatus == ChannelSendStatus.SENT ? null : errorCode)
                .set(NotificationTaskEntity::getLockedBy, null)
                .set(NotificationTaskEntity::getLockedAt, null));
        requestStatusUpdater.refresh(task.getNotificationRequestId());
    }
}
