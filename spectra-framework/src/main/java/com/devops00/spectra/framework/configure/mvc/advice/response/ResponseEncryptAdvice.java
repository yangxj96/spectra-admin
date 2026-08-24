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
import com.devops00.spectra.common.exception.EncryptException;
import com.devops00.spectra.common.utils.AESUtils;
import com.devops00.spectra.common.utils.RSAUtils;
import com.devops00.spectra.common.utils.SHA256Utils;
import com.devops00.spectra.framework.configure.mvc.crypto.CryptoKeyManager;
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
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
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

/**
 * 响应内容加密
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/6/3 10:40
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@NullMarked
public class ResponseEncryptAdvice implements ResponseBodyAdvice<Object> {

    private static final Pattern PATTERN = Pattern.compile("com\\.devops00\\.spectra\\..*\\.controller.*");

    private final ObjectMapper om;

    private final CryptoKeyManager cryptoKeyManager;

    public ResponseEncryptAdvice(CryptoKeyManager cryptoKeyManager, ObjectMapper om) {
        this.cryptoKeyManager = cryptoKeyManager;
        this.om = om;
        log.info(LogPrefix.WEB.f("接口加密 Advice 已注册（运行时由 CryptoKeyManager 控制启用/禁用）"));
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 检查加解密是否启用
        if (!cryptoKeyManager.isEnabled()) {
            log.debug(LogPrefix.WEB.f("加密已禁用"));
            return false;
        }

        // 忽略流式
        if (returnType.getParameterType().isAssignableFrom(Flux.class)) {
            log.debug(LogPrefix.WEB.f("跳过响应加密: 流式返回类型"));
            return false;
        }

        // 忽略 ByteArrayHttpMessageConverter（避免干扰文件下载等二进制响应）
        if (converterType.isAssignableFrom(ByteArrayHttpMessageConverter.class)) {
            log.debug(LogPrefix.WEB.f("跳过响应加密: 字节数组转换器"));
            return false;
        }

        // 忽略 ResourceHttpMessageConverter（避免把文件下载响应序列化并加密）
        if (converterType.isAssignableFrom(ResourceHttpMessageConverter.class)) {
            log.debug(LogPrefix.WEB.f("跳过响应加密: Resource 转换器"));
            return false;
        }

        // 检查 @Encrypt 注解（方法级优先于类级）
        Method method = returnType.getMethod();
        if (method != null) {
            Encrypt methodAnno = AnnotatedElementUtils.findMergedAnnotation(method, Encrypt.class);
            if (methodAnno != null) {
                if (!methodAnno.value() || !methodAnno.response()) {
                    log.debug("{}跳过响应加密: @Encrypt(value={},response={}) on {}", LogPrefix.WEB.p(), methodAnno.value(), methodAnno.response(),
                            method.getName());
                }
                return methodAnno.value() && methodAnno.response();
            }

            Encrypt classAnno = AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(), Encrypt.class);
            if (classAnno != null) {
                if (!classAnno.value() || !classAnno.response()) {
                    log.debug("{}跳过响应加密: @Encrypt(value={},response={}) on {}", LogPrefix.WEB.p(), classAnno.value(), classAnno.response(),
                            method.getDeclaringClass().getSimpleName());
                }
                return classAnno.value() && classAnno.response();
            }
        }

        // 兜底：包名匹配
        var declaringClass = returnType.getContainingClass();
        boolean matched = PATTERN.matcher(declaringClass.getPackageName()).matches();
        if (!matched) {
            log.debug("{}跳过响应加密: 包名不匹配 {}", LogPrefix.WEB.p(), declaringClass.getPackageName());
        }
        return matched;
    }

    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType contentType,
                                            Class<? extends HttpMessageConverter<?>> converterType, ServerHttpRequest request,
                                            ServerHttpResponse response) {
        // 第一：流式直接放行
        if (MediaType.TEXT_EVENT_STREAM.includes(contentType) || body instanceof Flux || Flux.class.isAssignableFrom(returnType.getParameterType())) {
            log.debug(LogPrefix.WEB.f("跳过流式响应包装"));
            return body;
        }

        // 第二：null 处理（必须放后面），直接返回 null 交给 ResponseModifyAdvice 处理
        if (body == null) {
            log.debug(LogPrefix.WEB.f("body为null，跳过加密"));
            return null;
        }

        Map<String, Object> result = new HashMap<>();

        log.debug("{}开始加密响应, body类型={}", LogPrefix.WEB.p(), body.getClass().getSimpleName());

        try {
            long start = System.currentTimeMillis();

            // 获取密钥（从 CryptoKeyManager 内存缓存）
            PublicKey clientPublicKey = cryptoKeyManager.getClientPublicKey();
            PrivateKey serverPrivateKey = cryptoKeyManager.getServerPrivateKey();
            if (clientPublicKey == null || serverPrivateKey == null) {
                log.warn(LogPrefix.WEB.f("密钥不完整，跳过加密"));
                return body;
            }

            // 随机生成AES密钥和IV
            SecretKey aesKey = AESUtils.generateKey();
            byte[] iv = AESUtils.generateIv();

            // AES-GCM加密业务数据
            long t1 = System.currentTimeMillis();
            String encryptedData = AESUtils.encrypt(om.writeValueAsString(body), aesKey, iv);
            log.debug("{}AES加密耗时: {}ms", LogPrefix.WEB.p(), System.currentTimeMillis() - t1);

            // RSA-OAEP客户端公钥加密AES密钥
            long t2 = System.currentTimeMillis();
            String encryptedAesKey = RSAUtils.encrypt(aesKey.getEncoded(), clientPublicKey);
            log.debug("{}RSA加密AES密钥耗时: {}ms", LogPrefix.WEB.p(), System.currentTimeMillis() - t2);

            // 组织待签名字符串
            long t3 = System.currentTimeMillis();
            String nonce = SHA256Utils.generateNonce();
            long timestamp = System.currentTimeMillis() / 1000;
            String signContent = String.format("data=%s&nonce=%s&timestamp=%d", encryptedData, nonce, timestamp);

            // RSA私钥签名（SHA256withRSA）
            String signature = RSAUtils.sign(signContent, serverPrivateKey);
            log.debug("{}签名耗时: {}ms", LogPrefix.WEB.p(), System.currentTimeMillis() - t3);

            log.debug("{}响应加密总耗时: {}ms", LogPrefix.WEB.p(), System.currentTimeMillis() - start);

            // 组装返回
            result.put("data", encryptedData);
            result.put("key", encryptedAesKey);
            result.put("iv", AESUtils.getIvHex(iv));
            result.put("nonce", nonce);
            result.put("timestamp", timestamp);
            result.put("signature", signature);

            log.debug("{}响应加密完成, 返回字段={data,key,iv,nonce,timestamp,signature}", LogPrefix.WEB.p());
            return result;
        } catch (Exception e) {
            log.error("响应加密失败: {}", e.getMessage(), e);
            throw new EncryptException("响应加密失败", e);
        }
    }
}
