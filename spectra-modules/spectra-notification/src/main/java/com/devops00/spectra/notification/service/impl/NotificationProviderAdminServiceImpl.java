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
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderConfigDocument;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderConfiguration;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderHealthState;
import com.devops00.spectra.notification.javabean.from.NotificationProviderSaveFrom;
import com.devops00.spectra.notification.javabean.vo.NotificationProviderVO;
import com.devops00.spectra.notification.service.NotificationProviderAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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

    /** 阿里云短信默认端点。 */
    private static final String ALIYUN_SMS_ENDPOINT = "dysmsapi.aliyuncs.com";

    /** 阿里云短信默认地域。 */
    private static final String ALIYUN_SMS_REGION = "cn-hangzhou";

    /** 腾讯云短信默认端点。 */
    private static final String TENCENT_SMS_ENDPOINT = "sms.tencentcloudapi.com";

    /** 腾讯云短信默认地域。 */
    private static final String TENCENT_SMS_REGION = "ap-guangzhou";

    /** 默认 SMTP 端口。 */
    private static final int DEFAULT_SMTP_PORT = 587;

    /** 支持的 Provider 类型。 */
    private static final List<String> SUPPORTED_PROVIDER_TYPES = List.of(
            "ALIYUN_SMS", "TENCENT_SMS", "SMTP", "HTTP_JSON", "MOCK");

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

    /** 用户时区时间转换器。 */
    private final TimeMapper timeMapper;

    /**
     * 公共运行时配置写入端口；精简测试上下文可以不提供。
     */
    private SystemConfigValueWriter valueWriter;

    /**
     * 更新或推进目标状态（{@code setValueWriter}）。
     */
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
                    .state(NotificationProviderHealthState.HEALTHY.name())
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
                .state(state.name())
                .enabled(document.enabled())
                .reason(resolveReason(document, secretConfigured, secretUsable, state))
                .endpoint(document.endpoint())
                .port(document.port())
                .region(document.region())
                .credentialId(document.credentialId())
                .appId(document.appId())
                .signName(document.signName())
                .senderAddress(document.senderAddress())
                .senderName(document.senderName())
                .sslEnabled(document.sslEnabled())
                .starttlsEnabled(document.starttlsEnabled())
                .timeoutMs(document.timeoutMs())
                .rateLimitPerSecond(document.rateLimitPerSecond())
                .maxAttempts(document.maxAttempts())
                .templateCode(document.templateCode())
                .templateParameterOrder(document.templateParameterOrder())
                .secretConfigured(secretConfigured)
                .secretKeyId(document.secretKeyId())
                .updatedAt(timeMapper.toLocalDateTime(document.updatedAt()))
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
            return new NotificationProviderConfiguration(channel, "IN_APP", true, "", 0, null, null, null, null,
                    null, null, false, false, 0, 0, 1, null, null, null, null, null);
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
                document.endpoint(), document.port(), document.region(), document.credentialId(), document.appId(),
                document.signName(), document.senderAddress(), document.senderName(), document.sslEnabled(),
                document.starttlsEnabled(), document.timeoutMs(), document.rateLimitPerSecond(),
                document.maxAttempts(), document.templateCode(), document.templateParameterOrder(), secret,
                document.secretKeyId(), document.updatedAt());
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
        if (!SUPPORTED_PROVIDER_TYPES.contains(providerType)) {
            throw new DataSaveException("不支持的 Provider 类型");
        }
        var endpoint = normalize(params.getEndpoint());
        if ("ALIYUN_SMS".equals(providerType) && !StringUtils.hasText(endpoint)) {
            endpoint = ALIYUN_SMS_ENDPOINT;
        } else if ("TENCENT_SMS".equals(providerType) && !StringUtils.hasText(endpoint)) {
            endpoint = TENCENT_SMS_ENDPOINT;
        }
        var port = normalizePort(providerType, params.getPort(), params.isSslEnabled());
        var region = normalize(params.getRegion());
        if ("ALIYUN_SMS".equals(providerType) && !StringUtils.hasText(region)) {
            region = ALIYUN_SMS_REGION;
        } else if ("TENCENT_SMS".equals(providerType) && !StringUtils.hasText(region)) {
            region = TENCENT_SMS_REGION;
        }
        var credentialId = normalize(params.getCredentialId());
        var appId = normalize(params.getAppId());
        var signName = normalize(params.getSignName());
        var senderAddress = normalize(params.getSenderAddress());
        var senderName = normalize(params.getSenderName());
        var templateCode = normalize(params.getTemplateCode());
        var templateParameterOrder = normalize(params.getTemplateParameterOrder());
        var sslEnabled = params.isSslEnabled();
        var starttlsEnabled = params.isStarttlsEnabled();
        var timeoutMs = params.getTimeoutMs();
        var rateLimitPerSecond = params.getRateLimitPerSecond();
        var maxAttempts = params.getMaxAttempts();
        if ("SMTP".equals(providerType) && params.isSslEnabled() && params.isStarttlsEnabled()) {
            throw new DataSaveException("SMTP 不能同时启用隐式 SSL 和 STARTTLS");
        }
        normalizeProviderFields(providerType, endpoint, credentialId, appId, signName, senderAddress, templateCode);
        if ("MOCK".equals(providerType)) {
            endpoint = "";
            port = 0;
            region = null;
            credentialId = null;
            appId = null;
            signName = null;
            senderAddress = null;
            senderName = null;
            sslEnabled = false;
            starttlsEnabled = false;
            timeoutMs = 0;
            rateLimitPerSecond = 0;
            maxAttempts = 1;
            templateCode = null;
            templateParameterOrder = null;
        }
        var current = readDocument(channel);
        var secretKeyId = current.secretKeyId();
        var existingSecretCiphertext = valueProvider.find(secretKey(channel)).orElse(null);
        var providerTypeChanged = !providerType.equals(current.providerType());
        if (params.isClearSecret() || providerTypeChanged) {
            secretKeyId = null;
            if (StringUtils.hasText(existingSecretCiphertext)) {
                write(secretKey(channel), "", "通知" + channel.name() + " Provider Secret");
            }
        }
        if (!params.isClearSecret() && params.getSecret() != null && !params.getSecret().isBlank()) {
            secretKeyId = channel.name().toLowerCase() + "-" + UUID.randomUUID();
            write(secretKey(channel), payloadProtector.protectSecret(params.getSecret().trim()),
                    "通知" + channel.name() + " Provider Secret（AES-GCM 密文）");
        }
        var document = new NotificationProviderConfigDocument(providerType, params.isEnabled(), endpoint,
                port, region, credentialId, appId, signName, senderAddress, senderName, sslEnabled, starttlsEnabled,
                timeoutMs, rateLimitPerSecond, maxAttempts, templateCode, templateParameterOrder, secretKeyId,
                Instant.now());
        try {
            write(configKey(channel), objectMapper.writeValueAsString(document), "通知" + channel.name() + " Provider 配置");
        } catch (JacksonException exception) {
            throw new DataSaveException("Provider 配置序列化失败", exception);
        }
        return get(channel);
    }

    /**
     * 读取并校验非敏感配置文档。
     */
    private NotificationProviderConfigDocument readDocument(NotificationChannel channel) {
        var value = valueProvider.find(configKey(channel)).orElse(null);
        if (value == null || value.isBlank()) {
            return new NotificationProviderConfigDocument(null, false, "", 0, null, null, null, null, null, null,
                    false, false, DEFAULT_TIMEOUT_MS, DEFAULT_RATE_LIMIT, DEFAULT_MAX_ATTEMPTS, null, null, null,
                    null);
        }
        try {
            var document = objectMapper.readValue(value, NotificationProviderConfigDocument.class);
            var providerType = normalize(document.providerType());
            var mock = "MOCK".equals(providerType);
            return new NotificationProviderConfigDocument(providerType, document.enabled(),
                    mock ? "" : normalize(document.endpoint()), mock
                            ? 0
                            : normalizePort(providerType, document.port(),
                                    document.sslEnabled()),
                    normalize(document.region()), normalize(document.credentialId()),
                    normalize(document.appId()), normalize(document.signName()), normalize(document.senderAddress()),
                    normalize(document.senderName()), document.sslEnabled(), document.starttlsEnabled(),
                    mock ? 0 : normalizeTimeout(document.timeoutMs()), mock ? 0 : normalizeRate(document.rateLimitPerSecond()),
                    mock ? 1 : normalizeAttempts(document.maxAttempts()),
                    normalize(document.templateCode()), normalize(document.templateParameterOrder()),
                    normalize(document.secretKeyId()), document.updatedAt());
        } catch (RuntimeException exception) {
            return new NotificationProviderConfigDocument("INVALID", false, "", 0, null, null, null, null, null,
                    null, false, false, DEFAULT_TIMEOUT_MS, DEFAULT_RATE_LIMIT, DEFAULT_MAX_ATTEMPTS, null, null,
                    null, null);
        }
    }

    /**
     * 判断配置状态；没有健康检查结果时不报告 HEALTHY。
     */
    private NotificationProviderHealthState resolveState(
                                                         NotificationProviderConfigDocument document, boolean secretUsable) {
        if (document.providerType() == null || document.providerType().isBlank()) {
            return NotificationProviderHealthState.NOT_CONFIGURED;
        }
        if ("INVALID".equals(document.providerType())
                || !SUPPORTED_PROVIDER_TYPES.contains(document.providerType())
                || !isConfigurationComplete(document)
                || (requiresSecret(document.providerType()) && !secretUsable)) {
            return NotificationProviderHealthState.BLOCKED;
        }
        if (!document.enabled()) {
            return NotificationProviderHealthState.DISABLED;
        }
        return NotificationProviderHealthState.UNHEALTHY;
    }

    /**
     * 生成脱敏状态原因。
     */
    private String resolveReason(NotificationProviderConfigDocument document, boolean secretConfigured,
                                 boolean secretUsable, NotificationProviderHealthState state) {
        if (state == NotificationProviderHealthState.BLOCKED) {
            if (!secretConfigured && requiresSecret(document.providerType())) {
                return "SECRET_NOT_CONFIGURED";
            }
            if (!secretUsable && requiresSecret(document.providerType())) {
                return "SECRET_UNAVAILABLE";
            }
            return "CONFIG_INVALID";
        }
        if (state == NotificationProviderHealthState.DISABLED) {
            return "DISABLED_BY_CONFIGURATION";
        }
        if (state == NotificationProviderHealthState.UNHEALTHY) {
            return "HEALTH_CHECK_REQUIRED";
        }
        return state.name();
    }

    /**
     * 判断条件是否满足（{@code hasSecret}）。
     */
    private boolean hasSecret(NotificationProviderConfigDocument document, String ciphertext) {
        return document.secretKeyId() != null
                && !document.secretKeyId().isBlank()
                && ciphertext != null
                && !ciphertext.isBlank();
    }

    /**
     * 判断条件是否满足（{@code canDecrypt}）。
     */
    private boolean canDecrypt(String ciphertext) {
        try {
            payloadProtector.unprotectSecret(ciphertext);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    /**
     * 判断条件是否满足（{@code isHttpEndpoint}）。
     */
    private boolean isHttpEndpoint(String endpoint) {
        try {
            var uri = URI.create(endpoint);
            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    /**
     * 执行内部处理逻辑（{@code write}）。
     */
    private void write(String key, String value, String remarks) {
        if (valueWriter == null) {
            throw new DataSaveException("系统配置写入服务不可用");
        }
        valueWriter.upsert(key, value, ConfiguredValueType.TEXT, remarks);
    }

    /**
     * 处理内部业务逻辑（{@code configKey}）。
     */
    private String configKey(NotificationChannel channel) {
        return CONFIG_KEY_PREFIX + channel.name().toLowerCase();
    }

    /**
     * 处理内部业务逻辑（{@code secretKey}）。
     */
    private String secretKey(NotificationChannel channel) {
        return configKey(channel) + SECRET_KEY_SUFFIX;
    }

    /**
     * 转换、解析或规范化数据（{@code normalize}）。
     */
    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * 转换、解析或规范化数据（{@code normalizeTimeout}）。
     */
    private int normalizeTimeout(int value) {
        return value >= 100 && value <= 30_000 ? value : DEFAULT_TIMEOUT_MS;
    }

    /**
     * 转换、解析或规范化数据（{@code normalizeRate}）。
     */
    private int normalizeRate(int value) {
        return value > 0 && value <= 10_000 ? value : DEFAULT_RATE_LIMIT;
    }

    /**
     * 转换、解析或规范化数据（{@code normalizeAttempts}）。
     */
    private int normalizeAttempts(int value) {
        return value > 0 && value <= 5 ? value : DEFAULT_MAX_ATTEMPTS;
    }

    /**
     * 转换、解析或规范化数据（{@code normalizePort}）。
     */
    private int normalizePort(String providerType, int port, boolean sslEnabled) {
        if ("SMTP".equalsIgnoreCase(providerType)) {
            return port > 0 && port <= 65_535 ? port : (sslEnabled ? 465 : DEFAULT_SMTP_PORT);
        }
        return port > 0 && port <= 65_535 ? port : 0;
    }

    /**
     * 转换、解析或规范化数据（{@code normalizeProviderFields}）。
     */
    private void normalizeProviderFields(String providerType, String endpoint, String credentialId, String appId,
                                         String signName, String senderAddress, String templateCode) {
        switch (providerType) {
            case "ALIYUN_SMS" -> {
                if (!StringUtils.hasText(credentialId)
                        || !StringUtils.hasText(signName)
                        || !StringUtils.hasText(templateCode)) {
                    throw new DataSaveException("阿里云短信必须配置 AccessKey ID、短信签名和模板编码");
                }
            }
            case "TENCENT_SMS" -> {
                if (!StringUtils.hasText(credentialId)
                        || !StringUtils.hasText(appId)
                        || !StringUtils.hasText(signName)
                        || !StringUtils.hasText(templateCode)) {
                    throw new DataSaveException("腾讯云短信必须配置 SecretId、SDK AppID、短信签名和模板 ID");
                }
            }
            case "SMTP" -> {
                if (!StringUtils.hasText(endpoint)
                        || !StringUtils.hasText(credentialId)
                        || !StringUtils.hasText(senderAddress)) {
                    throw new DataSaveException("SMTP 必须配置主机、用户名和发件地址");
                }
            }
            case "HTTP_JSON" -> {
                if (!isHttpEndpoint(endpoint)) {
                    throw new DataSaveException("HTTP_JSON Provider 必须配置合法的 HTTP 或 HTTPS 端点");
                }
            }
            case "MOCK" -> {
                // 内置模拟服务不需要任何第三方配置。
            }
            default -> throw new DataSaveException("不支持的 Provider 类型");
        }
    }

    /**
     * 判断条件是否满足（{@code isConfigurationComplete}）。
     */
    private boolean isConfigurationComplete(NotificationProviderConfigDocument document) {
        return switch (document.providerType()) {
            case "ALIYUN_SMS" -> StringUtils.hasText(document.credentialId())
                    && StringUtils.hasText(document.signName())
                    && StringUtils.hasText(document.templateCode());
            case "TENCENT_SMS" -> StringUtils.hasText(document.credentialId())
                    && StringUtils.hasText(document.appId())
                    && StringUtils.hasText(document.signName())
                    && StringUtils.hasText(document.templateCode());
            case "SMTP" -> isSmtpEndpoint(document.endpoint())
                    && document.port() > 0
                    && StringUtils.hasText(document.credentialId())
                    && StringUtils.hasText(document.senderAddress())
                    && !(document.sslEnabled() && document.starttlsEnabled());
            case "HTTP_JSON" -> isHttpEndpoint(document.endpoint());
            case "MOCK" -> true;
            default -> false;
        };
    }

    /**
     * 校验并确保数据满足当前约束（{@code requiresSecret}）。
     */
    private boolean requiresSecret(String providerType) {
        return !"MOCK".equals(providerType) && providerType != null && !providerType.isBlank();
    }

    /**
     * 判断条件是否满足（{@code isSmtpEndpoint}）。
     */
    private boolean isSmtpEndpoint(String endpoint) {
        return StringUtils.hasText(endpoint) && !endpoint.contains("/") && !endpoint.contains("\\");
    }
}
