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

package com.devops00.spectra.core.security.initialization.service;

import com.devops00.spectra.core.security.secret.javabean.entity.SecretVersionEntity;
import com.devops00.spectra.core.security.secret.service.SecretManagementService;
import com.devops00.spectra.core.security.secret.service.SecretRuntimeService;
import com.devops00.spectra.core.system.javabean.enums.ConfiguredValueType;
import com.devops00.spectra.core.system.service.ConfiguredService;
import com.devops00.spectra.core.security.initialization.service.impl.SystemSecretInitializerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 首次系统初始化生成全部内部密钥的测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@ExtendWith(MockitoExtension.class)
class SystemSecretInitializerTest {

    @Mock
    private SecretManagementService secretManagementService;
    @Mock
    private SecretRuntimeService secretRuntimeService;
    @Mock
    private ConfiguredService configuredService;

    private SystemSecretInitializerImpl initializer;

    @BeforeEach
    void setUp() {
        initializer = new SystemSecretInitializerImpl(secretManagementService, secretRuntimeService, configuredService);
        when(secretRuntimeService.findActive(anyString())).thenReturn(Optional.empty());
        when(secretManagementService.createPending(anyString(), anyString(), eq("GENERATED")))
                .thenAnswer(invocation -> {
                    var version = new SecretVersionEntity();
                    version.setId(UUID.randomUUID());
                    return version;
                });
    }

    @Test
    void generatesAllInternalSecretsAndLeavesCryptoDisabled() {
        initializer.initialize();

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(secretManagementService, times(10)).createPending(codeCaptor.capture(), anyString(), eq("GENERATED"));
        assertThat(codeCaptor.getAllValues()).containsExactlyInAnyOrder(
                "crypto.server.public-key", "crypto.server.private-key", "crypto.client.public-key",
                "crypto.client.private-key", "notification.address-encryption-key",
                "notification.sensitive-payload-key", "security.verification-code-hmac",
                "security.authorization-change-token-hmac", "security.request-integrity-hmac",
                "security.csrf-signing-key");
        verify(secretManagementService, times(10)).publish(any(UUID.class));
        verify(configuredService).upsert("crypto.enabled", "false", ConfiguredValueType.BOOL,
                "密钥管理页面配置的接口加解密开关");
    }
}
