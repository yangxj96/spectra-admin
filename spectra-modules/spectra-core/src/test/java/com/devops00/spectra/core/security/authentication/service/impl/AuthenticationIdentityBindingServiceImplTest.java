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

import com.devops00.spectra.common.exception.SpectraException;
import com.devops00.spectra.core.security.authentication.javabean.entity.AuthenticationIdentity;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationIdentityBindingServiceImplTest {

    @Test
    void refusesToUnbindThePasswordIdentity() {
        var identityService = mock(AuthenticationIdentityService.class);
        var userId = UUID.randomUUID();
        var password = identity("PASSWORD", userId);
        when(identityService.listByUserId(userId)).thenReturn(List.of(password));
        var service = new AuthenticationIdentityBindingServiceImpl(mock(RedisTemplate.class), mock(SecurityProperties.class), identityService);

        assertThrows(SpectraException.class, () -> service.unbind(userId, password.getId()));
    }

    @Test
    void revokesOnlyTheSelectedNonPasswordIdentity() {
        var identityService = mock(AuthenticationIdentityService.class);
        var userId = UUID.randomUUID();
        var password = identity("PASSWORD", userId);
        var phone = identity("SMS", userId);
        when(identityService.listByUserId(userId)).thenReturn(List.of(password, phone));
        var service = new AuthenticationIdentityBindingServiceImpl(mock(RedisTemplate.class), mock(SecurityProperties.class), identityService);

        service.unbind(userId, phone.getId());

        verify(identityService).revokeByUserIdAndId(userId, phone.getId());
    }

    private static AuthenticationIdentity identity(String methodCode, UUID userId) {
        var identity = new AuthenticationIdentity();
        identity.setId(UUID.randomUUID());
        identity.setUserId(userId);
        identity.setMethodCode(methodCode);
        identity.setState("ACTIVE");
        return identity;
    }
}
