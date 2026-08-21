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

package com.devops00.spectra.core.security.authentication.service.impl;

import com.devops00.spectra.core.security.authentication.javabean.entity.MfaEnrollment;
import com.devops00.spectra.core.security.authentication.javabean.entity.TotpCredential;
import com.devops00.spectra.core.security.authentication.mapper.MfaEnrollmentMapper;
import com.devops00.spectra.core.security.authentication.mapper.RecoveryCodeMapper;
import com.devops00.spectra.core.security.authentication.mapper.TotpCredentialMapper;
import com.devops00.spectra.core.system.constant.SystemConfigKeys;
import com.devops00.spectra.core.system.service.ConfiguredService;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import org.junit.jupiter.api.Test;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MfaServiceImplTest {

    @Test
    void usesUserLoginAccountInTotpProvisioningUri() {
        var enrollmentMapper = mock(MfaEnrollmentMapper.class);
        var credentialMapper = mock(TotpCredentialMapper.class);
        var recoveryCodeMapper = mock(RecoveryCodeMapper.class);
        var userMapper = mock(UserMapper.class);
        var configuredService = mock(ConfiguredService.class);
        var securityAuditWriter = mock(SecurityAuditWriter.class);
        var properties = new SecurityProperties();
        properties.setMfaEncryptionKey("01234567890123456789012345678901");
        properties.setMfaEncryptionKeyVersion("v1");
        properties.setMfaTotpIssuer("Spectra");

        UUID userId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();
        User user = new User();
        user.setEmail("devops00.com");
        user.setUsername("DEV_OPS");
        when(userMapper.selectById(userId)).thenReturn(user);
        when(configuredService.findValue(SystemConfigKeys.SYSTEM_NAME)).thenReturn(Optional.of("DevOps00"));
        when(enrollmentMapper.insert(any(MfaEnrollment.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, MfaEnrollment.class).setId(enrollmentId);
            return 1;
        });
        when(credentialMapper.insert(any(TotpCredential.class))).thenReturn(1);

        var service = new MfaServiceImpl(enrollmentMapper, credentialMapper, recoveryCodeMapper, userMapper,
                configuredService, properties, securityAuditWriter);

        String provisioningUri = service.beginTotpEnrollment(userId).provisioningUri();
        String decodedUri = URLDecoder.decode(provisioningUri, StandardCharsets.UTF_8);

        assertTrue(decodedUri.contains("DevOps00:devops00.com"));
        assertFalse(decodedUri.contains(userId.toString()));
    }
}
