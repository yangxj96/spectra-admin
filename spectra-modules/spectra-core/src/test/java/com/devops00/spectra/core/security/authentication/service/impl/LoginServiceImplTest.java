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

import com.devops00.spectra.security.base.change.SecurityAuthenticationPort;
import com.devops00.spectra.security.base.constant.ClientType;
import com.devops00.spectra.security.base.constant.LoginType;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.holder.SecurityUserLoader;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import com.devops00.spectra.security.base.javabean.from.LoginFrom;
import com.devops00.spectra.security.base.javabean.vo.TokenVO;
import com.devops00.spectra.security.base.mfa.SecurityMfaChallengePort;
import com.devops00.spectra.security.base.mfa.SecurityMfaVerifier;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginServiceImplTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ordinaryUserWithActiveTotpMustCompleteMfaAfterPasswordLogin() {
        UUID userId = UUID.randomUUID();
        SecurityUser user = user(userId, "user@example.com", "ROLE_USER");
        LoginDispatcher dispatcher = mock(LoginDispatcher.class);
        SecurityAuthenticationPort authenticationPort = mock(SecurityAuthenticationPort.class);
        SecurityMfaVerifier verifier = mock(SecurityMfaVerifier.class);
        SecurityMfaChallengePort challengePort = mock(SecurityMfaChallengePort.class);
        SecurityMfaChallengePort.MfaLoginChallenge challenge = new SecurityMfaChallengePort.MfaLoginChallenge(
                "challenge-id", userId, user.getUsername(), ClientType.WEB, false, false,
                System.currentTimeMillis() + 300_000);

        when(dispatcher.authenticate(any(LoginFrom.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(authenticationPort.isLockedOut(user.getUsername())).thenReturn(false);
        when(verifier.hasActiveTotp(userId)).thenReturn(true);
        when(challengePort.create(userId, user.getUsername(), ClientType.WEB, false)).thenReturn(challenge);

        LoginServiceImpl service = service(dispatcher, authenticationPort, verifier, challengePort);
        TokenVO result = service.login(passwordLogin(user.getUsername()), ClientType.WEB);

        assertTrue(result.isMfaRequired());
        assertFalse(result.isMfaEnrollmentRequired());
        verify(challengePort).create(userId, user.getUsername(), ClientType.WEB, false);
        verify(authenticationPort, never()).login(any(SecurityUser.class));
    }

    @Test
    void ordinaryUserWithoutTotpCanLoginWithoutMfa() {
        UUID userId = UUID.randomUUID();
        SecurityUser user = user(userId, "user@example.com", "ROLE_USER");
        LoginDispatcher dispatcher = mock(LoginDispatcher.class);
        SecurityAuthenticationPort authenticationPort = mock(SecurityAuthenticationPort.class);
        SecurityMfaVerifier verifier = mock(SecurityMfaVerifier.class);
        TokenVO expected = TokenVO.builder().id(userId).build();

        when(dispatcher.authenticate(any(LoginFrom.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(authenticationPort.isLockedOut(user.getUsername())).thenReturn(false);
        when(verifier.hasActiveTotp(userId)).thenReturn(false);
        when(authenticationPort.login(user)).thenReturn(expected);

        LoginServiceImpl service = service(dispatcher, authenticationPort, verifier, mockChallengePort());
        TokenVO result = service.login(passwordLogin(user.getUsername()), ClientType.WEB);

        assertFalse(result.isMfaRequired());
        verify(authenticationPort).login(user);
    }

    @Test
    void devOpsWithRevokedTotpCanLoginWithoutRebindingMfa() {
        UUID userId = UUID.randomUUID();
        SecurityUser user = user(userId, "devops@example.com", "ROLE_DEV_OPS");
        LoginDispatcher dispatcher = mock(LoginDispatcher.class);
        SecurityAuthenticationPort authenticationPort = mock(SecurityAuthenticationPort.class);
        SecurityMfaVerifier verifier = mock(SecurityMfaVerifier.class);
        TokenVO expected = TokenVO.builder().id(userId).build();

        when(dispatcher.authenticate(any(LoginFrom.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(authenticationPort.isLockedOut(user.getUsername())).thenReturn(false);
        when(verifier.hasActiveTotp(userId)).thenReturn(false);
        when(verifier.hasAnyTotpEnrollment(userId)).thenReturn(true);
        when(verifier.hasNonRevokedTotpEnrollment(userId)).thenReturn(false);
        when(authenticationPort.login(user)).thenReturn(expected);

        LoginServiceImpl service = service(dispatcher, authenticationPort, verifier, mockChallengePort());
        TokenVO result = service.login(passwordLogin(user.getUsername()), ClientType.WEB);

        assertFalse(result.isMfaRequired());
        verify(authenticationPort).login(user);
    }

    @Test
    void devOpsWithoutTotpMustCompleteInitialMfaEnrollment() {
        UUID userId = UUID.randomUUID();
        SecurityUser user = user(userId, "devops@example.com", "ROLE_DEV_OPS");
        LoginDispatcher dispatcher = mock(LoginDispatcher.class);
        SecurityAuthenticationPort authenticationPort = mock(SecurityAuthenticationPort.class);
        SecurityMfaVerifier verifier = mock(SecurityMfaVerifier.class);
        SecurityMfaChallengePort challengePort = mock(SecurityMfaChallengePort.class);
        SecurityMfaChallengePort.MfaLoginChallenge challenge = new SecurityMfaChallengePort.MfaLoginChallenge(
                "devops-enrollment-challenge", userId, user.getUsername(), ClientType.WEB, true, false,
                System.currentTimeMillis() + 300_000);

        when(dispatcher.authenticate(any(LoginFrom.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(authenticationPort.isLockedOut(user.getUsername())).thenReturn(false);
        when(verifier.hasActiveTotp(userId)).thenReturn(false);
        when(verifier.hasAnyTotpEnrollment(userId)).thenReturn(false);
        when(verifier.hasNonRevokedTotpEnrollment(userId)).thenReturn(false);
        when(challengePort.create(userId, user.getUsername(), ClientType.WEB, true)).thenReturn(challenge);

        LoginServiceImpl service = service(dispatcher, authenticationPort, verifier, challengePort);
        TokenVO result = service.login(passwordLogin(user.getUsername()), ClientType.WEB);

        assertTrue(result.isMfaRequired());
        assertTrue(result.isMfaEnrollmentRequired());
        verify(challengePort).create(userId, user.getUsername(), ClientType.WEB, true);
        verify(authenticationPort, never()).login(any(SecurityUser.class));
    }

    @Test
    void directOtpLoginDoesNotCreateASecondMfaChallenge() {
        UUID userId = UUID.randomUUID();
        SecurityUser user = user(userId, "user@example.com", "ROLE_USER");
        user.setExtraData(Map.of("mfaVerified", true, "authenticationAssurance", "AAL2"));
        LoginDispatcher dispatcher = mock(LoginDispatcher.class);
        SecurityAuthenticationPort authenticationPort = mock(SecurityAuthenticationPort.class);
        SecurityMfaVerifier verifier = mock(SecurityMfaVerifier.class);
        SecurityMfaChallengePort challengePort = mockChallengePort();
        TokenVO expected = TokenVO.builder().id(userId).build();

        when(dispatcher.authenticate(any(LoginFrom.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(authenticationPort.isLockedOut(user.getUsername())).thenReturn(false);
        when(verifier.hasActiveTotp(userId)).thenReturn(true);
        when(authenticationPort.login(user)).thenReturn(expected);

        LoginServiceImpl service = service(dispatcher, authenticationPort, verifier, challengePort);
        TokenVO result = service.login(otpLogin(user.getUsername()), ClientType.WEB);

        assertFalse(result.isMfaRequired());
        verify(challengePort, never()).create(any(), any(), any(), anyBoolean());
        verify(authenticationPort).login(user);
    }

    private LoginServiceImpl service(LoginDispatcher dispatcher, SecurityAuthenticationPort authenticationPort,
                                     SecurityMfaVerifier verifier, SecurityMfaChallengePort challengePort) {
        ObjectProvider<SecurityAuditWriter> auditProvider = mock(ObjectProvider.class);
        ObjectProvider<SecurityMfaChallengePort> challengeProvider = mock(ObjectProvider.class);
        ObjectProvider<SecurityMfaVerifier> verifierProvider = mock(ObjectProvider.class);
        ObjectProvider<SecurityUserLoader> userLoaderProvider = mock(ObjectProvider.class);
        when(auditProvider.getIfAvailable()).thenReturn(null);
        when(challengeProvider.getIfAvailable()).thenReturn(challengePort);
        when(verifierProvider.getIfAvailable()).thenReturn(verifier);
        when(userLoaderProvider.getIfAvailable()).thenReturn(null);
        return new LoginServiceImpl(dispatcher, new SecurityProperties(), auditProvider, authenticationPort,
                mock(SecurityContextAccessor.class), challengeProvider, verifierProvider, userLoaderProvider);
    }

    private static SecurityMfaChallengePort mockChallengePort() {
        return mock(SecurityMfaChallengePort.class);
    }

    private static SecurityUser user(UUID id, String username, String role) {
        SecurityUser user = new SecurityUser();
        user.setId(id);
        user.setUsername(username);
        user.setAuthorities(List.of(new SimpleGrantedAuthority(role)));
        return user;
    }

    private static LoginFrom passwordLogin(String username) {
        LoginFrom login = new LoginFrom();
        login.setType(LoginType.PASSWORD);
        login.setUsername(username);
        return login;
    }

    private static LoginFrom otpLogin(String username) {
        LoginFrom login = new LoginFrom();
        login.setType(LoginType.OTP);
        login.setUsername(username);
        return login;
    }
}
