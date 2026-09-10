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

package com.devops00.spectra.core.security.initialization.service.impl;

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.core.security.initialization.service.SystemSecretInitializer;
import com.devops00.spectra.core.security.secret.service.SecretManagementService;
import com.devops00.spectra.core.security.secret.service.SecretRuntimeService;
import com.devops00.spectra.core.system.constant.SystemConfigKeys;
import com.devops00.spectra.core.system.javabean.enums.ConfiguredValueType;
import com.devops00.spectra.core.system.security.SystemKeyMaterial;
import com.devops00.spectra.core.system.service.ConfiguredService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

/**
 * 首次系统初始化的系统内部密钥生成器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/10
 */
@Service
@RequiredArgsConstructor
public class SystemSecretInitializerImpl implements SystemSecretInitializer {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final List<String> INTERNAL_SECRET_CODES = List.of(
            "crypto.server.public-key", "crypto.server.private-key", "crypto.client.public-key",
            "crypto.client.private-key", "notification.address-encryption-key", "notification.sensitive-payload-key",
            "security.verification-code-hmac", "security.authorization-change-token-hmac",
            "security.request-integrity-hmac", "security.csrf-signing-key");

    private final SecretManagementService secretManagementService;
    private final SecretRuntimeService secretRuntimeService;
    private final ConfiguredService configuredService;

    @Override
    @Transactional
    public void initialize() {
        long activeCount = INTERNAL_SECRET_CODES.stream()
                .filter(code -> secretRuntimeService.findActive(code).isPresent())
                .count();
        if (activeCount != 0 && activeCount != INTERNAL_SECRET_CODES.size()) {
            throw new DataSaveException("系统内部密钥初始化状态不完整");
        }
        if (activeCount == 0) {
            KeyPair serverPair = generateRsaKeyPair();
            KeyPair clientPair = generateRsaKeyPair();
            publishGenerated("crypto.server.public-key", SystemKeyMaterial.publicKeyBase64(serverPair.getPublic()));
            publishGenerated("crypto.server.private-key", SystemKeyMaterial.privateKeyBase64(serverPair.getPrivate()));
            publishGenerated("crypto.client.public-key", SystemKeyMaterial.publicKeyBase64(clientPair.getPublic()));
            publishGenerated("crypto.client.private-key", SystemKeyMaterial.privateKeyBase64(clientPair.getPrivate()));
            publishGenerated("notification.address-encryption-key", randomBase64());
            publishGenerated("notification.sensitive-payload-key", randomBase64());
            publishGenerated("security.verification-code-hmac", randomBase64());
            publishGenerated("security.authorization-change-token-hmac", randomBase64());
            publishGenerated("security.request-integrity-hmac", randomBase64());
            publishGenerated("security.csrf-signing-key", randomBase64());
        }
        configuredService.upsert(SystemConfigKeys.CRYPTO_ENABLED, "false", ConfiguredValueType.BOOL,
                "密钥管理页面配置的接口加解密开关");
    }

    private void publishGenerated(String code, String value) {
        var version = secretManagementService.createPending(code, value, "GENERATED");
        secretManagementService.publish(version.getId());
    }

    private KeyPair generateRsaKeyPair() {
        try {
            return SystemKeyMaterial.generateKeyPair();
        } catch (Exception exception) {
            throw new DataSaveException("生成系统接口加解密密钥失败", exception);
        }
    }

    private String randomBase64() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
