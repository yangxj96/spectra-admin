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

package com.devops00.spectra.core.security.authentication.application;

import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.common.security.policy.PasswordPolicy;
import com.devops00.spectra.common.security.policy.SecurityPasswordPolicyProvider;
import com.devops00.spectra.common.security.policy.SecurityPolicyUnavailableException;
import com.devops00.spectra.core.security.authentication.constant.LoginType;
import com.devops00.spectra.core.security.authentication.exception.LoginException;
import com.devops00.spectra.core.security.authentication.javabean.converter.AuthConverter;
import com.devops00.spectra.core.security.authentication.javabean.entity.AuthenticationIdentity;
import com.devops00.spectra.core.security.authentication.javabean.entity.PasswordCredential;
import com.devops00.spectra.core.security.authentication.javabean.entity.SecurityUser;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PasswordCredentialAgeTest {

    private static final Instant NOW = Instant.parse("2026-10-07T00:00:00Z");

    @Test
    void maximumAgeExpiresAtExactBoundaryForEveryLoginMethod() {
        for (var type : LoginType.values()) {
            assertEquals("密码已过期，请联系管理员重置密码", assertThrows(LoginException.class,
                    () -> assemble(type, NOW.minusSeconds(86400), null, 1)).getMessage());
            assertDoesNotThrow(() -> assemble(type, NOW.minusSeconds(86399), null, 1));
        }
    }

    @Test
    void explicitExpirationAndAgeAreBothEnforced() {
        assertThrows(LoginException.class, () -> assemble(LoginType.PASSWORD, NOW, NOW, null));
        assertDoesNotThrow(() -> assemble(LoginType.PASSWORD, NOW, NOW.plusSeconds(1), null));
        assertThrows(LoginException.class, () -> assemble(LoginType.PASSWORD, NOW.minusSeconds(172800), NOW.plusSeconds(3600), 1));
        assertDoesNotThrow(() -> assemble(LoginType.PASSWORD, NOW.minusSeconds(172800), null, null));
        assertThrows(LoginException.class, () -> assemble(LoginType.PASSWORD, null, null, 1));
        assertThrows(LoginException.class, () -> assemble(LoginType.PASSWORD, NOW.plusSeconds(1), null, 1));
    }

    @Test
    void policyChangeIsAppliedOnNextAssemblyAndUnavailablePolicyRejects() {
        var policy = new AtomicReference<>(new PasswordPolicy(8, false, false, false, false, null));
        var assembler = assembler(policy::get);
        var user = user();
        var identity = identity(LoginType.PASSWORD, user);
        var credential = credential(NOW.minusSeconds(172800), null);

        assertTrue(assembler.toSecurityUser(LoginType.PASSWORD, identity, credential, user).isCredentialsNonExpired());
        policy.set(new PasswordPolicy(8, false, false, false, false, 1));
        assertThrows(LoginException.class,
                () -> assembler.toSecurityUser(LoginType.PASSWORD, identity, credential, user));
        policy.set(null);
        assertThrows(SecurityPolicyUnavailableException.class,
                () -> assembler.toSecurityUser(LoginType.PASSWORD, identity, credential, user));
    }

    private void assemble(LoginType type, Instant changedAt, Instant expiresAt, Integer maxAgeDays) {
        var user = user();
        var identity = identity(type, user);
        assembler(() -> new PasswordPolicy(8, false, false, false, false, maxAgeDays))
                .toSecurityUser(type, identity, credential(changedAt, expiresAt), user);
    }

    private User user() {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private AuthenticationIdentity identity(LoginType type, User user) {
        var identity = new AuthenticationIdentity();
        identity.setUserId(user.getId());
        identity.setMethodCode(type.name());
        identity.setState("ACTIVE");
        return identity;
    }

    private PasswordCredential credential(Instant changedAt, Instant expiresAt) {
        var credential = new PasswordCredential();
        credential.setChangedAt(changedAt);
        credential.setExpiresAt(expiresAt);
        return credential;
    }

    private SecurityUserAssembler assembler(SecurityPasswordPolicyProvider policy) {
        var converter = mock(AuthConverter.class);
        when(converter.toSecurityUser(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(new SecurityUser());
        return new SecurityUserAssembler(converter, mock(AuthorizationSnapshotProvider.class), policy,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }
}
