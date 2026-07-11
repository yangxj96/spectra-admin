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

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.exception.EncryptException;
import com.devops00.spectra.common.utils.AESUtils;
import com.devops00.spectra.common.utils.RSAUtils;
import com.devops00.spectra.framework.configure.mvc.properties.SMProperties;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
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
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;

/// 请求体解密 Advice
///
/// 在 MessageConverter 反序列化之前拦截请求，
/// 自动检测加密请求（包含 data/key/iv 字段）并解密后放行。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/11
@Slf4j
@NullMarked
@ControllerAdvice
@ConditionalOnProperty(prefix = "spectra.system.sm", name = "enabled", havingValue = "true")
public class RequestDecryptAdvice implements RequestBodyAdvice {

    private final ObjectMapper om;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public RequestDecryptAdvice(SMProperties properties, ObjectMapper om) {
        this.om = om;
        try {
            this.publicKey = RSAUtils.restorePublicKey(properties.getPublicKey());
            this.privateKey = RSAUtils.restorePrivateKey(properties.getPrivateKey());
        } catch (Exception e) {
            throw new IllegalStateException("RSA密钥初始化失败", e);
        }
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        // 忽略 ByteArrayHttpMessageConverter（文件上传等二进制场景）
        if (converterType.isAssignableFrom(ByteArrayHttpMessageConverter.class)) {
            return false;
        }
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage,
                                           MethodParameter parameter, Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        byte[] bodyBytes = inputMessage.getBody().readAllBytes();

        // 尝试解析 JSON 并检测是否为加密请求
        JsonNode node;
        try {
            node = om.readTree(bodyBytes);
        } catch (Exception e) {
            // 非 JSON 请求体，原样放行
            log.debug(LogPrefix.WEB.f("非 JSON 请求体，跳过解密"));
            return new DecryptedHttpInputMessage(inputMessage, bodyBytes);
        }

        if (!isEncryptedBody(node)) {
            log.debug(LogPrefix.WEB.f("明文请求，跳过解密"));
            return new DecryptedHttpInputMessage(inputMessage, bodyBytes);
        }

        log.debug(LogPrefix.WEB.f("检测到加密请求，开始解密"));

        try {
            long start = System.currentTimeMillis();
            String decryptedJson = decrypt(node);
            log.info("{}请求解密耗时: {}ms", LogPrefix.WEB.p(), System.currentTimeMillis() - start);
            return new DecryptedHttpInputMessage(inputMessage,
                    decryptedJson.getBytes(StandardCharsets.UTF_8));
        } catch (EncryptException e) {
            throw e;
        } catch (Exception e) {
            log.error("请求解密失败: {}", e.getMessage(), e);
            throw new EncryptException("请求解密失败", e);
        }
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage,
                                MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage,
                                  MethodParameter parameter, Type targetType,
                                  Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    /// 判断请求体是否为加密格式（同时包含 data、key、iv 三个字段）
    private boolean isEncryptedBody(JsonNode node) {
        return node.has("data") && node.has("key") && node.has("iv");
    }

    /// 解密加密请求体
    private String decrypt(JsonNode node) throws Exception {
        String encryptedData = node.get("data").asText();
        String encryptedKey = node.get("key").asText();
        String ivHex = node.get("iv").asText();
        String nonce = node.has("nonce") ? node.get("nonce").asText() : null;
        long timestamp = node.has("timestamp") ? node.get("timestamp").asLong() : 0;

        // 签名验证（如果提供了 signature）
        if (node.has("signature")) {
            long t1 = System.currentTimeMillis();
            String signature = node.get("signature").asText();
            String signContent = String.format("data=%s&nonce=%s&timestamp=%d",
                    encryptedData, nonce != null ? nonce : "", timestamp);
            boolean valid = RSAUtils.verify(signContent, signature, publicKey);
            if (!valid) {
                throw new EncryptException("请求签名验证失败，数据可能被篡改");
            }
            log.info("{}请求签名验证耗时: {}ms", LogPrefix.WEB.p(), System.currentTimeMillis() - t1);
        }

        // RSA 私钥解密 AES 密钥
        long t2 = System.currentTimeMillis();
        byte[] aesKeyBytes = RSAUtils.decrypt(encryptedKey, privateKey);
        log.info("{}RSA解密AES密钥耗时: {}ms", LogPrefix.WEB.p(), System.currentTimeMillis() - t2);

        // AES-GCM 解密业务数据
        long t3 = System.currentTimeMillis();
        byte[] iv = AESUtils.hexToIv(ivHex);
        String decrypted = AESUtils.decrypt(encryptedData, aesKeyBytes, iv);
        log.info("{}AES解密业务数据耗时: {}ms", LogPrefix.WEB.p(), System.currentTimeMillis() - t3);

        return decrypted;
    }

    /// 可替换 body 的 HttpInputMessage 包装类
    private static class DecryptedHttpInputMessage implements HttpInputMessage {

        private final HttpHeaders headers;
        private final InputStream body;

        DecryptedHttpInputMessage(HttpInputMessage original, byte[] bodyBytes) {
            this.headers = original.getHeaders();
            this.body = new ByteArrayInputStream(bodyBytes);
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
