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
import com.devops00.spectra.common.utils.AESUtils;
import com.devops00.spectra.common.utils.RSAUtils;
import com.devops00.spectra.common.utils.SHA256Utils;
import com.devops00.spectra.framework.web.crypto.CryptoKeyManager;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.core.io.Resource;
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

    private static final Pattern CONTROLLER_PACKAGE = Pattern.compile("com\\.devops00\\.spectra\\..*\\.controller(?:\\..*)?");

    private final ObjectMapper om;

    private final CryptoKeyManager cryptoKeyManager;

    public ResponseEncryptAdvice(CryptoKeyManager cryptoKeyManager, ObjectMapper om) {
        this.cryptoKeyManager = cryptoKeyManager;
        this.om = om;
        log.info(LogPrefix.WEB.f("接口加密 Advice 已注册（运行时由 CryptoKeyManager 控制启用/禁用）"));
    }

    /**
     * 获取或判断 Framework 的 supports 结果。
     *
     * @param returnType    控制器方法返回类型，用于判断响应加密规则。
     * @param converterType 当前 HTTP 消息转换器类型，用于判断 Advice 是否适用。
     * @return 返回响应是否满足加密条件；加密开关未就绪、显式关闭或响应类型不支持时返回 false，满足条件时返回 true。
     */
    /**
     * 按响应内容和加密策略生成加密响应。
     *
     * @param body          控制器返回的原始响应体；null 表示控制器没有响应内容。
     * @param returnType    控制器方法返回类型，用于识别流式和资源响应。
     * @param contentType   当前响应的媒体类型，用于排除流式内容。
     * @param converterType 当前 HTTP 消息转换器类型，用于保持二进制和资源响应原样返回。
     * @param request       当前 HTTP 请求，用于读取请求关联信息和加密上下文。
     * @param response      当前 HTTP 响应，用于写入加密响应所需的响应头。
     * @return 流式、资源、二进制或 null 响应原样返回；普通响应成功时返回包含密文、密钥摘要和签名的 JSON 字符串；密钥缺失或加密失败时抛出加密异常，不以空字符串掩盖失败。
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 忽略流式
        if (Flux.class.isAssignableFrom(returnType.getParameterType())) {
            log.debug(LogPrefix.WEB.f("跳过响应加密: 流式返回类型"));
            return false;
        }

        // 忽略 ByteArrayHttpMessageConverter（避免干扰文件下载等二进制响应）
        if (ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType)) {
            log.debug(LogPrefix.WEB.f("跳过响应加密: 字节数组转换器"));
            return false;
        }

        // 忽略 ResourceHttpMessageConverter（避免把文件下载响应序列化并加密）
        if (ResourceHttpMessageConverter.class.isAssignableFrom(converterType)) {
            log.debug(LogPrefix.WEB.f("跳过响应加密: Resource 转换器"));
            return false;
        }

        if (Resource.class.isAssignableFrom(returnType.getParameterType())) {
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
                return methodAnno.value() && methodAnno.response() && encryptionConfigured();
            }

            Encrypt classAnno = AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(), Encrypt.class);
            if (classAnno != null) {
                if (!classAnno.value() || !classAnno.response()) {
                    log.debug("{}跳过响应加密: @Encrypt(value={},response={}) on {}", LogPrefix.WEB.p(), classAnno.value(), classAnno.response(),
                            method.getDeclaringClass().getSimpleName());
                }
                return classAnno.value() && classAnno.response() && encryptionConfigured();
            }
        }

        if (!encryptionConfigured()) {
            log.debug(LogPrefix.WEB.f("加密已禁用"));
            return false;
        }

        // 兜底：包名匹配
        var declaringClass = returnType.getContainingClass();
        boolean matched = CONTROLLER_PACKAGE.matcher(declaringClass.getPackageName()).matches();
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
        if (MediaType.TEXT_EVENT_STREAM.includes(contentType)
                || body instanceof Flux
                || body instanceof Resource
                || (body instanceof ResponseEntity<?> entity && entity.getBody() instanceof Resource)
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

        log.debug("{}开始加密响应, body类型={}", LogPrefix.WEB.p(), body.getClass().getSimpleName());

        try {
            long start = System.currentTimeMillis();

            // 获取密钥（从 CryptoKeyManager 内存缓存）
            PublicKey clientPublicKey = cryptoKeyManager.getClientPublicKey();
            PrivateKey serverPrivateKey = cryptoKeyManager.getServerPrivateKey();
            if (clientPublicKey == null || serverPrivateKey == null) {
                throw new EncryptException("加密密钥不可用");
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
        } catch (EncryptException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("响应加密失败，correlationId={}", correlationId());
            throw new EncryptException("响应加密失败", exception);
        }
    }

    /** 返回全局开关状态，区分明确关闭和密钥暂不可用。 */
    private boolean encryptionConfigured() {
        return cryptoKeyManager.isConfiguredEnabled();
    }

    private static String correlationId() {
        String correlationId = RequestCorrelationContext.current().correlationId();
        return correlationId == null ? "unknown" : correlationId;
    }
}
