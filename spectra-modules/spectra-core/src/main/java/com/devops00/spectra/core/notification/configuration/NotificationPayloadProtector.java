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

package com.devops00.spectra.core.notification.configuration;

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.exception.EncryptException;
import com.devops00.spectra.common.utils.AESUtils;
import com.devops00.spectra.core.notification.properties.NotificationModuleProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 通知地址和敏感载荷的 AES-GCM 保护器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Component
@RequiredArgsConstructor
public class NotificationPayloadProtector {

    /**
     * 当前载荷保护格式版本。
     */
    private static final String VERSION = "v1";

    /**
     * 通知模块密钥配置。
     */
    private final NotificationModuleProperties properties;

    /**
     * 敏感参数序列化器。
     */
    private final ObjectMapper objectMapper;

    /**
     * 保护外部渠道地址。
     */
    public String protectAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new DataSaveException("通知收件地址不能为空");
        }
        return protect(address, properties.addressEncryptionKey(), "通知地址");
    }

    /**
     * 解密外部渠道地址；解密失败时直接阻断当前投递。
     */
    public String unprotectAddress(String ciphertext) {
        return unprotect(ciphertext, properties.addressEncryptionKey(), "通知地址");
    }

    /**
     * 保护敏感参数对象。
     */
    public String protectParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return null;
        }
        try {
            return protect(objectMapper.writeValueAsString(parameters), properties.sensitivePayloadKey(), "通知敏感载荷");
        } catch (JacksonException exception) {
            throw new EncryptException("通知敏感载荷序列化失败", exception);
        }
    }

    /**
     * 解密 Provider 投递所需的敏感参数；失败时由调用方转换为明确的阻断结果。
     *
     * @param ciphertext 敏感参数密文
     * @return 敏感参数
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> unprotectParameters(String ciphertext) {
        var plainText = unprotect(ciphertext, properties.sensitivePayloadKey(), "通知敏感载荷");
        try {
            Map<?, ?> map = objectMapper.readValue(plainText, Map.class);
            if (map == null) {
                throw new DataSaveException("通知敏感载荷格式不正确");
            }
            var result = new LinkedHashMap<String, Object>();
            map.forEach((key, item) -> {
                if (key != null) {
                    result.put(String.valueOf(key), item);
                }
            });
            return result;
        } catch (JacksonException exception) {
            throw new EncryptException("通知敏感载荷解析失败", exception);
        }
    }

    /**
     * 保护 Provider Secret；Secret 只允许以密文形式进入运行时配置存储。
     */
    public String protectSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new DataSaveException("Provider Secret 不能为空");
        }
        return protect(secret, properties.sensitivePayloadKey(), "Provider Secret");
    }

    /**
     * 解密 Provider Secret；解密失败时直接阻断渠道使用。
     */
    public String unprotectSecret(String ciphertext) {
        return unprotect(ciphertext, properties.sensitivePayloadKey(), "Provider Secret");
    }

    /**
     * 使用指定配置密钥解密载荷。
     */
    private String unprotect(String ciphertext, String encodedKey, String name) {
        if (ciphertext == null || ciphertext.isBlank()) {
            throw new DataSaveException(name + "未配置");
        }
        try {
            var parts = ciphertext.split(":", 3);
            if (parts.length != 3 || !VERSION.equals(parts[0])) {
                throw new DataSaveException(name + "密文格式不正确");
            }
            var keyBytes = decodeKey(encodedKey, name);
            return AESUtils.decrypt(parts[2], new SecretKeySpec(keyBytes, "AES"), AESUtils.hexToIv(parts[1]));
        } catch (DataSaveException | EncryptException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new EncryptException(name + "解密失败", exception);
        }
    }

    /**
     * 使用配置密钥对文本执行 AES-GCM 加密。
     */
    private String protect(String plainText, String encodedKey, String name) {
        try {
            var keyBytes = decodeKey(encodedKey, name);
            var iv = AESUtils.generateIv();
            var cipherText = AESUtils.encrypt(plainText, new SecretKeySpec(keyBytes, "AES"), iv);
            return VERSION + ":" + AESUtils.getIvHex(iv) + ":" + cipherText;
        } catch (RuntimeException exception) {
            if (exception instanceof DataSaveException || exception instanceof EncryptException) {
                throw exception;
            }
            throw new EncryptException(name + "加密失败", exception);
        } catch (Exception exception) {
            throw new EncryptException(name + "加密失败", exception);
        }
    }

    /**
     * 解码并校验 AES 密钥长度。
     */
    private byte[] decodeKey(String encodedKey, String name) {
        if (encodedKey == null || encodedKey.isBlank()) {
            throw new DataSaveException(name + "加密密钥未配置");
        }
        final byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(encodedKey);
        } catch (IllegalArgumentException exception) {
            throw new DataSaveException(name + "加密密钥格式不正确", exception);
        }
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new DataSaveException(name + "加密密钥长度不正确");
        }
        return keyBytes;
    }
}
