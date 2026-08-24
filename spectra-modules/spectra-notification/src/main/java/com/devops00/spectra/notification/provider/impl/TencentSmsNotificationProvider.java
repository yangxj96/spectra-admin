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
import com.devops00.spectra.common.utils.SHA256Utils;
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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 腾讯云短信 Provider；使用官方 TC3-HMAC-SHA256 签名调用 SendSms。
 */
@Component
@RequiredArgsConstructor
public class TencentSmsNotificationProvider implements NotificationProvider {

    private static final String CODE = "TENCENT_SMS";
    private static final String SERVICE = "sms";
    private static final String VERSION = "2021-01-11";
    private static final String DEFAULT_ENDPOINT = "sms.tencentcloudapi.com";
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
        var response = request(configuration, "DescribeSmsTemplateList", Map.of("Limit", 1, "Offset", 0));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            return NotificationProviderHealth.unhealthy("HEALTH_CHECK_HTTP_" + response.statusCode(), checkedAt);
        }
        var body = readJson(response.body());
        if (!(body.get("Response") instanceof Map<?, ?>)) {
            return NotificationProviderHealth.unhealthy("HEALTH_CHECK_INVALID_RESPONSE", checkedAt);
        }
        return body.containsKey("Error")
                ? NotificationProviderHealth.unhealthy("HEALTH_CHECK_PROVIDER_REJECTED", checkedAt)
                : NotificationProviderHealth.healthy("HEALTH_CHECK_OK", checkedAt);
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
            var templateParameters = orderedParameters(message.parameters(), configuration.templateParameterOrder());
            var payload = new LinkedHashMap<String, Object>();
            payload.put("SmsSdkAppId", configuration.appId());
            payload.put("SignName", configuration.signName());
            payload.put("TemplateId", message.providerTemplateCode() == null
                    ? configuration.templateCode()
                    : message.providerTemplateCode());
            payload.put("TemplateParamSet", templateParameters);
            payload.put("PhoneNumberSet", List.of(recipient));
            var response = request(configuration, "SendSms", payload);
            if (response.statusCode() == 429) {
                return ChannelSendResult.failed("RATE_LIMITED", null, "PROVIDER_RATE_LIMITED");
            }
            if (response.statusCode() >= 500) {
                return ChannelSendResult.unknown(CODE, null, "PROVIDER_SERVER_ERROR");
            }
            var body = readJson(response.body());
            var responseBody = nestedResponse(body);
            var error = responseBody.get("Error");
            if (error != null) {
                return ChannelSendResult.failed("PROVIDER_REJECTED", text(responseBody.get("RequestId")),
                        "PROVIDER_REJECTED");
            }
            var sendStatusSet = responseBody.get("SendStatusSet");
            if (!(sendStatusSet instanceof List<?> statuses) || statuses.isEmpty()) {
                return ChannelSendResult.unknown(CODE, text(responseBody.get("RequestId")),
                        "PROVIDER_INVALID_RESPONSE");
            }
            var messageId = firstMessageId(sendStatusSet);
            if (statuses.getFirst() instanceof Map<?, ?> status
                    && !"Ok".equalsIgnoreCase(String.valueOf(status.get("Code")))) {
                return ChannelSendResult.failed("PROVIDER_REJECTED", messageId, "PROVIDER_REJECTED");
            }
            if (!(statuses.getFirst() instanceof Map<?, ?>)) {
                return ChannelSendResult.unknown(CODE, text(responseBody.get("RequestId")),
                        "PROVIDER_INVALID_RESPONSE");
            }
            return ChannelSendResult.sent(CODE, messageId, "PROVIDER_ACCEPTED");
        } catch (Exception exception) {
            return ChannelSendResult.unknown(CODE, null, "PROVIDER_REQUEST_UNAVAILABLE");
        }
    }

    /**
     * 处理内部业务逻辑（{@code request}）。
     */
    private HttpResponse<String> request(NotificationProviderConfiguration configuration, String action,
                                         Map<String, ?> payload) {
        try {
            var body = objectMapper.writeValueAsString(payload);
            var timestamp = Instant.now().getEpochSecond();
            var endpoint = endpoint(configuration);
            var host = endpoint.getHost();
            var canonicalHeaders = "content-type:application/json; charset=utf-8\n" + "host:" + host + "\n";
            var signedHeaders = "content-type;host";
            var canonicalRequest = "POST\n/\n\n" + canonicalHeaders + "\n" + signedHeaders + "\n"
                    + SHA256Utils.hash(body);
            var date = UTC_SECOND.format(Instant.ofEpochSecond(timestamp)).substring(0, 10);
            var credentialScope = date + "/" + SERVICE + "/tc3_request";
            var stringToSign = "TC3-HMAC-SHA256\n" + timestamp + "\n" + credentialScope + "\n"
                    + SHA256Utils.hash(canonicalRequest);
            var secretDate = hmac(("TC3" + configuration.secret()).getBytes(StandardCharsets.UTF_8), date);
            var secretService = hmac(secretDate, SERVICE);
            var secretSigning = hmac(secretService, "tc3_request");
            var signature = hex(hmac(secretSigning, stringToSign));
            var authorization = "TC3-HMAC-SHA256 Credential=" + configuration.credentialId() + "/"
                    + credentialScope + ", SignedHeaders=" + signedHeaders + ", Signature=" + signature;
            var request = HttpRequest.newBuilder(endpoint)
                    .timeout(Duration.ofMillis(configuration.timeoutMs()))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Host", host)
                    .header("X-TC-Action", action)
                    .header("X-TC-Version", VERSION)
                    .header("X-TC-Timestamp", String.valueOf(timestamp))
                    .header("X-TC-Region", configuration.region() == null ? "" : configuration.region())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            return client(configuration).send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Tencent SMS request failed", exception);
        }
    }

    /**
     * 处理内部业务逻辑（{@code orderedParameters}）。
     */
    private List<String> orderedParameters(Map<String, Object> parameters, String parameterOrder) {
        var values = new ArrayList<String>();
        if (parameterOrder != null && !parameterOrder.isBlank()) {
            for (var name : parameterOrder.split(",")) {
                var key = name.trim();
                if (!key.isEmpty()) {
                    values.add(String.valueOf(parameters.getOrDefault(key, "")));
                }
            }
            return values;
        }
        parameters.values().forEach(value -> values.add(value == null ? "" : String.valueOf(value)));
        return values;
    }

    /**
     * 查询或获取目标数据（{@code readJson}）。
     */
    @SuppressWarnings("unchecked")
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
     * 处理内部业务逻辑（{@code nestedResponse}）。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> nestedResponse(Map<String, Object> body) {
        return body.get("Response") instanceof Map<?, ?> response
                ? (Map<String, Object>) response
                : Map.of();
    }

    /**
     * 处理内部业务逻辑（{@code firstMessageId}）。
     */
    @SuppressWarnings("unchecked")
    private String firstMessageId(Object sendStatusSet) {
        if (sendStatusSet instanceof List<?> statuses
                && !statuses.isEmpty()
                && statuses.getFirst() instanceof Map<?, ?> status) {
            return text(status.get("SerialNo"));
        }
        return null;
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
     * 处理内部业务逻辑（{@code hmac}）。
     */
    private byte[] hmac(byte[] key, String value) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Tencent SMS signature failed", exception);
        }
    }

    /**
     * 处理内部业务逻辑（{@code hex}）。
     */
    private String hex(byte[] bytes) {
        var result = new StringBuilder(bytes.length * 2);
        for (var item : bytes) {
            result.append(String.format("%02x", item));
        }
        return result.toString();
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
                && configuration.appId() != null
                && !configuration.appId().isBlank()
                && configuration.signName() != null
                && !configuration.signName().isBlank()
                && configuration.templateCode() != null
                && !configuration.templateCode().isBlank()
                && configuration.timeoutMs() >= 100;
    }
}
