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

package com.devops00.spectra.core.security.secret.service;

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.exception.SecuritySecretUnavailableException;
import com.devops00.spectra.common.port.security.SecretValueCipher;
import com.devops00.spectra.core.security.secret.javabean.entity.SecretDefinitionEntity;
import com.devops00.spectra.core.security.secret.javabean.entity.SecretVersionEntity;
import com.devops00.spectra.core.security.secret.service.impl.SecretManagementServiceImpl;
import com.devops00.spectra.core.security.secret.mapper.SecretDefinitionMapper;
import com.devops00.spectra.core.security.secret.mapper.SecretVersionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/** 密钥版本服务的状态和 fail-closed 契约测试。 */
@ExtendWith(MockitoExtension.class)
class SecretManagementServiceTest {

    @Mock
    private SecretDefinitionMapper definitionMapper;
    @Mock
    private SecretVersionMapper versionMapper;
    @Mock
    private SecretValueCipher cipher;

    private SecretManagementServiceImpl service;
    private SecretDefinitionEntity definition;

    @BeforeEach
    void setUp() {
        service = new SecretManagementServiceImpl(definitionMapper, versionMapper, cipher);
        definition = new SecretDefinitionEntity();
        definition.setId(UUID.randomUUID());
        definition.setCode("security.test-key");
        definition.setMutable(true);
        lenient().when(definitionMapper.selectOne(ArgumentMatchers.any())).thenReturn(definition);
    }

    @Test
    void createsPendingVersionAndNeverReturnsPlaintextField() {
        var encrypted = new SecretValueCipher.EncryptedValue("AES-256-GCM", new byte[12], new byte[]{1, 2});
        when(versionMapper.selectOne(ArgumentMatchers.any())).thenReturn(null);
        when(cipher.encrypt("security.test-key", "secret-value")).thenReturn(encrypted);

        SecretVersionEntity result = service.createPending("security.test-key", "secret-value", "MANUAL");

        assertEquals(1, result.getVersionNo());
        assertEquals("PENDING", result.getState());
        assertNotNull(result.getFingerprint());
        verify(versionMapper).insert(ArgumentMatchers.any(SecretVersionEntity.class));
    }

    @Test
    void publishesPendingVersionAndRejectsRepeatedPublish() {
        var target = new SecretVersionEntity();
        target.setId(UUID.randomUUID());
        target.setSecretDefinitionId(definition.getId());
        target.setState("PENDING");
        when(versionMapper.selectById(target.getId())).thenReturn(target);
        when(versionMapper.update(ArgumentMatchers.isNull(), ArgumentMatchers.any())).thenReturn(1);

        service.publish(target.getId());

        target.setState("ACTIVE");
        assertThrows(DataSaveException.class, () -> service.publish(target.getId()));
    }

    @Test
    void databaseFailureDuringActiveLookupFailsClosed() {
        when(versionMapper.selectOne(ArgumentMatchers.any()))
                .thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(SecuritySecretUnavailableException.class,
                () -> service.findActive("security.test-key"));
    }
}
