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

package com.devops00.spectra.core.notification.sender;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.core.notification.javabean.entity.NotificationSmsConfigEntity;
import com.devops00.spectra.core.notification.javabean.enums.SmsProviderType;
import com.devops00.spectra.core.notification.mapper.NotificationSmsConfigMapper;
import com.devops00.spectra.core.notification.properties.NotificationModuleProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.MessageDigest;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 短信同步发送器，支持阿里云、腾讯云和内置模拟发送。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsSender implements NotificationSender {

    private static final String ALIYUN_DEFAULT_ENDPOINT = "dysmsapi.aliyuncs.com";
    private static final String TENCENT_DEFAULT_ENDPOINT = "sms.tencentcloudapi.com";
    private static final String TENCENT_SERVICE = "sms";
    private static final String TENCENT_VERSION = "2021-01-11";
    private static final DateTimeFormatter UTC_SECOND = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneOffset.UTC);

    private final NotificationSmsConfigMapper configMapper;
    private final NotificationModuleProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }

    @Override
    public SendOutcome send(NotificationSendTarget target, RenderedMessage message) {
        var config = activeConfig();
        if (config == null) {
            return SendOutcome.failure("SMS", null, "SMS_CONFIG_NOT_FOUND", "未配置启用中的短信渠道");
        }
        if (!StringUtils.hasText(target.address())) {
            return SendOutcome.failure(config.getProvider(), config.getId(), "SMS_ADDRESS_MISSING", "收件手机号为空");
        }
        if (!properties.isEnabled()) {
            return SendOutcome.failure(config.getProvider(), config.getId(), "NOTIFICATION_DISABLED", "通知模块未启用");
        }
        if (properties.isForceMock() || SmsProviderType.MOCK.name().equals(config.getProvider())) {
            log.info("模拟短信发送: configId={}, recipient={}", config.getId(), target.address());
            return SendOutcome.success(SmsProviderType.MOCK.name(), config.getId());
        }
        return switch (SmsProviderType.valueOf(config.getProvider())) {
            case ALIYUN -> sendAliyun(config, target, message);
            case TENCENT -> sendTencent(config, target, message);
            case MOCK -> SendOutcome.success(SmsProviderType.MOCK.name(), config.getId());
        };
    }

    /**
     * 查询当前启用中的短信配置。
     *
     * @return 启用配置；不存在时返回 {@code null}
     */
    private NotificationSmsConfigEntity activeConfig() {
        return configMapper.selectOne(new LambdaQueryWrapper<NotificationSmsConfigEntity>()
                .eq(NotificationSmsConfigEntity::getActive, true)
                .isNull(NotificationSmsConfigEntity::getDeleted)
                .last("LIMIT 1"));
    }

    /**
     * 通过阿里云短信 OpenAPI 发送。
     *
     * @param config  短信配置
     * @param target  发送目标
     * @param message 渲染内容
     * @return 发送结果
     */
    private SendOutcome sendAliyun(NotificationSmsConfigEntity config, NotificationSendTarget target,
                                   RenderedMessage message) {
        try {
            var parameters = aliyunParameters(config, target.address(), message);
            var response = request(URI.create("https://" + endpoint(config, ALIYUN_DEFAULT_ENDPOINT)),
                    config, formBody(parameters), "application/x-www-form-urlencoded; charset=UTF-8");
            if (response.statusCode() >= 500) {
                return SendOutcome.failure(config.getProvider(), config.getId(), "SMS_PROVIDER_ERROR",
                        "短信供应商服务异常");
            }
            var body = readJson(response.body());
            if (!"OK".equalsIgnoreCase(String.valueOf(body.getOrDefault("Code", "")))) {
                return SendOutcome.failure(config.getProvider(), config.getId(), "SMS_PROVIDER_REJECTED",
                        text(body.get("Message")));
            }
            return SendOutcome.success(config.getProvider(), config.getId());
        } catch (RuntimeException | IOException exception) {
            log.warn("阿里云短信发送异常: configId={}", config.getId(), exception);
            return SendOutcome.failure(config.getProvider(), config.getId(), "SMS_REQUEST_FAILED", "短信发送请求失败");
        }
    }

    /**
     * 构造阿里云短信请求参数并签名。
     *
     * @param config    短信配置
     * @param recipient 收件手机号
     * @param message   渲染内容
     * @return 已完成签名的参数
     */
    private Map<String, String> aliyunParameters(NotificationSmsConfigEntity config, String recipient,
                                                 RenderedMessage message) {
        var parameters = new LinkedHashMap<String, String>();
        parameters.put("AccessKeyId", config.getAccessKeyId());
        parameters.put("Action", "SendSms");
        parameters.put("Format", "JSON");
        parameters.put("PhoneNumbers", recipient);
        parameters.put("RegionId", StringUtils.hasText(config.getRegion()) ? config.getRegion() : "cn-hangzhou");
        parameters.put("SignName", config.getSignName());
        parameters.put("SignatureMethod", "HMAC-SHA1");
        parameters.put("SignatureNonce", UUID.randomUUID().toString());
        parameters.put("SignatureVersion", "1.0");
        parameters.put("TemplateCode", templateCode(config, message));
        parameters.put("TemplateParam", objectMapper.writeValueAsString(stringParameters(message.parameters())));
        parameters.put("Timestamp", UTC_SECOND.format(Instant.now()));
        parameters.put("Version", "2017-05-25");
        parameters.put("Signature", aliyunSignature(parameters, config.getAccessKeySecret()));
        return parameters;
    }

    /**
     * 通过腾讯云短信 OpenAPI 发送。
     *
     * @param config  短信配置
     * @param target  发送目标
     * @param message 渲染内容
     * @return 发送结果
     */
    private SendOutcome sendTencent(NotificationSmsConfigEntity config, NotificationSendTarget target,
                                    RenderedMessage message) {
        try {
            var payload = tencentPayload(config, target.address(), message);
            var response = request(
                    URI.create("https://" + endpoint(config, TENCENT_DEFAULT_ENDPOINT)),
                    config, objectMapper.writeValueAsString(payload), "application/json; charset=utf-8", true);
            if (response.statusCode() >= 500) {
                return SendOutcome.failure(config.getProvider(), config.getId(), "SMS_PROVIDER_ERROR",
                        "短信供应商服务异常");
            }
            var body = readJson(response.body());
            var nested = nestedResponse(body);
            if (nested.get("Error") != null) {
                return SendOutcome.failure(config.getProvider(), config.getId(), "SMS_PROVIDER_REJECTED",
                        text(nested.get("Error")));
            }
            return SendOutcome.success(config.getProvider(), config.getId());
        } catch (RuntimeException | IOException exception) {
            log.warn("腾讯云短信发送异常: configId={}", config.getId(), exception);
            return SendOutcome.failure(config.getProvider(), config.getId(), "SMS_REQUEST_FAILED", "短信发送请求失败");
        }
    }

    /**
     * 构造腾讯云短信请求体。
     *
     * @param config    短信配置
     * @param recipient 收件手机号
     * @param message   渲染内容
     * @return 请求体
     */
    private Map<String, Object> tencentPayload(NotificationSmsConfigEntity config, String recipient,
                                               RenderedMessage message) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("SmsSdkAppId", extraText(config, "appId"));
        payload.put("SignName", config.getSignName());
        payload.put("TemplateId", templateCode(config, message));
        payload.put("TemplateParamSet", tencentParameters(config, message.parameters()));
        payload.put("PhoneNumberSet", List.of(recipient));
        return payload;
    }

    /**
     * 按配置的参数顺序生成腾讯云模板参数。
     *
     * @param config     短信配置
     * @param parameters 模板参数
     * @return 有序参数值
     */
    private List<String> tencentParameters(NotificationSmsConfigEntity config, Map<String, Object> parameters) {
        var order = extraText(config, "templateParameterOrder");
        var values = new ArrayList<String>();
        if (StringUtils.hasText(order)) {
            for (var name : order.split(",")) {
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
     * 读取配置扩展字段中的文本值。
     *
     * @param config 短信配置
     * @param key    扩展字段名
     * @return 文本值；不存在时返回空字符串
     */
    private String extraText(NotificationSmsConfigEntity config, String key) {
        if (config.getExtra() == null) {
            return "";
        }
        return String.valueOf(config.getExtra().getOrDefault(key, ""));
    }

    /**
     * 返回模板供应商编码，模板未指定时使用配置默认值。
     *
     * @param config  短信配置
     * @param message 渲染内容
     * @return 供应商模板编码
     */
    private String templateCode(NotificationSmsConfigEntity config, RenderedMessage message) {
        return StringUtils.hasText(message.providerTemplateCode())
                ? message.providerTemplateCode()
                : config.getProviderTemplateCode();
    }

    /**
     * 返回配置中的供应商地址。
     *
     * @param config      短信配置
     * @param defaultHost 默认地址
     * @return 供应商地址
     */
    private String endpoint(NotificationSmsConfigEntity config, String defaultHost) {
        var endpoint = extraText(config, "endpoint");
        if (!StringUtils.hasText(endpoint)) {
            return defaultHost;
        }
        return endpoint.replaceFirst("^https?://", "").replaceFirst("/.*$", "");
    }

    /**
     * 执行 HTTP 请求。
     *
     * @param uri         请求地址
     * @param config      短信配置
     * @param body        请求体
     * @param contentType 内容类型
     * @return HTTP 响应
     * @throws IOException 请求失败时抛出
     */
    private HttpResponse<String> request(URI uri, NotificationSmsConfigEntity config, String body, String contentType)
            throws IOException {
        return request(uri, config, body, contentType, false);
    }

    /**
     * 执行 HTTP 请求，可选择腾讯云签名。
     *
     * @param uri         请求地址
     * @param config      短信配置
     * @param body        请求体
     * @param contentType 内容类型
     * @param tencent     是否使用腾讯云签名
     * @return HTTP 响应
     * @throws IOException 请求失败时抛出
     */
    private HttpResponse<String> request(URI uri, NotificationSmsConfigEntity config, String body, String contentType,
                                         boolean tencent)
            throws IOException {
        var builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofMillis(properties.getTimeoutMs()))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (tencent) {
            tencentHeaders(builder, uri, config, body);
        }
        var client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(properties.getTimeoutMs())).build();
        try {
            return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("SMS request interrupted", exception);
        }
    }

    /**
     * 添加腾讯云 TC3 签名头。
     *
     * @param builder HTTP 请求构造器
     * @param uri     请求地址
     * @param config  短信配置
     * @param body    请求体
     */
    private void tencentHeaders(HttpRequest.Builder builder, URI uri, NotificationSmsConfigEntity config, String body) {
        var timestamp = Instant.now().getEpochSecond();
        var date = UTC_SECOND.format(Instant.ofEpochSecond(timestamp)).substring(0, 10);
        var canonicalHeaders = "content-type:application/json; charset=utf-8\nhost:" + uri.getHost() + "\n";
        var signedHeaders = "content-type;host";
        var canonicalRequest = "POST\n/\n\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + sha256(body);
        var credentialScope = date + "/" + TENCENT_SERVICE + "/tc3_request";
        var stringToSign = "TC3-HMAC-SHA256\n" + timestamp + "\n" + credentialScope + "\n" + sha256(canonicalRequest);
        var secretDate = hmac(("TC3" + config.getAccessKeySecret()).getBytes(StandardCharsets.UTF_8), date);
        var secretService = hmac(secretDate, TENCENT_SERVICE);
        var secretSigning = hmac(secretService, "tc3_request");
        var signature = hex(hmac(secretSigning, stringToSign));
        builder.header("Authorization", "TC3-HMAC-SHA256 Credential=" + config.getAccessKeyId() + "/"
                + credentialScope + ", SignedHeaders=" + signedHeaders + ", Signature=" + signature)
                .header("X-TC-Action", "SendSms")
                .header("X-TC-Version", TENCENT_VERSION)
                .header("X-TC-Timestamp", String.valueOf(timestamp))
                .header("X-TC-Region", StringUtils.hasText(config.getRegion()) ? config.getRegion() : "");
    }

    /**
     * 生成阿里云 RPC 签名。
     *
     * @param parameters 请求参数
     * @param secret     访问密钥 Secret
     * @return 签名
     */
    private String aliyunSignature(Map<String, String> parameters, String secret) {
        var canonicalized = parameters.entrySet()
                .stream()
                .filter(entry -> !"Signature".equals(entry.getKey()))
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
        return hmacSha1(secret + "&", "POST&%2F&" + encode(canonicalized));
    }

    /**
     * 编码阿里云参数。
     *
     * @param value 原值
     * @return 编码后的值
     */
    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

    /**
     * 生成 HMAC-SHA1 签名。
     *
     * @param key   密钥
     * @param value 待签名内容
     * @return Base64 签名
     */
    private String hmacSha1(String key, String value) {
        try {
            var mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            return Base64.getEncoder().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("SMS signature failed", exception);
        }
    }

    /**
     * 生成 HMAC-SHA256 摘要。
     *
     * @param key   密钥
     * @param value 待签名内容
     * @return 摘要字节
     */
    private byte[] hmac(byte[] key, String value) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("SMS signature failed", exception);
        }
    }

    /**
     * 生成 SHA-256 十六进制摘要。
     *
     * @param value 原值
     * @return 十六进制摘要
     */
    private String sha256(String value) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return hex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("SMS digest failed", exception);
        }
    }

    /**
     * 转换为十六进制字符串。
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private String hex(byte[] bytes) {
        var result = new StringBuilder(bytes.length * 2);
        for (var item : bytes) {
            result.append(String.format(Locale.ROOT, "%02x", item));
        }
        return result.toString();
    }

    /**
     * 将普通参数转换为字符串参数。
     *
     * @param parameters 模板参数
     * @return 字符串参数
     */
    private Map<String, String> stringParameters(Map<String, Object> parameters) {
        return parameters.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue() == null ? "" : String.valueOf(entry.getValue()),
                        (first, ignored) -> first, LinkedHashMap::new));
    }

    /**
     * 将请求体解析为 Map。
     *
     * @param body 响应体
     * @return 解析结果
     */
    private Map<String, Object> readJson(String body) {
        if (!StringUtils.hasText(body)) {
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
     * 读取腾讯云响应中的 Response 节点。
     *
     * @param body 响应体
     * @return Response 节点
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> nestedResponse(Map<String, Object> body) {
        return body.get("Response") instanceof Map<?, ?> response ? (Map<String, Object>) response : Map.of();
    }

    /**
     * 将表单参数编码为请求体。
     *
     * @param parameters 参数
     * @return 表单请求体
     */
    private String formBody(Map<String, String> parameters) {
        return parameters.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    /**
     * 返回文本值。
     *
     * @param value 原值
     * @return 文本值
     */
    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
