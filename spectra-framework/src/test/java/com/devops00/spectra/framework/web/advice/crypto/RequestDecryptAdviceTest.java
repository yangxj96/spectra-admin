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

import com.devops00.spectra.common.exception.EncryptException;
import com.devops00.spectra.common.port.security.SecurityReplayNonceAdminPort;
import com.devops00.spectra.common.security.crypto.symmetric.AESUtils;
import com.devops00.spectra.common.security.crypto.asymmetric.RSAUtils;
import com.devops00.spectra.framework.web.crypto.CryptoKeyManager;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 请求解密安全边界回归测试。 */
class RequestDecryptAdviceTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static KeyPair serverKeyPair;
    private static KeyPair clientKeyPair;

    @BeforeAll
    static void setUpKeys() throws Exception {
        serverKeyPair = RSAUtils.generateKeyPair();
        clientKeyPair = RSAUtils.generateKeyPair();
    }

    @ParameterizedTest
    @ValueSource(strings = {"signature", "nonce", "timestamp", "iv"})
    void shouldRejectEncryptedEnvelopeWithoutRequiredField(String missingField) throws Exception {
        var redis = redisReturning(true);
        var advice = new RequestDecryptAdvice(keyManager(), OBJECT_MAPPER, redis, new SecurityProperties());
        var envelope = validEnvelope("nonce-1", currentTimestamp());
        envelope.remove(missingField);

        var exception = assertThrows(EncryptException.class,
                () -> advice.beforeBodyRead(message(OBJECT_MAPPER.writeValueAsString(envelope)), parameter(),
                        Map.class, StringHttpMessageConverter.class));

        assertEquals("请求加密信封缺少必需字段: " + missingField, exception.getMessage());
    }

    @Test
    void shouldRejectBodyThatExceedsConfiguredDefaultLimit() throws Exception {
        var body = "{\"data\":\"" + "x".repeat(1_100_000) + "\"}";
        var advice = new RequestDecryptAdvice(mock(CryptoKeyManager.class), OBJECT_MAPPER, redisReturning(true), new SecurityProperties());

        var exception = assertThrows(EncryptException.class,
                () -> advice.beforeBodyRead(message(body), parameter(), Map.class, StringHttpMessageConverter.class));

        assertEquals("请求体超过最大大小", exception.getMessage());
    }

    @Test
    void shouldNotDowngradeMalformedEncryptedJsonToPlaintext() throws Exception {
        var advice = new RequestDecryptAdvice(mock(CryptoKeyManager.class), OBJECT_MAPPER, redisReturning(true), new SecurityProperties());

        var exception = assertThrows(EncryptException.class,
                () -> advice.beforeBodyRead(message("{not-json"), parameter(), Map.class, StringHttpMessageConverter.class));

        assertEquals("加密请求 JSON 格式无效", exception.getMessage());
    }

    @Test
    void shouldRejectEncryptedRequestWithNonJsonContentType() throws Exception {
        var advice = new RequestDecryptAdvice(mock(CryptoKeyManager.class), OBJECT_MAPPER, redisReturning(true), new SecurityProperties());
        var message = message("plain text");
        message.getHeaders().setContentType(MediaType.TEXT_PLAIN);

        var exception = assertThrows(EncryptException.class,
                () -> advice.beforeBodyRead(message, parameter(), Map.class, StringHttpMessageConverter.class));

        assertEquals("加密请求必须使用 JSON 请求体", exception.getMessage());
    }

    @Test
    void shouldNotAllowPlainJsonWhenCryptoConfigurationIsUnavailable() throws Exception {
        var keyManager = mock(CryptoKeyManager.class);
        when(keyManager.isConfiguredEnabled()).thenReturn(true);
        when(keyManager.isEnabled()).thenReturn(false);
        var advice = new RequestDecryptAdvice(keyManager, OBJECT_MAPPER, redisReturning(true), new SecurityProperties());
        var message = message("{\"name\":\"spectra\"}");
        message.getHeaders().remove("X-Encrypted");

        var exception = assertThrows(EncryptException.class,
                () -> advice.beforeBodyRead(message, parameter(), Map.class, StringHttpMessageConverter.class));

        assertEquals("加密密钥不可用", exception.getMessage());
    }

    @Test
    void shouldRejectIllegalIvBeforeAttemptingAesDecryption() throws Exception {
        var redis = redisReturning(true);
        var advice = new RequestDecryptAdvice(keyManager(), OBJECT_MAPPER, redis, new SecurityProperties());
        var envelope = validEnvelope("nonce-2", currentTimestamp());
        envelope.put("iv", "not-hex");

        var exception = assertThrows(EncryptException.class,
                () -> advice.beforeBodyRead(message(OBJECT_MAPPER.writeValueAsString(envelope)), parameter(),
                        Map.class, StringHttpMessageConverter.class));

        assertEquals("请求加密信封 IV 无效", exception.getMessage());
    }

    @Test
    void shouldRejectIllegalBase64BeforeAttemptingDecryption() throws Exception {
        var redis = redisReturning(true);
        var advice = new RequestDecryptAdvice(keyManager(), OBJECT_MAPPER, redis, new SecurityProperties());
        var envelope = validEnvelope("nonce-base64", currentTimestamp());
        envelope.put("data", "not-base64");
        envelope.put("signature", RSAUtils.sign("data=not-base64&nonce=nonce-base64&timestamp="
                + envelope.get("timestamp"), clientKeyPair.getPrivate()));

        var exception = assertThrows(EncryptException.class,
                () -> advice.beforeBodyRead(message(OBJECT_MAPPER.writeValueAsString(envelope)), parameter(),
                        Map.class, StringHttpMessageConverter.class));

        assertEquals("请求加密信封 data 编码无效", exception.getMessage());
    }

    @Test
    void shouldRejectExpiredEnvelopeWithoutConsumingNonce() throws Exception {
        var redis = redisReturning(true);
        var advice = new RequestDecryptAdvice(keyManager(), OBJECT_MAPPER, redis, new SecurityProperties());
        var envelope = validEnvelope("nonce-3", currentTimestamp() - 301);

        var exception = assertThrows(EncryptException.class,
                () -> advice.beforeBodyRead(message(OBJECT_MAPPER.writeValueAsString(envelope)), parameter(),
                        Map.class, StringHttpMessageConverter.class));

        assertEquals("请求已过期（时间戳超出300秒窗口）", exception.getMessage());
        verify(redis.opsForValue(), never()).setIfAbsent(anyString(), any(), any(Duration.class));
    }

    @Test
    void shouldRejectReplayedNonce() throws Exception {
        var redis = redisReturning(false);
        var advice = new RequestDecryptAdvice(keyManager(), OBJECT_MAPPER, redis, new SecurityProperties());
        var envelope = validEnvelope("nonce-4", currentTimestamp());

        var exception = assertThrows(EncryptException.class,
                () -> advice.beforeBodyRead(message(OBJECT_MAPPER.writeValueAsString(envelope)), parameter(),
                        Map.class, StringHttpMessageConverter.class));

        assertEquals("重复请求（nonce 已使用）", exception.getMessage());
    }

    @Test
    void shouldRejectNonceAtOrBeforeGlobalCutoffBeforeConsumingIt() throws Exception {
        var nonceAdmin = mock(SecurityReplayNonceAdminPort.class);
        when(nonceAdmin.isBeforeOrAtCutoff(anyLong())).thenReturn(true);
        var advice = new RequestDecryptAdvice(keyManager(), OBJECT_MAPPER, redisReturning(true),
                new SecurityProperties(), nonceAdmin);
        var envelope = validEnvelope("nonce-cutoff", currentTimestamp());

        var exception = assertThrows(EncryptException.class,
                () -> advice.beforeBodyRead(message(OBJECT_MAPPER.writeValueAsString(envelope)), parameter(),
                        Map.class, StringHttpMessageConverter.class));

        assertEquals("请求已被当前 nonce 失效窗口拒绝", exception.getMessage());
        verify(nonceAdmin, never()).invalidate(anyString(), anyLong());
    }

    @Test
    void shouldKeepRedisUnavailableAsFailClosedFailure() throws Exception {
        var redis = redisReturning(true);
        var values = redis.opsForValue();
        when(values.setIfAbsent(anyString(), any(), any(Duration.class)))
                .thenThrow(new DataAccessResourceFailureException("redis unavailable"));
        var advice = new RequestDecryptAdvice(keyManager(), OBJECT_MAPPER, redis, new SecurityProperties());
        var envelope = validEnvelope("nonce-5", currentTimestamp());

        assertThrows(RuntimeException.class,
                () -> advice.beforeBodyRead(message(OBJECT_MAPPER.writeValueAsString(envelope)), parameter(),
                        Map.class, StringHttpMessageConverter.class));
    }

    @Test
    void shouldRejectWhenCryptoKeyIsUnavailable() throws Exception {
        var keyManager = mock(CryptoKeyManager.class);
        when(keyManager.getClientPublicKey()).thenReturn(null);
        when(keyManager.getServerPrivateKey()).thenReturn(null);
        var advice = new RequestDecryptAdvice(keyManager, OBJECT_MAPPER, redisReturning(true), new SecurityProperties());
        var envelope = validEnvelope("nonce-6", currentTimestamp());

        var exception = assertThrows(EncryptException.class,
                () -> advice.beforeBodyRead(message(OBJECT_MAPPER.writeValueAsString(envelope)), parameter(),
                        Map.class, StringHttpMessageConverter.class));

        assertEquals("密钥未就绪，无法解密请求", exception.getMessage());
    }

    @Test
    void shouldDecryptUnsignedEnvelopeForPermitAllEndpoint() throws Exception {
        var keyManager = mock(CryptoKeyManager.class);
        when(keyManager.getServerPrivateKey()).thenReturn(serverKeyPair.getPrivate());
        when(keyManager.getClientPublicKey()).thenReturn(null);
        var advice = new RequestDecryptAdvice(keyManager, OBJECT_MAPPER, redisReturning(true), new SecurityProperties());
        var envelope = unsignedEnvelope("anonymous-login", currentTimestamp());

        var result = advice.beforeBodyRead(message(OBJECT_MAPPER.writeValueAsString(envelope)),
                new MethodParameter(Endpoint.class.getDeclaredMethod("anonymousRead", Map.class), 0),
                Map.class, StringHttpMessageConverter.class);

        assertEquals("{\"name\":\"spectra\"}", new String(result.getBody().readAllBytes(), StandardCharsets.UTF_8));
    }

    private static CryptoKeyManager keyManager() {
        var keyManager = mock(CryptoKeyManager.class);
        when(keyManager.getClientPublicKey()).thenReturn(clientKeyPair.getPublic());
        when(keyManager.getServerPrivateKey()).thenReturn(serverKeyPair.getPrivate());
        return keyManager;
    }

    @SuppressWarnings("unchecked")
    private static RedisTemplate<String, Object> redisReturning(boolean result) {
        var redis = (RedisTemplate<String, Object>) mock(RedisTemplate.class);
        var values = (ValueOperations<String, Object>) mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.setIfAbsent(anyString(), any(), any(Duration.class))).thenReturn(result);
        return redis;
    }

    private static Map<String, Object> validEnvelope(String nonce, long timestamp) throws Exception {
        var aesKey = AESUtils.generateKey();
        var iv = AESUtils.generateIv();
        var data = AESUtils.encrypt("{\"name\":\"spectra\"}", aesKey, iv);
        var encryptedKey = RSAUtils.encrypt(aesKey.getEncoded(), serverKeyPair.getPublic());
        var signContent = "data=%s&nonce=%s&timestamp=%d".formatted(data, nonce, timestamp);
        var envelope = new HashMap<String, Object>();
        envelope.put("data", data);
        envelope.put("key", encryptedKey);
        envelope.put("iv", AESUtils.getIvHex(iv));
        envelope.put("nonce", nonce);
        envelope.put("timestamp", timestamp);
        envelope.put("signature", RSAUtils.sign(signContent, clientKeyPair.getPrivate()));
        return envelope;
    }

    private static Map<String, Object> unsignedEnvelope(String nonce, long timestamp) throws Exception {
        var aesKey = AESUtils.generateKey();
        var iv = AESUtils.generateIv();
        var data = AESUtils.encrypt("{\"name\":\"spectra\"}", aesKey, iv);
        var encryptedKey = RSAUtils.encrypt(aesKey.getEncoded(), serverKeyPair.getPublic());
        var envelope = new HashMap<String, Object>();
        envelope.put("data", data);
        envelope.put("key", encryptedKey);
        envelope.put("iv", AESUtils.getIvHex(iv));
        envelope.put("nonce", nonce);
        envelope.put("timestamp", timestamp);
        return envelope;
    }

    private static long currentTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    private static MethodParameter parameter() throws NoSuchMethodException {
        return new MethodParameter(Endpoint.class.getDeclaredMethod("read", Map.class), 0);
    }

    private static HttpInputMessage message(String body) {
        return new BodyMessage(body);
    }

    private static final class Endpoint {

        @SuppressWarnings("unused")
        void read(@RequestBody Map<String, Object> body) {
        }

        @PreAuthorize("permitAll()")
        void anonymousRead(@RequestBody Map<String, Object> body) {
        }
    }

    private static final class BodyMessage implements HttpInputMessage {

        private final HttpHeaders headers = new HttpHeaders();
        private final InputStream body;

        private BodyMessage(String value) {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Encrypted", "1");
            body = new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public InputStream getBody() {
            return body;
        }
    }
}
