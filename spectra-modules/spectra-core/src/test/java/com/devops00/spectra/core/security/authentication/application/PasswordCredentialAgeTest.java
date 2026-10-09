package com.devops00.spectra.core.security.authentication.application;

import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.common.security.policy.PasswordPolicy;
import com.devops00.spectra.common.security.policy.SecurityPasswordPolicyProvider;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PasswordCredentialAgeTest {

    private static final Instant NOW = Instant.parse("2026-10-07T00:00:00Z");

    @Test
    void maximumAgeExpiresAtExactBoundaryForEveryLoginMethod() {
        for (var type : LoginType.values()) {
            assertThrows(LoginException.class, () -> assemble(type, NOW.minusSeconds(86400), null, 1));
            assertDoesNotThrow(() -> assemble(type, NOW.minusSeconds(86399), null, 1));
        }
    }

    @Test
    void explicitExpirationAndAgeAreBothEnforced() {
        assertThrows(LoginException.class, () -> assemble(LoginType.PASSWORD, NOW, NOW, null));
        assertThrows(LoginException.class, () -> assemble(LoginType.PASSWORD, NOW.minusSeconds(172800), NOW.plusSeconds(3600), 1));
        assertDoesNotThrow(() -> assemble(LoginType.PASSWORD, NOW.minusSeconds(172800), null, null));
        assertThrows(LoginException.class, () -> assemble(LoginType.PASSWORD, null, null, 1));
    }

    private void assemble(LoginType type, Instant changedAt, Instant expiresAt, Integer maxAgeDays) {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setStatus(UserStatus.ACTIVE);
        var identity = new AuthenticationIdentity();
        identity.setUserId(user.getId());
        identity.setMethodCode(type.name());
        identity.setState("ACTIVE");
        var credential = new PasswordCredential();
        credential.setChangedAt(changedAt);
        credential.setExpiresAt(expiresAt);
        var converter = mock(AuthConverter.class);
        when(converter.toSecurityUser(user)).thenReturn(new SecurityUser());
        SecurityPasswordPolicyProvider policy = () -> new PasswordPolicy(8, false, false, false, false, maxAgeDays);
        var assembler = new SecurityUserAssembler(converter, mock(AuthorizationSnapshotProvider.class), policy,
                Clock.fixed(NOW, ZoneOffset.UTC));
        assembler.toSecurityUser(type, identity, credential, user);
    }
}
