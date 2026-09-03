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

package com.devops00.spectra.core.notification.provider.impl;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.core.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.core.notification.javabean.domain.ChannelSendResult;
import com.devops00.spectra.core.notification.javabean.domain.NotificationProviderConfiguration;
import com.devops00.spectra.core.notification.javabean.domain.NotificationProviderHealth;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.core.notification.provider.NotificationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 通用 JSON HTTP Provider；实际供应商只需通过配置提供标准端点，不把 SDK 侵入通知 Worker。
 *
 * <p>请求体固定包含 {@code channel}、{@code recipient}、{@code title}、{@code content}、
 * {@code taskId} 和 {@code templateCode}；响应只读取状态和消息 ID，不保存原始响应正文。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Component
@RequiredArgsConstructor
public class HttpJsonNotificationProvider implements NotificationProvider {

    /**
     * Provider 类型编码。
     */
    private static final String CODE = "HTTP_JSON";

    /**
     * 请求体字段名称。
     */
    private static final String MESSAGE_ID = "messageId";

    /**
     * 地址和 Secret 解密器。
     */
    private final NotificationPayloadProtector payloadProtector;

    /**
     * JSON 序列化器。
     */
    private final ObjectMapper objectMapper;

    /**
     * 返回通用 HTTP Provider 编码。
     */
    @Override
    public String code() {
        return CODE;
    }

    /**
     * 通用 JSON Provider 支持短信和邮件两个外部渠道。
     */
    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.SMS || channel == NotificationChannel.EMAIL;
    }

    /**
     * 通过 GET 端点检查供应商是否可达；HTTP 4xx/5xx 均视为不健康。
     */
    @Override
    public NotificationProviderHealth health(NotificationProviderConfiguration configuration) {
        var checkedAt = Instant.now();
        if (!isUsable(configuration)) {
            return NotificationProviderHealth.blocked("PROVIDER_CONFIGURATION_INVALID", checkedAt);
        }
        try {
            var request = authorizedRequest(configuration)
                    .GET()
                    .build();
            var response = client(configuration).send(request, HttpResponse.BodyHandlers.discarding());
            var healthy = response.statusCode() >= 200 && response.statusCode() < 400;
            var reason = healthy ? "HEALTH_CHECK_OK" : "HEALTH_CHECK_HTTP_" + response.statusCode();
            return healthy ? NotificationProviderHealth.healthy(reason, checkedAt) : NotificationProviderHealth.unhealthy(reason, checkedAt);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return NotificationProviderHealth.unhealthy("HEALTH_CHECK_INTERRUPTED", checkedAt);
        } catch (IOException | RuntimeException exception) {
            return NotificationProviderHealth.unhealthy("HEALTH_CHECK_UNAVAILABLE", checkedAt);
        }
    }

    /**
     * 向供应商发送标准 JSON 请求，并将响应映射为通知域状态。
     */
    @Override
    public ChannelSendResult send(NotificationTaskEntity task, NotificationProviderConfiguration configuration) {
        if (!isUsable(configuration)) {
            return ChannelSendResult.blocked(CODE, null, "PROVIDER_CONFIGURATION_INVALID");
        }
        final String recipient;
        try {
            recipient = payloadProtector.unprotectAddress(task.getRecipientCiphertext());
        } catch (RuntimeException exception) {
            return ChannelSendResult.blocked(CODE, null, "RECIPIENT_ADDRESS_UNAVAILABLE");
        }
        var body = new LinkedHashMap<String, Object>();
        body.put("channel", task.getChannel());
        body.put("recipient", recipient);
        body.put("title", task.getTitle());
        body.put("content", task.getContent());
        body.put("taskId", task.getId() == null ? null : task.getId().toString());
        body.put("templateCode", configuration.templateCode());
        try {
            var request = authorizedRequest(configuration)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .header("Content-Type", "application/json")
                    .build();
            var response = client(configuration).send(request, HttpResponse.BodyHandlers.ofString());
            return mapResponse(response.statusCode(), response.body());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return ChannelSendResult.unknown(CODE, null, "PROVIDER_REQUEST_INTERRUPTED");
        } catch (IOException | RuntimeException exception) {
            return ChannelSendResult.unknown(CODE, null, "PROVIDER_REQUEST_UNAVAILABLE");
        }
    }

    /**
     * 将供应商响应映射为脱敏的标准结果。
     */
    private ChannelSendResult mapResponse(int statusCode, String responseBody) {
        if (statusCode == 429) {
            return ChannelSendResult.failed("RATE_LIMITED", null, "PROVIDER_RATE_LIMITED");
        }
        if (statusCode >= 500) {
            return ChannelSendResult.unknown(CODE, null, "PROVIDER_SERVER_ERROR");
        }
        if (statusCode < 200 || statusCode >= 300) {
            return ChannelSendResult.failed("PROVIDER_REJECTED", null, "PROVIDER_HTTP_REJECTED");
        }
        var response = readResponse(responseBody);
        var status = response.get("status") == null ? "SENT" : String.valueOf(response.get("status")).toUpperCase();
        if (!Map.of("SENT", true, "ACCEPTED", true).containsKey(status)) {
            if ("UNKNOWN".equals(status)) {
                return ChannelSendResult.unknown(CODE, messageId(response), "PROVIDER_UNKNOWN_RESULT");
            }
            return ChannelSendResult.failed("PROVIDER_REJECTED", messageId(response), "PROVIDER_REJECTED");
        }
        return ChannelSendResult.sent(CODE, messageId(response), "PROVIDER_ACCEPTED");
    }

    /**
     * 只读取白名单状态和消息 ID。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> readResponse(String responseBody) {
        if (!StrUtils.isNotBlank(responseBody)) {
            return Map.of();
        }
        try {
            Map<?, ?> map = objectMapper.readValue(responseBody, Map.class);
            return map == null ? Map.of() : (Map<String, Object>) map;
        } catch (RuntimeException exception) {
            return Map.of();
        }
    }

    /**
     * 处理内部业务逻辑（{@code messageId}）。
     */
    private String messageId(Map<String, Object> response) {
        var value = response.get(MESSAGE_ID);
        return value == null ? null : String.valueOf(value).substring(0, Math.min(200, String.valueOf(value).length()));
    }

    /**
     * 处理内部业务逻辑（{@code authorizedRequest}）。
     */
    private HttpRequest.Builder authorizedRequest(NotificationProviderConfiguration configuration) {
        var builder = HttpRequest.newBuilder(URI.create(configuration.endpoint()))
                .timeout(Duration.ofMillis(configuration.timeoutMs()));
        if (StrUtils.isNotBlank(configuration.secret())) {
            builder.header("Authorization", "Bearer " + configuration.secret());
        }
        return builder;
    }

    /**
     * 处理内部业务逻辑（{@code client}）。
     */
    private HttpClient client(NotificationProviderConfiguration configuration) {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(configuration.timeoutMs()))
                .build();
    }

    /**
     * 判断条件是否满足（{@code isUsable}）。
     */
    private boolean isUsable(NotificationProviderConfiguration configuration) {
        return configuration != null
                && configuration.enabled()
                && CODE.equals(configuration.providerType())
                && StrUtils.isNotBlank(configuration.endpoint())
                && StrUtils.isNotBlank(configuration.secret())
                && configuration.timeoutMs() >= 100;
    }
}
