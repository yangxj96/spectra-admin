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

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.core.security.authentication.javabean.entity.AuthenticationIdentity;
import com.devops00.spectra.core.security.authentication.mapper.AuthenticationIdentityMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证 {@code AuthenticationIdentityServiceImplTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class AuthenticationIdentityServiceImplTest {

    @Test
    void createsNonPasswordIdentityWithoutPersistingTheIdentifier() {
        var mapper = mock(AuthenticationIdentityMapper.class);
        when(mapper.selectOne(any())).thenReturn(null);
        when(mapper.insert(any(AuthenticationIdentity.class))).thenReturn(1);
        var service = new AuthenticationIdentityServiceImpl(mapper);
        var userId = UUID.randomUUID();

        var identity = service.createIdentity(userId, "SMS", "13800138000");

        assertEquals(userId, identity.getUserId());
        assertEquals("SMS", identity.getMethodCode());
        assertEquals("LOCAL", identity.getProviderCode());
        assertEquals("ACTIVE", identity.getState());
        assertEquals(64, identity.getIdentifierHash().length());
        org.mockito.Mockito.verify(mapper).insert(any(AuthenticationIdentity.class));
    }

    @Test
    void refusesToTakeAnIdentityOwnedByAnotherUser() {
        var mapper = mock(AuthenticationIdentityMapper.class);
        var existing = new AuthenticationIdentity();
        existing.setUserId(UUID.randomUUID());
        when(mapper.selectOne(any())).thenReturn(existing);
        var service = new AuthenticationIdentityServiceImpl(mapper);

        assertThrows(DataSaveException.class,
                () -> service.createIdentity(UUID.randomUUID(), "EMAIL", "user@example.com"));
    }

    @Test
    void listsOnlyTheIdentityRowsReturnedByTheTargetMapper() {
        var mapper = mock(AuthenticationIdentityMapper.class);
        var userId = UUID.randomUUID();
        var identity = new AuthenticationIdentity();
        identity.setUserId(userId);
        when(mapper.selectList(any())).thenReturn(List.of(identity));
        var service = new AuthenticationIdentityServiceImpl(mapper);

        assertEquals(1, service.listByUserId(userId).size());
        verify(mapper).selectList(any());
    }

    @Test
    void revokesOnlyAnIdentityOwnedByTheRequestedUser() {
        var mapper = mock(AuthenticationIdentityMapper.class);
        var userId = UUID.randomUUID();
        var identityId = UUID.randomUUID();
        var identity = new AuthenticationIdentity();
        identity.setId(identityId);
        identity.setUserId(userId);
        identity.setState("ACTIVE");
        when(mapper.selectOne(any())).thenReturn(identity);
        when(mapper.updateById(identity)).thenReturn(1);
        var service = new AuthenticationIdentityServiceImpl(mapper);

        service.revokeByUserIdAndId(userId, identityId);

        assertEquals("REVOKED", identity.getState());
        verify(mapper).updateById(identity);
    }
}
