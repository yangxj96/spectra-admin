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

package com.devops00.spectra.framework.configure.mvc.advice.request;

import com.devops00.spectra.common.annotation.Encrypt;
import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.exception.EncryptException;
import com.devops00.spectra.common.utils.AESUtils;
import com.devops00.spectra.common.utils.RSAUtils;
import com.devops00.spectra.framework.configure.mvc.crypto.CryptoKeyManager;
import com.devops00.spectra.security.base.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.security.base.util.SecurityRedisExecutor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;

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

    /**
     * 防重放时间窗口（秒）
     */
    private static final long REPLAY_WINDOW_SECONDS = 300;

    /**
     * Redis nonce 缓存前缀
     */
    private static final String NONCE_PREFIX = "crypto:nonce:";

    // jackson序列化
    private final ObjectMapper om;

    // 加解密key管理器
    private final CryptoKeyManager cryptoKeyManager;

    // redis
    private final RedisTemplate<String, Object> redisTemplate;

    public RequestDecryptAdvice(CryptoKeyManager cryptoKeyManager, ObjectMapper om, RedisTemplate<String, Object> redisTemplate) {
        this.cryptoKeyManager = cryptoKeyManager;
        this.om = om;
        this.redisTemplate = redisTemplate;
        log.info(LogPrefix.WEB.f("请求解密 Advice 已注册（运行时由 CryptoKeyManager 控制启用/禁用）"));
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 检查加解密是否启用
        if (!cryptoKeyManager.isEnabled()) {
            return false;
        }

        // 忽略 ByteArrayHttpMessageConverter（文件上传等二进制场景）
        if (converterType.isAssignableFrom(ByteArrayHttpMessageConverter.class)) {
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

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType)
            throws IOException {
        if (!isJsonContentType(inputMessage.getHeaders().getContentType())) {
            // 二进制/表单请求不能被读取后再判断，否则会把大文件完整载入内存。
            return inputMessage;
        }
        byte[] bodyBytes = inputMessage.getBody().readAllBytes();

        // 优先检查 X-Encrypted 请求头
        boolean hasEncryptedHeader = "1".equals(inputMessage.getHeaders().getFirst(ENCRYPTED_HEADER));

        // 尝试解析 JSON 并检测是否为加密请求
        JsonNode node;
        try {
            node = om.readTree(bodyBytes);
        } catch (Exception e) {
            // 非 JSON 请求体，原样放行
            log.debug(LogPrefix.WEB.f("非 JSON 请求体，跳过解密"));
            return new DecryptedHttpInputMessage(inputMessage, bodyBytes);
        }

        // 双重判断：请求头标记 或 请求体结构
        if (!hasEncryptedHeader && !isEncryptedBody(node)) {
            log.debug(LogPrefix.WEB.f("明文请求，跳过解密"));
            return new DecryptedHttpInputMessage(inputMessage, bodyBytes);
        }

        log.debug("{}检测到加密请求（X-Encrypted={}），开始解密", LogPrefix.WEB.p(), hasEncryptedHeader);

        try {
            long start = System.currentTimeMillis();
            String decryptedJson = decrypt(node);
            log.debug("{}请求解密完成, 耗时: {}ms", LogPrefix.WEB.p(), System.currentTimeMillis() - start);
            return new DecryptedHttpInputMessage(inputMessage, decryptedJson.getBytes(StandardCharsets.UTF_8));
        } catch (EncryptException e) {
            throw e;
        } catch (SecurityRedisUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("请求解密失败: {}", e.getMessage(), e);
            throw new EncryptException("请求解密失败", e);
        }
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

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

    private static boolean isJsonContentType(org.springframework.http.MediaType contentType) {
        return contentType != null
                && (org.springframework.http.MediaType.APPLICATION_JSON.includes(contentType)
                        || contentType.getSubtype().endsWith("+json"));
    }

    /**
     * 解密加密请求体（含验签 + 防重放）
     */
    private String decrypt(JsonNode node) throws Exception {
        String encryptedData = node.get("data").asString();
        String encryptedKey = node.get("key").asString();
        String ivHex = node.get("iv").asString();
        String nonce = node.has("nonce") ? node.get("nonce").asString() : null;
        long timestamp = node.has("timestamp") ? node.get("timestamp").asLong() : 0;

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
        if (!node.has("signature")) {
            return;
        }
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
    private static void validateTimestamp(long timestamp) {
        long now = System.currentTimeMillis() / 1000;
        if (timestamp > 0 && Math.abs(now - timestamp) > REPLAY_WINDOW_SECONDS) {
            throw new EncryptException("请求已过期（时间戳超出" + REPLAY_WINDOW_SECONDS + "秒窗口）");
        }
    }

    /**
     * 更新或推进目标状态（{@code consumeNonce}）。
     */
    private void consumeNonce(String nonce) throws Exception {
        if (nonce == null || nonce.isEmpty()) {
            return;
        }
        String nonceKey = NONCE_PREFIX + nonce;
        Boolean success = SecurityRedisExecutor.require("记录加密请求 nonce",
                () -> redisTemplate.opsForValue().setIfAbsent(nonceKey, "1", Duration.ofSeconds(REPLAY_WINDOW_SECONDS)));
        if (Boolean.FALSE.equals(success)) {
            throw new EncryptException("重复请求（nonce 已使用）");
        }
    }

    /**
     * 执行加密或解密处理（{@code decryptPayload}）。
     */
    private String decryptPayload(String encryptedKey, PrivateKey serverPrivateKey, String encryptedData, String ivHex)
            throws Exception {
        long t2 = System.currentTimeMillis();
        byte[] aesKeyBytes = RSAUtils.decrypt(encryptedKey, serverPrivateKey);
        log.debug("{}RSA解密AES密钥耗时: {}ms", LogPrefix.WEB.p(), System.currentTimeMillis() - t2);

        long t3 = System.currentTimeMillis();
        byte[] iv = AESUtils.hexToIv(ivHex);
        String decrypted = AESUtils.decrypt(encryptedData, aesKeyBytes, iv);
        log.debug("{}AES解密业务数据耗时: {}ms", LogPrefix.WEB.p(), System.currentTimeMillis() - t3);

        return decrypted;
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

        @Override
        public HttpHeaders getHeaders() {
            return HttpHeaders.readOnlyHttpHeaders(headers);
        }

        @Override
        public InputStream getBody() {
            return body;
        }
    }
}
