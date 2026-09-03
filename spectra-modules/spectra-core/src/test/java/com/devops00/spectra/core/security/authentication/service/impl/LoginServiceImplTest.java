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

import com.devops00.spectra.common.port.security.SecurityAuthenticationPort;
import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.core.security.authentication.constant.LoginType;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.security.authentication.javabean.entity.SecurityUser;
import com.devops00.spectra.core.security.authentication.javabean.from.LoginFrom;
import com.devops00.spectra.common.port.security.SecurityToken;
import com.devops00.spectra.core.security.audit.SecurityAuditWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginServiceImplTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatedUserReceivesIssuedTokenDirectlyAfterPrimaryAuthentication() {
        UUID userId = UUID.randomUUID();
        SecurityUser user = user(userId, "user@example.com", "ROLE_USER");
        LoginDispatcher dispatcher = mock(LoginDispatcher.class);
        SecurityAuthenticationPort authenticationPort = mock(SecurityAuthenticationPort.class);
        SecurityToken expected = SecurityToken.builder().id(userId).build();

        when(dispatcher.authenticate(any(LoginFrom.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(authenticationPort.isLockedOut(user.getUsername())).thenReturn(false);
        when(authenticationPort.login(user)).thenReturn(expected);

        LoginServiceImpl service = service(dispatcher, authenticationPort);
        SecurityToken result = service.login(passwordLogin(user.getUsername()), ClientType.WEB);

        assertSame(expected, result);
        verify(authenticationPort).login(user);
    }

    @Test
    void devOpsUserAlsoReceivesIssuedTokenAfterPrimaryAuthentication() {
        UUID userId = UUID.randomUUID();
        SecurityUser user = user(userId, "devops@example.com", "ROLE_DEV_OPS");
        LoginDispatcher dispatcher = mock(LoginDispatcher.class);
        SecurityAuthenticationPort authenticationPort = mock(SecurityAuthenticationPort.class);
        SecurityToken expected = SecurityToken.builder().id(userId).build();

        when(dispatcher.authenticate(any(LoginFrom.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(authenticationPort.isLockedOut(user.getUsername())).thenReturn(false);
        when(authenticationPort.login(user)).thenReturn(expected);

        LoginServiceImpl service = service(dispatcher, authenticationPort);
        SecurityToken result = service.login(passwordLogin(user.getUsername()), ClientType.WEB);

        assertSame(expected, result);
        verify(authenticationPort).login(user);
    }

    @Test
    void loginFailureStillRecordsLockoutInputAndRethrowsAuthenticationFailure() {
        UUID userId = UUID.randomUUID();
        SecurityUser user = user(userId, "user@example.com", "ROLE_USER");
        LoginDispatcher dispatcher = mock(LoginDispatcher.class);
        SecurityAuthenticationPort authenticationPort = mock(SecurityAuthenticationPort.class);

        var failure = new org.springframework.security.authentication.BadCredentialsException("bad credentials");
        when(dispatcher.authenticate(any(LoginFrom.class))).thenThrow(failure);
        when(authenticationPort.isLockedOut(user.getUsername())).thenReturn(false);

        LoginServiceImpl service = service(dispatcher, authenticationPort);

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.security.authentication.BadCredentialsException.class,
                () -> service.login(passwordLogin(user.getUsername()), ClientType.WEB));
        verify(authenticationPort).recordLoginFail(user.getUsername());
    }

    private LoginServiceImpl service(LoginDispatcher dispatcher, SecurityAuthenticationPort authenticationPort) {
        ObjectProvider<SecurityAuditWriter> auditProvider = mock(ObjectProvider.class);
        when(auditProvider.getIfAvailable()).thenReturn(null);
        return new LoginServiceImpl(dispatcher, auditProvider, authenticationPort, mock(SecurityContextAccessor.class));
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

}
