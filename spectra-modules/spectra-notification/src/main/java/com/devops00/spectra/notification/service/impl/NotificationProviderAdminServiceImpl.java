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

import com.devops00.spectra.common.config.SystemConfigValueProvider;
import com.devops00.spectra.common.config.SystemConfigValueWriter;
import com.devops00.spectra.common.constant.ConfiguredValueType;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderConfigDocument;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderConfiguration;
import com.devops00.spectra.notification.javabean.from.NotificationProviderSaveFrom;
import com.devops00.spectra.notification.javabean.vo.NotificationProviderVO;
import com.devops00.spectra.notification.service.NotificationProviderAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 基于公共运行时配置的 Provider 管理实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Service
@RequiredArgsConstructor
public class NotificationProviderAdminServiceImpl implements NotificationProviderAdminService {

    /**
     * Provider 配置键前缀。
     */
    private static final String CONFIG_KEY_PREFIX = "notification.provider.";

    /**
     * Provider Secret 密文键后缀。
     */
    private static final String SECRET_KEY_SUFFIX = ".secret";

    /**
     * 默认请求超时。
     */
    private static final int DEFAULT_TIMEOUT_MS = 5_000;

    /**
     * 默认限流值。
     */
    private static final int DEFAULT_RATE_LIMIT = 10;

    /**
     * 默认最大尝试次数。
     */
    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    /**
     * 公共运行时配置读取端口。
     */
    private final SystemConfigValueProvider valueProvider;

    /**
     * Provider Secret 保护器。
     */
    private final NotificationPayloadProtector payloadProtector;

    /**
     * JSON 序列化器。
     */
    private final ObjectMapper objectMapper;

    /**
     * 公共运行时配置写入端口；精简测试上下文可以不提供。
     */
    private SystemConfigValueWriter valueWriter;

    @Autowired(required = false)
    public void setValueWriter(SystemConfigValueWriter valueWriter) {
        this.valueWriter = valueWriter;
    }

    /**
     * 查询 SMS、EMAIL、IN_APP 三个渠道的脱敏配置。
     */
    @Override
    public List<NotificationProviderVO> list() {
        return Arrays.stream(NotificationChannel.values()).map(this::get).toList();
    }

    /**
     * 查询指定渠道配置。
     */
    @Override
    public NotificationProviderVO get(NotificationChannel channel) {
        if (channel == null) {
            throw new DataSaveException("通知渠道不能为空");
        }
        if (channel == NotificationChannel.IN_APP) {
            return NotificationProviderVO.builder()
                    .channel(channel.name())
                    .providerType("IN_APP")
                    .state("HEALTHY")
                    .enabled(true)
                    .reason("IN_APP_READY")
                    .timeoutMs(0)
                    .rateLimitPerSecond(0)
                    .maxAttempts(1)
                    .secretConfigured(false)
                    .build();
        }
        var document = readDocument(channel);
        var secretCiphertext = valueProvider.find(secretKey(channel)).orElse(null);
        var secretConfigured = hasSecret(document, secretCiphertext);
        var secretUsable = secretConfigured && canDecrypt(secretCiphertext);
        var state = resolveState(document, secretUsable);
        return NotificationProviderVO.builder()
                .channel(channel.name())
                .providerType(document.providerType())
                .state(state)
                .enabled(document.enabled())
                .reason(resolveReason(document, secretConfigured, secretUsable, state))
                .endpoint(document.endpoint())
                .timeoutMs(document.timeoutMs())
                .rateLimitPerSecond(document.rateLimitPerSecond())
                .maxAttempts(document.maxAttempts())
                .templateCode(document.templateCode())
                .secretConfigured(secretConfigured)
                .secretKeyId(document.secretKeyId())
                .updatedAt(document.updatedAt())
                .build();
    }

    /**
     * 解析 Provider 运行时配置；密钥不可解密时返回空 Secret 以触发 fail-closed。
     */
    @Override
    public NotificationProviderConfiguration resolve(NotificationChannel channel) {
        if (channel == null) {
            throw new DataSaveException("通知渠道不能为空");
        }
        if (channel == NotificationChannel.IN_APP) {
            return new NotificationProviderConfiguration(channel, "IN_APP", true, "", 0, 0, 1,
                    null, null, null, null);
        }
        var document = readDocument(channel);
        var ciphertext = valueProvider.find(secretKey(channel)).orElse(null);
        String secret = null;
        if (hasSecret(document, ciphertext)) {
            try {
                secret = payloadProtector.unprotectSecret(ciphertext);
            } catch (RuntimeException ignored) {
                // Secret 不可解密时必须以空值返回，让 Provider 明确阻断发送。
            }
        }
        return new NotificationProviderConfiguration(channel, document.providerType(), document.enabled(),
                document.endpoint(), document.timeoutMs(), document.rateLimitPerSecond(), document.maxAttempts(),
                document.templateCode(), secret, document.secretKeyId(), document.updatedAt());
    }

    /**
     * 保存指定渠道的非敏感配置和加密 Secret。
     */
    @Override
    @Transactional
    public NotificationProviderVO modify(NotificationChannel channel, NotificationProviderSaveFrom params) {
        if (channel == null || channel == NotificationChannel.IN_APP) {
            throw new DataSaveException("站内信不支持外部 Provider 配置");
        }
        if (params == null) {
            throw new DataSaveException("Provider 配置不能为空");
        }
        var providerType = normalize(params.getProviderType()).toUpperCase();
        if (!List.of("HTTP_JSON", "MOCK").contains(providerType)) {
            throw new DataSaveException("不支持的 Provider 类型");
        }
        var endpoint = normalize(params.getEndpoint());
        if ("HTTP_JSON".equals(providerType)) {
            validateEndpoint(endpoint);
        } else {
            endpoint = "";
        }
        var current = readDocument(channel);
        var secretKeyId = current.secretKeyId();
        if (params.isClearSecret()) {
            secretKeyId = null;
            write(secretKey(channel), null, "通知" + channel.name() + " Provider Secret");
        } else if (params.getSecret() != null && !params.getSecret().isBlank()) {
            secretKeyId = channel.name().toLowerCase() + "-" + UUID.randomUUID();
            write(secretKey(channel), payloadProtector.protectSecret(params.getSecret().trim()),
                    "通知" + channel.name() + " Provider Secret（AES-GCM 密文）");
        }
        var document = new NotificationProviderConfigDocument(providerType, params.isEnabled(), endpoint,
                params.getTimeoutMs(), params.getRateLimitPerSecond(), params.getMaxAttempts(),
                normalize(params.getTemplateCode()), secretKeyId, Instant.now());
        try {
            write(configKey(channel), objectMapper.writeValueAsString(document), "通知" + channel.name() + " Provider 配置");
        } catch (JacksonException exception) {
            throw new DataSaveException("Provider 配置序列化失败");
        }
        return get(channel);
    }

    /**
     * 读取并校验非敏感配置文档。
     */
    private NotificationProviderConfigDocument readDocument(NotificationChannel channel) {
        var value = valueProvider.find(configKey(channel)).orElse(null);
        if (value == null || value.isBlank()) {
            return new NotificationProviderConfigDocument(null, false, "", DEFAULT_TIMEOUT_MS,
                    DEFAULT_RATE_LIMIT, DEFAULT_MAX_ATTEMPTS, null, null, null);
        }
        try {
            var document = objectMapper.readValue(value, NotificationProviderConfigDocument.class);
            return new NotificationProviderConfigDocument(normalize(document.providerType()), document.enabled(),
                    normalize(document.endpoint()), normalizeTimeout(document.timeoutMs()),
                    normalizeRate(document.rateLimitPerSecond()), normalizeAttempts(document.maxAttempts()),
                    normalize(document.templateCode()), normalize(document.secretKeyId()), document.updatedAt());
        } catch (RuntimeException exception) {
            return new NotificationProviderConfigDocument("INVALID", false, "", DEFAULT_TIMEOUT_MS,
                    DEFAULT_RATE_LIMIT, DEFAULT_MAX_ATTEMPTS, null, null, null);
        }
    }

    /**
     * 判断配置状态；没有健康检查结果时不报告 HEALTHY。
     */
    private String resolveState(NotificationProviderConfigDocument document, boolean secretUsable) {
        if (document.providerType() == null || document.providerType().isBlank()) {
            return "NOT_CONFIGURED";
        }
        if ("INVALID".equals(document.providerType())
                || ("HTTP_JSON".equals(document.providerType())
                        && (!isHttpEndpoint(document.endpoint()) || !secretUsable))) {
            return "BLOCKED";
        }
        if (!document.enabled()) {
            return "DISABLED";
        }
        return "UNHEALTHY";
    }

    /**
     * 生成脱敏状态原因。
     */
    private String resolveReason(NotificationProviderConfigDocument document, boolean secretConfigured,
                                 boolean secretUsable, String state) {
        if ("BLOCKED".equals(state)) {
            if (!secretConfigured && "HTTP_JSON".equals(document.providerType())) {
                return "SECRET_NOT_CONFIGURED";
            }
            if (!secretUsable && "HTTP_JSON".equals(document.providerType())) {
                return "SECRET_UNAVAILABLE";
            }
            return "CONFIG_INVALID";
        }
        if ("DISABLED".equals(state)) {
            return "DISABLED_BY_CONFIGURATION";
        }
        if ("UNHEALTHY".equals(state)) {
            return "HEALTH_CHECK_REQUIRED";
        }
        return state;
    }

    private boolean hasSecret(NotificationProviderConfigDocument document, String ciphertext) {
        return document.secretKeyId() != null
                && !document.secretKeyId().isBlank()
                && ciphertext != null
                && !ciphertext.isBlank();
    }

    private boolean canDecrypt(String ciphertext) {
        try {
            payloadProtector.unprotectSecret(ciphertext);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    /**
     * 校验 HTTP Provider 端点。
     */
    private void validateEndpoint(String endpoint) {
        if (!isHttpEndpoint(endpoint)) {
            throw new DataSaveException("HTTP_JSON Provider 必须配置合法的 HTTP 或 HTTPS 端点");
        }
    }

    private boolean isHttpEndpoint(String endpoint) {
        try {
            var uri = URI.create(endpoint);
            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private void write(String key, String value, String remarks) {
        if (valueWriter == null) {
            throw new DataSaveException("系统配置写入服务不可用");
        }
        valueWriter.upsert(key, value, ConfiguredValueType.TEXT, remarks);
    }

    private String configKey(NotificationChannel channel) {
        return CONFIG_KEY_PREFIX + channel.name().toLowerCase();
    }

    private String secretKey(NotificationChannel channel) {
        return configKey(channel) + SECRET_KEY_SUFFIX;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private int normalizeTimeout(int value) {
        return value >= 100 && value <= 30_000 ? value : DEFAULT_TIMEOUT_MS;
    }

    private int normalizeRate(int value) {
        return value > 0 && value <= 10_000 ? value : DEFAULT_RATE_LIMIT;
    }

    private int normalizeAttempts(int value) {
        return value > 0 && value <= 5 ? value : DEFAULT_MAX_ATTEMPTS;
    }
}
