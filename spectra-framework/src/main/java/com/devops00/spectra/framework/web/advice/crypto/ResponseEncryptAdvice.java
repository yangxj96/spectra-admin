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
import com.devops00.spectra.common.security.crypto.symmetric.AESUtils;
import com.devops00.spectra.common.security.crypto.asymmetric.RSAUtils;
import com.devops00.spectra.common.security.crypto.digest.SHA256Utils;
import com.devops00.spectra.framework.web.crypto.CryptoKeyManager;
import com.devops00.spectra.framework.web.response.R;
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

    /** 默认只覆盖项目 Controller，具体接口仍可通过 {@link Encrypt} 注解细化加密范围。 */
    private static final Pattern CONTROLLER_PACKAGE = Pattern.compile("com\\.devops00\\.spectra\\..*\\.controller(?:\\..*)?");

    private final ObjectMapper om;

    private final CryptoKeyManager cryptoKeyManager;

    public ResponseEncryptAdvice(CryptoKeyManager cryptoKeyManager, ObjectMapper om) {
        this.cryptoKeyManager = cryptoKeyManager;
        this.om = om;
        log.info(LogPrefix.WEB.f("接口加密 Advice 已注册（运行时由 CryptoKeyManager 控制启用/禁用）"));
    }

    /**
     * 判断当前 Advice 是否适用于指定控制器响应。
     *
     * @param returnType    控制器方法返回类型，用于判断响应加密规则。
     * @param converterType 当前 HTTP 消息转换器类型，用于判断 Advice 是否适用。
     * @return 响应满足加密条件时返回 true；开关关闭、密钥未就绪或响应类型不支持时返回 false。
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 流式响应无法作为单个对象加密，必须交给流式写出链路处理。
        if (Flux.class.isAssignableFrom(returnType.getParameterType())) {
            log.debug(LogPrefix.WEB.f("跳过响应加密: 流式返回类型"));
            return false;
        }

        // 统一响应和 ResponseEntity 已经表达了完整响应语义，尤其不能加密错误响应。
        if (R.class.isAssignableFrom(returnType.getParameterType())
                || ResponseEntity.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }

        // 二进制响应保持原始字节，否则文件下载内容会被序列化或加密破坏。
        if (ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType)) {
            log.debug(LogPrefix.WEB.f("跳过响应加密: 字节数组转换器"));
            return false;
        }

        // Resource 响应由资源转换器直接写出，不进入 JSON 加密协议。
        if (ResourceHttpMessageConverter.class.isAssignableFrom(converterType)) {
            log.debug(LogPrefix.WEB.f("跳过响应加密: Resource 转换器"));
            return false;
        }

        if (Resource.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }

        // 方法级注解优先于类级注解；显式关闭时不能被包名兜底规则重新打开。
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

        // 没有显式注解时，使用项目 Controller 包作为默认加密边界。
        var declaringClass = returnType.getContainingClass();
        boolean matched = CONTROLLER_PACKAGE.matcher(declaringClass.getPackageName()).matches();
        if (!matched) {
            log.debug("{}跳过响应加密: 包名不匹配 {}", LogPrefix.WEB.p(), declaringClass.getPackageName());
        }
        return matched;
    }

    /**
     * 按响应内容和加密策略生成加密响应。
     *
     * @param body          控制器返回的原始响应体；null 表示控制器没有响应内容。
     * @param returnType    控制器方法返回类型，用于识别流式和资源响应。
     * @param contentType   当前响应的媒体类型，用于排除流式内容。
     * @param converterType 当前 HTTP 消息转换器类型，用于保持二进制和资源响应原样返回。
     * @param request       当前 HTTP 请求，用于读取请求关联信息和加密上下文。
     * @param response      当前 HTTP 响应，用于写入加密响应所需的响应头。
     * @return 不需要加密的响应原样返回；普通响应加密成功时返回密文 JSON，密钥缺失或加密失败时抛出加密异常。
     */
    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType contentType,
                                            Class<? extends HttpMessageConverter<?>> converterType, ServerHttpRequest request,
                                            ServerHttpResponse response) {
        // 流式、资源、统一响应和异常响应原样放行，避免破坏已有协议或重复加密。
        if (MediaType.TEXT_EVENT_STREAM.includes(contentType)
                || body instanceof Flux
                || body instanceof Resource
                || body instanceof R<?>
                || body instanceof ResponseEntity<?>
                || Flux.class.isAssignableFrom(returnType.getParameterType())) {
            log.debug(LogPrefix.WEB.f("跳过流式响应包装"));
            return body;
        }

        // 空响应交给 ResponseModifyAdvice 处理 HTTP 状态和统一响应格式。
        if (body == null) {
            log.debug(LogPrefix.WEB.f("body为null，跳过加密"));
            return null;
        }

        Map<String, Object> result = new HashMap<>();

        log.debug("{}开始加密响应, body类型={}", LogPrefix.WEB.p(), body.getClass().getSimpleName());

        try {
            long start = System.currentTimeMillis();

            // 获取密钥（从 CryptoKeyManager 内存缓存）；密钥不可用属于配置/基础设施异常。
            PublicKey clientPublicKey = cryptoKeyManager.getClientPublicKey();
            PrivateKey serverPrivateKey = cryptoKeyManager.getServerPrivateKey();
            if (clientPublicKey == null || serverPrivateKey == null) {
                throw new EncryptException("加密密钥不可用");
            }

            // 每个响应使用独立 AES 密钥和 IV，避免重复使用对称加密随机量。
            SecretKey aesKey = AESUtils.generateKey();
            byte[] iv = AESUtils.generateIv();

            // AES-GCM 加密业务数据，密文不会直接暴露原始响应内容。
            long t1 = System.currentTimeMillis();
            String encryptedData = AESUtils.encrypt(om.writeValueAsString(body), aesKey, iv);
            log.debug("{}AES加密耗时: {}ms", LogPrefix.WEB.p(), System.currentTimeMillis() - t1);

            // 使用客户端公钥封装本次响应的 AES 密钥，只有对应客户端私钥可以解开。
            long t2 = System.currentTimeMillis();
            String encryptedAesKey = RSAUtils.encrypt(aesKey.getEncoded(), clientPublicKey);
            log.debug("{}RSA加密AES密钥耗时: {}ms", LogPrefix.WEB.p(), System.currentTimeMillis() - t2);

            // 签名覆盖密文、随机数和时间戳，客户端可据此校验完整性和新鲜度。
            long t3 = System.currentTimeMillis();
            String nonce = SHA256Utils.generateNonce();
            long timestamp = System.currentTimeMillis() / 1000;
            String signContent = String.format("data=%s&nonce=%s&timestamp=%d", encryptedData, nonce, timestamp);

            // 服务端私钥签名（SHA256withRSA），客户端使用服务端公钥验证。
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

    /**
     * 处理关联标识相关数据。
     */
    private static String correlationId() {
        String correlationId = RequestCorrelationContext.current().correlationId();
        return correlationId == null ? "unknown" : correlationId;
    }
}
