/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.devops00.spectra.core.security.authentication.service.impl;

import com.devops00.spectra.core.security.authentication.application.SecurityUserAssembler;
import com.devops00.spectra.core.security.authentication.constant.LoginType;
import com.devops00.spectra.core.security.authentication.javabean.entity.AuthenticationIdentity;
import com.devops00.spectra.core.security.authentication.javabean.entity.PasswordCredential;
import com.devops00.spectra.core.security.authentication.javabean.entity.SecurityUser;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.core.security.authentication.service.PasswordCredentialService;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.service.UserService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Session 数据库重载使用可登录的本地身份，并拒绝只有外部身份的用户。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/13
 */
class DatabaseSecurityUserLoaderTest {

    private final UserService users = mock(UserService.class);
    private final AuthenticationIdentityService identities = mock(AuthenticationIdentityService.class);
    private final PasswordCredentialService credentials = mock(PasswordCredentialService.class);
    private final SecurityUserAssembler assembler = mock(SecurityUserAssembler.class);
    private final DatabaseSecurityUserLoader loader = new DatabaseSecurityUserLoader(
            users, identities, credentials, assembler);

    @Test
    void reloadsSmsIdentityWhenPasswordIdentityIsAbsent() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        PasswordCredential credential = mock(PasswordCredential.class);
        SecurityUser expected = mock(SecurityUser.class);
        AuthenticationIdentity external = identity("EMAIL", "EXTERNAL");
        AuthenticationIdentity sms = identity("SMS", "LOCAL");
        when(users.getById(userId)).thenReturn(user);
        when(user.getUsername()).thenReturn("synthetic-user");
        when(credentials.getByUserId(userId)).thenReturn(credential);
        when(identities.listByUserId(userId)).thenReturn(List.of(external, sms));
        when(assembler.toSecurityUser(LoginType.SMS, sms, credential, user)).thenReturn(expected);

        assertSame(expected, loader.load(userId));
        verify(assembler).toSecurityUser(LoginType.SMS, sms, credential, user);
    }

    @Test
    void rejectsIdentityThatCannotBeUsedByLocalLoginProviders() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(users.getById(userId)).thenReturn(user);
        when(user.getUsername()).thenReturn("synthetic-user");
        when(credentials.getByUserId(userId)).thenReturn(mock(PasswordCredential.class));
        when(identities.listByUserId(userId)).thenReturn(List.of(identity("EMAIL", "EXTERNAL"),
                identity("UNKNOWN", "LOCAL")));

        assertNull(loader.load(userId));
        verifyNoInteractions(assembler);
    }

    private AuthenticationIdentity identity(String method, String provider) {
        var identity = new AuthenticationIdentity();
        identity.setMethodCode(method);
        identity.setProviderCode(provider);
        return identity;
    }
}
