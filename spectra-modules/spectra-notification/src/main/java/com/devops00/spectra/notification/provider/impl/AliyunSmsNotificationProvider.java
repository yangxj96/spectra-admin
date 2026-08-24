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

package com.devops00.spectra.notification.provider.impl;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.notification.javabean.domain.ChannelSendResult;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderConfiguration;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderHealth;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.provider.NotificationProvider;
import com.devops00.spectra.notification.provider.NotificationTaskMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 阿里云短信 Provider；使用官方 SMS OpenAPI 的 RPC 签名方式，不把 AccessKey Secret 写入普通配置。
 */
@Component
@RequiredArgsConstructor
public class AliyunSmsNotificationProvider implements NotificationProvider {

    private static final String CODE = "ALIYUN_SMS";
    private static final String DEFAULT_ENDPOINT = "dysmsapi.aliyuncs.com";
    private static final String ACTION_SEND = "SendSms";
    private static final String ACTION_HEALTH = "QuerySendDetails";
    private static final DateTimeFormatter SEND_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter UTC_SECOND = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneOffset.UTC);

    private final NotificationPayloadProtector payloadProtector;
    private final ObjectMapper objectMapper;

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.SMS;
    }

    @Override
    public NotificationProviderHealth health(NotificationProviderConfiguration configuration) {
        var checkedAt = Instant.now();
        if (!usable(configuration)) {
            return NotificationProviderHealth.blocked("PROVIDER_CONFIGURATION_INVALID", checkedAt);
        }
        var parameters = new LinkedHashMap<String, String>();
        parameters.put("PhoneNumber", "13800138000");
        parameters.put("SendDate", LocalDate.now(ZoneOffset.UTC).format(SEND_DATE));
        parameters.put("PageSize", "1");
        parameters.put("CurrentPage", "1");
        var response = request(configuration, ACTION_HEALTH, parameters);
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            return NotificationProviderHealth.unhealthy("HEALTH_CHECK_HTTP_" + response.statusCode(), checkedAt);
        }
        var body = readJson(response.body());
        var resultCode = String.valueOf(body.getOrDefault("Code", ""));
        return "OK".equalsIgnoreCase(resultCode)
                ? NotificationProviderHealth.healthy("HEALTH_CHECK_OK", checkedAt)
                : NotificationProviderHealth.unhealthy("HEALTH_CHECK_PROVIDER_REJECTED", checkedAt);
    }

    @Override
    public ChannelSendResult send(NotificationTaskEntity task, NotificationProviderConfiguration configuration) {
        if (!usable(configuration)) {
            return ChannelSendResult.blocked(CODE, null, "PROVIDER_CONFIGURATION_INVALID");
        }
        final String recipient;
        try {
            recipient = payloadProtector.unprotectAddress(task.getRecipientCiphertext());
        } catch (RuntimeException exception) {
            return ChannelSendResult.blocked(CODE, null, "RECIPIENT_ADDRESS_UNAVAILABLE");
        }
        try {
            var message = NotificationTaskMessage.resolve(task, payloadProtector);
            var parameters = new LinkedHashMap<String, String>();
            parameters.put("PhoneNumbers", recipient);
            parameters.put("SignName", configuration.signName());
            parameters.put("TemplateCode", message.providerTemplateCode() == null
                    ? configuration.templateCode()
                    : message.providerTemplateCode());
            parameters.put("TemplateParam", objectMapper.writeValueAsString(stringParameters(message.parameters())));
            var response = request(configuration, ACTION_SEND, parameters);
            if (response.statusCode() == 429) {
                return ChannelSendResult.failed("RATE_LIMITED", null, "PROVIDER_RATE_LIMITED");
            }
            if (response.statusCode() >= 500) {
                return ChannelSendResult.unknown(CODE, null, "PROVIDER_SERVER_ERROR");
            }
            var body = readJson(response.body());
            var resultCode = String.valueOf(body.getOrDefault("Code", ""));
            if (!"OK".equalsIgnoreCase(resultCode)) {
                return ChannelSendResult.failed("PROVIDER_REJECTED", text(body.get("BizId")),
                        "PROVIDER_REJECTED");
            }
            return ChannelSendResult.sent(CODE, text(body.get("BizId")), "PROVIDER_ACCEPTED");
        } catch (Exception exception) {
            return ChannelSendResult.unknown(CODE, null, "PROVIDER_REQUEST_UNAVAILABLE");
        }
    }

    /**
     * 处理内部业务逻辑（{@code request}）。
     */
    private HttpResponse<String> request(NotificationProviderConfiguration configuration, String action,
                                         Map<String, String> actionParameters) {
        try {
            var parameters = commonParameters(configuration, action);
            parameters.putAll(actionParameters);
            parameters.put("Signature", signature(parameters, configuration.secret()));
            var body = parameters.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                    .collect(Collectors.joining("&"));
            var request = HttpRequest.newBuilder(endpoint(configuration))
                    .timeout(Duration.ofMillis(configuration.timeoutMs()))
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            return client(configuration).send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Aliyun SMS request failed", exception);
        }
    }

    /**
     * 处理内部业务逻辑（{@code commonParameters}）。
     */
    private Map<String, String> commonParameters(NotificationProviderConfiguration configuration, String action) {
        var parameters = new LinkedHashMap<String, String>();
        parameters.put("AccessKeyId", configuration.credentialId());
        parameters.put("Action", action);
        parameters.put("Format", "JSON");
        parameters.put("SignatureMethod", "HMAC-SHA1");
        parameters.put("SignatureNonce", UUID.randomUUID().toString());
        parameters.put("SignatureVersion", "1.0");
        parameters.put("Timestamp", UTC_SECOND.format(Instant.now()));
        parameters.put("Version", "2017-05-25");
        return parameters;
    }

    /**
     * 处理内部业务逻辑（{@code signature}）。
     */
    private String signature(Map<String, String> parameters, String secret) {
        var canonicalized = parameters.entrySet()
                .stream()
                .filter(entry -> !"Signature".equals(entry.getKey()))
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
        var stringToSign = "POST&%2F&" + encode(canonicalized);
        return hmacSha1(secret + "&", stringToSign);
    }

    /**
     * 处理内部业务逻辑（{@code hmacSha1}）。
     */
    private String hmacSha1(String key, String value) {
        try {
            var mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            return Base64.getEncoder().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Aliyun SMS signature failed", exception);
        }
    }

    /**
     * 转换、解析或规范化数据（{@code encode}）。
     */
    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

    /**
     * 处理内部业务逻辑（{@code endpoint}）。
     */
    private URI endpoint(NotificationProviderConfiguration configuration) {
        var endpoint = configuration.endpoint();
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = DEFAULT_ENDPOINT;
        }
        return URI.create(endpoint.startsWith("http://") || endpoint.startsWith("https://")
                ? endpoint
                : "https://" + endpoint);
    }

    /**
     * 处理内部业务逻辑（{@code client}）。
     */
    private HttpClient client(NotificationProviderConfiguration configuration) {
        return HttpClient.newBuilder().connectTimeout(Duration.ofMillis(configuration.timeoutMs())).build();
    }

    /**
     * 查询或获取目标数据（{@code readJson}）。
     */
    private Map<String, Object> readJson(String body) {
        if (body == null || body.isBlank()) {
            return Map.of();
        }
        try {
            Map<?, ?> map = objectMapper.readValue(body, Map.class);
            return map == null
                    ? Map.of()
                    : map.entrySet()
                            .stream()
                            .collect(Collectors.toMap(entry -> String.valueOf(entry.getKey()), Map.Entry::getValue));
        } catch (RuntimeException exception) {
            return Map.of();
        }
    }

    /**
     * 处理内部业务逻辑（{@code stringParameters}）。
     */
    private Map<String, String> stringParameters(Map<String, Object> parameters) {
        return parameters.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue() == null ? "" : String.valueOf(entry.getValue()),
                        (first, ignored) -> first, LinkedHashMap::new));
    }

    /**
     * 处理内部业务逻辑（{@code text}）。
     */
    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 处理内部业务逻辑（{@code usable}）。
     */
    private boolean usable(NotificationProviderConfiguration configuration) {
        return configuration != null
                && configuration.enabled()
                && CODE.equals(configuration.providerType())
                && configuration.secret() != null
                && !configuration.secret().isBlank()
                && configuration.credentialId() != null
                && !configuration.credentialId().isBlank()
                && configuration.signName() != null
                && !configuration.signName().isBlank()
                && configuration.templateCode() != null
                && !configuration.templateCode().isBlank()
                && configuration.timeoutMs() >= 100;
    }
}
