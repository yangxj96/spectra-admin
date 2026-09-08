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

package com.devops00.spectra.framework.web.advice.crypto;

import com.devops00.spectra.common.annotation.Encrypt;
import com.devops00.spectra.common.audit.RequestCorrelationContext;
import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.exception.EncryptException;
import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.common.security.crypto.symmetric.AESUtils;
import com.devops00.spectra.common.security.crypto.asymmetric.RSAUtils;
import com.devops00.spectra.common.security.crypto.digest.SHA256Utils;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisExecutor;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.web.crypto.CryptoKeyManager;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

/**
 * 请求体解密 Advice
 * <p>
 * 在 MessageConverter 反序列化之前拦截请求，
 * 自动检测加密请求并解密后放行。
 * 支持验签、防重放攻击（时间窗口 + Nonce 去重）。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/11
 */
@Slf4j
@NullMarked
@ControllerAdvice
public class RequestDecryptAdvice implements RequestBodyAdvice {

    /**
     * 加密请求标记头
     */
    private static final String ENCRYPTED_HEADER = "X-Encrypted";

    // jackson序列化
    private final ObjectMapper om;

    // 加解密key管理器
    private final CryptoKeyManager cryptoKeyManager;

    // redis
    private final RedisTemplate<String, Object> redisTemplate;

    private final SecurityProperties securityProperties;

    public RequestDecryptAdvice(CryptoKeyManager cryptoKeyManager, ObjectMapper om,
                                @Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redisTemplate,
                                SecurityProperties securityProperties) {
        this.cryptoKeyManager = cryptoKeyManager;
        this.om = om;
        this.redisTemplate = redisTemplate;
        this.securityProperties = securityProperties;
        log.info(LogPrefix.WEB.f("请求解密 Advice 已注册（运行时由 CryptoKeyManager 控制启用/禁用）"));
    }

    /**
     * 获取或判断 Framework 的 supports 结果。
     *
     * @param methodParameter 控制器方法参数元数据。
     * @param targetType      请求体要反序列化成的目标类型。
     * @param converterType   当前 HTTP 消息转换器类型，用于判断 Advice 是否适用。
     * @return 返回请求是否需要进入解密 Advice；加密明确关闭或二进制转换器请求返回 false，启用加密且未显式关闭时返回 true。
     */
    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 明确关闭时不注册 Advice；配置故障时仍保留 Advice，防止加密请求静默降级为明文。
        if (!cryptoKeyManager.isConfiguredEnabled()) {
            return false;
        }

        // 忽略 ByteArrayHttpMessageConverter（文件上传等二进制场景）
        if (ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType)) {
            log.debug(LogPrefix.WEB.f("跳过请求解密: 字节数组转换器"));
            return false;
        }

        // 检查 @Encrypt 注解（方法级优先于类级）
        Method method = methodParameter.getMethod();
        if (method != null) {
            Encrypt methodAnno = AnnotatedElementUtils.findMergedAnnotation(method, Encrypt.class);
            if (methodAnno != null) {
                if (!methodAnno.value()) {
                    log.debug("{}跳过请求解密: @Encrypt(false) on {}", LogPrefix.WEB.p(), method.getName());
                }
                return methodAnno.value();
            }

            Encrypt classAnno = AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(), Encrypt.class);
            if (classAnno != null) {
                if (!classAnno.value()) {
                    log.debug("{}跳过请求解密: @Encrypt(false) on {}", LogPrefix.WEB.p(), method.getDeclaringClass().getSimpleName());
                }
                return classAnno.value();
            }
        }

        return true;
    }

    /**
     * 读取并解密请求体后交给控制器反序列化。
     *
     * @param inputMessage  已读取请求头和请求体的 HTTP 输入消息。
     * @param parameter     控制器方法参数，用于确定请求解密目标。
     * @param targetType    请求体要反序列化成的目标类型。
     * @param converterType 当前 HTTP 消息转换器类型，用于判断 Advice 是否适用。
     * @return 返回原始或解密后的 HTTP 输入消息；明文和非 JSON 请求体按界限读取后返回重建消息，加密校验失败或超限时抛出异常，不返回 null。
     * @throws IOException 依赖不可用或输入不满足组件约束时抛出。
     */
    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType)
            throws IOException {
        boolean hasEncryptedHeader = "1".equals(inputMessage.getHeaders().getFirst(ENCRYPTED_HEADER));
        if (!isJsonContentType(inputMessage.getHeaders().getContentType())) {
            if (hasEncryptedHeader) {
                throw new RequestCryptoException("加密请求必须使用 JSON 请求体");
            }
            // 二进制/表单请求不能被读取后再判断，否则会把大文件完整载入内存。
            return inputMessage;
        }
        byte[] bodyBytes = readBoundedBody(inputMessage);

        // 尝试解析 JSON 并检测是否为加密请求
        JsonNode node;
        try {
            node = om.readTree(bodyBytes);
        } catch (Exception e) {
            if (hasEncryptedHeader) {
                throw new RequestCryptoException("加密请求 JSON 格式无效", e);
            }
            rejectWhenCryptoUnavailable();
            // 非 JSON 请求体，原样放行
            log.debug(LogPrefix.WEB.f("非 JSON 请求体，跳过解密"));
            return new DecryptedHttpInputMessage(inputMessage, bodyBytes);
        }

        if (node == null) {
            if (hasEncryptedHeader) {
                throw new RequestCryptoException("加密请求 JSON 格式无效");
            }
            rejectWhenCryptoUnavailable();
            return new DecryptedHttpInputMessage(inputMessage, bodyBytes);
        }

        // 双重判断：请求头标记 或 请求体结构
        if (!hasEncryptedHeader && !isEncryptedBody(node)) {
            rejectWhenCryptoUnavailable();
            log.debug(LogPrefix.WEB.f("明文请求，跳过解密"));
            return new DecryptedHttpInputMessage(inputMessage, bodyBytes);
        }

        log.debug("{}检测到加密请求（X-Encrypted={}），开始解密", LogPrefix.WEB.p(), hasEncryptedHeader);

        try {
            long start = System.currentTimeMillis();
            String decryptedJson = decrypt(node);
            log.debug("{}请求解密完成, 耗时: {}ms", LogPrefix.WEB.p(), System.currentTimeMillis() - start);
            return new DecryptedHttpInputMessage(inputMessage, decryptedJson.getBytes(StandardCharsets.UTF_8));
        } catch (EncryptException exception) {
            throw exception;
        } catch (SecurityRedisUnavailableException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("请求解密失败，correlationId={}", correlationId());
            throw new RequestCryptoException("请求加密数据无效", exception);
        }
    }

    /**
     * 处理已反序列化的请求体并执行必要的安全校验。
     *
     * @param body          待加密、解密或转换的请求/响应体。
     * @param inputMessage  已读取请求头和请求体的 HTTP 输入消息。
     * @param parameter     控制器方法参数，用于确定请求解密目标。
     * @param targetType    请求体要反序列化成的目标类型。
     * @param converterType 当前 HTTP 消息转换器类型，用于判断 Advice 是否适用。
     * @return 返回已完成安全校验的反序列化请求体；当前实现原样返回 {@code body}，不会把正常请求转换为 null。
     */
    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    /**
     * 处理空请求体，并按请求契约返回安全结果。
     *
     * @param body          待加密、解密或转换的请求/响应体。
     * @param inputMessage  已读取请求头和请求体的 HTTP 输入消息。
     * @param parameter     控制器方法参数，用于确定请求解密目标。
     * @param targetType    请求体要反序列化成的目标类型。
     * @param converterType 当前 HTTP 消息转换器类型，用于判断 Advice 是否适用。
     * @return 返回空请求体的原始值；没有请求体时返回 null，Advice 不用空对象替代调用方声明的空值语义。
     */
    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                  Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    /**
     * 判断请求体是否为加密格式（同时包含 data、key、iv 三个字段）
     */
    private boolean isEncryptedBody(JsonNode node) {
        return node.has("data") && node.has("key") && node.has("iv");
    }

    /** 在 JSON 解析前限制请求体大小，并同时检查声明长度和实际读取长度。 */
    private byte[] readBoundedBody(HttpInputMessage inputMessage) throws IOException {
        long maximum = securityProperties.getCryptoRequestMaxBodyBytes();
        long declaredLength = inputMessage.getHeaders().getContentLength();
        if (declaredLength > maximum) {
            throw new RequestCryptoException("请求体超过最大大小");
        }
        var output = new ByteArrayOutputStream((int) Math.min(maximum, 8_192));
        byte[] buffer = new byte[8_192];
        long total = 0;
        int read;
        while ((read = inputMessage.getBody().read(buffer)) != -1) {
            total += read;
            if (total > maximum) {
                throw new RequestCryptoException("请求体超过最大大小");
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private static boolean isJsonContentType(org.springframework.http.MediaType contentType) {
        return contentType != null
                && (org.springframework.http.MediaType.APPLICATION_JSON.includes(contentType)
                        || contentType.getSubtype().endsWith("+json"));
    }

    /**
     * 解密加密请求体（含验签 + 防重放）
     */
    private String decrypt(JsonNode node) throws Exception {
        requireEnvelope(node);
        String encryptedData = node.get("data").asString();
        String encryptedKey = node.get("key").asString();
        String ivHex = node.get("iv").asString();
        String nonce = node.get("nonce").asString();
        long timestamp = node.get("timestamp").asLong();

        // 从 CryptoKeyManager 获取密钥
        PublicKey clientPublicKey = cryptoKeyManager.getClientPublicKey();
        PrivateKey serverPrivateKey = cryptoKeyManager.getServerPrivateKey();
        validateKeys(clientPublicKey, serverPrivateKey);
        verifySignature(node, encryptedData, nonce, timestamp, clientPublicKey);
        validateTimestamp(timestamp);
        consumeNonce(nonce);

        return decryptPayload(encryptedKey, serverPrivateKey, encryptedData, ivHex);
    }

    /**
     * 校验并确保数据满足当前约束（{@code validateKeys}）。
     */
    private static void validateKeys(PublicKey clientPublicKey, PrivateKey serverPrivateKey) {
        if (clientPublicKey == null || serverPrivateKey == null) {
            throw new EncryptException("密钥未就绪，无法解密请求");
        }
    }

    /**
     * 处理内部业务逻辑（{@code verifySignature}）。
     */
    private void verifySignature(JsonNode node, String encryptedData, String nonce, long timestamp,
                                 PublicKey clientPublicKey)
            throws Exception {
        long start = System.currentTimeMillis();
        String signature = node.get("signature").asString();
        String signContent = String.format("data=%s&nonce=%s&timestamp=%d", encryptedData,
                nonce != null ? nonce : "", timestamp);
        if (!RSAUtils.verify(signContent, signature, clientPublicKey)) {
            throw new EncryptException("请求签名验证失败，数据可能被篡改");
        }
        log.debug("{}请求签名验证耗时: {}ms", LogPrefix.WEB.p(), System.currentTimeMillis() - start);
    }

    /**
     * 校验并确保数据满足当前约束（{@code validateTimestamp}）。
     */
    private void validateTimestamp(long timestamp) {
        long now = System.currentTimeMillis() / 1000;
        long window = securityProperties.getCryptoReplayWindowSeconds();
        if (Math.abs(now - timestamp) > window) {
            throw new RequestCryptoException("请求已过期（时间戳超出" + window + "秒窗口）");
        }
    }

    /**
     * 更新或推进目标状态（{@code consumeNonce}）。
     */
    private void consumeNonce(String nonce) throws Exception {
        long window = securityProperties.getCryptoReplayWindowSeconds();
        String nonceKey = SecurityRedisKey.CRYPTO_NONCE.format(SHA256Utils.hash(nonce));
        Boolean success = SecurityRedisExecutor.require("记录加密请求 nonce",
                () -> redisTemplate.opsForValue().setIfAbsent(nonceKey, "1", Duration.ofSeconds(window)));
        if (Boolean.FALSE.equals(success)) {
            throw new RequestCryptoException("重复请求（nonce 已使用）");
        }
    }

    /**
     * 执行加密或解密处理（{@code decryptPayload}）。
     */
    private String decryptPayload(String encryptedKey, PrivateKey serverPrivateKey, String encryptedData, String ivHex)
            throws Exception {
        validateBase64(encryptedKey, "key");
        validateBase64(encryptedData, "data");
        if (!ivHex.matches("[0-9a-fA-F]{24}")) {
            throw new RequestCryptoException("请求加密信封 IV 无效");
        }
        long t2 = System.currentTimeMillis();
        byte[] aesKeyBytes = RSAUtils.decrypt(encryptedKey, serverPrivateKey);
        log.debug("{}RSA解密AES密钥耗时: {}ms", LogPrefix.WEB.p(), System.currentTimeMillis() - t2);

        long t3 = System.currentTimeMillis();
        byte[] iv = AESUtils.hexToIv(ivHex);
        String decrypted = AESUtils.decrypt(encryptedData, aesKeyBytes, iv);
        log.debug("{}AES解密业务数据耗时: {}ms", LogPrefix.WEB.p(), System.currentTimeMillis() - t3);

        return decrypted;
    }

    /** 校验加密信封字段，避免空值、隐式默认值和类型转换绕过安全边界。 */
    private static void requireEnvelope(JsonNode node) {
        List<String> required = List.of("data", "key", "iv", "signature", "nonce", "timestamp");
        for (String field : required) {
            JsonNode value = node.get(field);
            if (value == null || (!"timestamp".equals(field) && (!value.isTextual() || value.asString().isBlank()))) {
                throw new RequestCryptoException("请求加密信封缺少必需字段: " + field);
            }
        }
        JsonNode timestamp = node.get("timestamp");
        if (!timestamp.isIntegralNumber() || timestamp.asLong() <= 0) {
            throw new RequestCryptoException("请求加密信封时间戳无效");
        }
        String nonce = node.get("nonce").asString();
        if (nonce.length() > 128 || !nonce.matches("[A-Za-z0-9._:-]+")) {
            throw new RequestCryptoException("请求加密信封 nonce 无效");
        }
    }

    /** 校验标准 Base64，避免将非法输入交给底层密码 API。 */
    private static void validateBase64(String value, String field) {
        try {
            if (Base64.getDecoder().decode(value).length == 0) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException exception) {
            throw new RequestCryptoException("请求加密信封 " + field + " 编码无效", exception);
        }
    }

    /** 加密配置已开启但密钥暂不可用时，拒绝把请求降级为明文。 */
    private void rejectWhenCryptoUnavailable() {
        if (cryptoKeyManager.isConfiguredEnabled() && !cryptoKeyManager.isEnabled()) {
            throw new EncryptException("加密密钥不可用");
        }
    }

    private static String correlationId() {
        String correlationId = RequestCorrelationContext.current().correlationId();
        return correlationId == null ? "unknown" : correlationId;
    }

    /**
     * 可替换 body 的 HttpInputMessage 包装类
     */
    private static class DecryptedHttpInputMessage implements HttpInputMessage {

        private final HttpHeaders headers;
        private final InputStream body;

        DecryptedHttpInputMessage(HttpInputMessage original, byte[] bodyBytes) {
            this.headers = HttpHeaders.readOnlyHttpHeaders(original.getHeaders());
            this.body = new ByteArrayInputStream(bodyBytes);
        }

        /**
         * 获取或判断 Framework 的 getHeaders 结果。
         *
         * @return 返回被包装请求的 HTTP 请求头快照；结果始终为非 null 的 {@code HttpHeaders}。
         */
        @Override
        public HttpHeaders getHeaders() {
            return HttpHeaders.readOnlyHttpHeaders(headers);
        }

        /**
         * 获取或判断 Framework 的 getBody 结果。
         *
         * @return 返回解密或重建后的请求体输入流；输入消息无法提供流时抛出 IOException，不返回 null。
         */
        @Override
        public InputStream getBody() {
            return body;
        }
    }
}
