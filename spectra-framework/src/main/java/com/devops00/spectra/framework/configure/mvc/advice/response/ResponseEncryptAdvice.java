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

package com.devops00.spectra.framework.configure.mvc.advice.response;


import com.devops00.spectra.common.annotation.Encrypt;
import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.utils.AESUtils;
import com.devops00.spectra.common.utils.RSAUtils;
import com.devops00.spectra.common.utils.SHA256Utils;
import com.devops00.spectra.framework.configure.mvc.properties.SMProperties;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import reactor.core.publisher.Flux;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import java.lang.reflect.Method;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/// 响应内容加密
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/6/3 10:40
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@NullMarked
public class ResponseEncryptAdvice implements ResponseBodyAdvice<Object> {

    private static final Pattern PATTERN = Pattern.compile("com\\.devops00\\.spectra\\..*\\.controller.*");

    private final ObjectMapper om;

    private final PublicKey publicKey;

    private final PrivateKey privateKey;

    public ResponseEncryptAdvice(SMProperties properties, ObjectMapper om) {
        this.om = om;
        try {
            this.publicKey = RSAUtils.restorePublicKey(properties.getPublicKey());
            this.privateKey = RSAUtils.restorePrivateKey(properties.getPrivateKey());
        } catch (Exception e) {
            throw new IllegalStateException("RSA密钥初始化失败", e);
        }
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        log.debug(LogPrefix.WEB.f("进入响应加解密处理"));

        // 忽略流式
        if (returnType.getParameterType().isAssignableFrom(Flux.class)) {
            return false;
        }

        // 忽略 ByteArrayHttpMessageConverter（避免干扰文件下载等二进制响应）
        if (converterType.isAssignableFrom(ByteArrayHttpMessageConverter.class)) {
            return false;
        }

        // 检查 @Encrypt 注解（方法级优先于类级）
        Method method = returnType.getMethod();
        if (method != null) {
            Encrypt methodAnno = AnnotatedElementUtils.findMergedAnnotation(method, Encrypt.class);
            if (methodAnno != null) {
                return methodAnno.value();
            }

            Encrypt classAnno = AnnotatedElementUtils.findMergedAnnotation(
                    method.getDeclaringClass(), Encrypt.class);
            if (classAnno != null) {
                return classAnno.value();
            }
        }

        // 兜底：包名匹配
        var declaringClass = returnType.getContainingClass();
        return PATTERN.matcher(declaringClass.getPackageName()).matches();
    }

    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body,
                                            MethodParameter returnType,
                                            MediaType contentType,
                                            Class<? extends HttpMessageConverter<?>> converterType,
                                            ServerHttpRequest request,
                                            ServerHttpResponse response) {
        // 第一：流式直接放行
        if (MediaType.TEXT_EVENT_STREAM.includes(contentType)
                || body instanceof Flux
                || Flux.class.isAssignableFrom(returnType.getParameterType())) {
            log.debug(LogPrefix.WEB.f("跳过流式响应包装"));
            return body;
        }

        // 第二：null 处理（必须放后面），直接返回 null 交给 ResponseModifyAdvice 处理
        if (body == null) {
            log.debug(LogPrefix.WEB.f("body为null，跳过加密"));
            return null;
        }

        Map<String, Object> result = new HashMap<>();

        try {
            long start = System.currentTimeMillis();

            // 随机生成AES密钥和IV
            SecretKey aesKey = AESUtils.generateKey();
            byte[] iv = AESUtils.generateIv();

            // AES-GCM加密业务数据
            long t1 = System.currentTimeMillis();
            String encryptedData = AESUtils.encrypt(om.writeValueAsString(body), aesKey, iv);
            log.info("AES加密耗时: {}ms", System.currentTimeMillis() - t1);

            // RSA-OAEP公钥加密AES密钥
            long t2 = System.currentTimeMillis();
            String encryptedAesKey = RSAUtils.encrypt(aesKey.getEncoded(), publicKey);
            log.info("RSA加密AES密钥耗时: {}ms", System.currentTimeMillis() - t2);

            // 组织待签名字符串
            long t3 = System.currentTimeMillis();
            String nonce = SHA256Utils.generateNonce();
            long timestamp = System.currentTimeMillis() / 1000;
            String signContent = String.format("data=%s&nonce=%s&timestamp=%d", encryptedData, nonce, timestamp);

            // RSA私钥签名（SHA256withRSA）
            String signature = RSAUtils.sign(signContent, privateKey);
            log.info("签名耗时: {}ms", System.currentTimeMillis() - t3);

            log.info("总耗时: {}ms", System.currentTimeMillis() - start);

            // 组装返回
            result.put("data", encryptedData);
            result.put("key", encryptedAesKey);
            result.put("iv", AESUtils.getIvHex(iv));
            result.put("nonce", nonce);
            result.put("timestamp", timestamp);
            result.put("signature", signature);

            return result;
        } catch (Exception e) {
            log.error("响应加密失败: {}", e.getMessage(), e);
            throw new com.devops00.spectra.common.exception.EncryptException("响应加密失败", e);
        }
    }

}
