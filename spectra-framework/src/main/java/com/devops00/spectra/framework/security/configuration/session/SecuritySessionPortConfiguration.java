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

package com.devops00.spectra.framework.security.configuration.session;

import com.devops00.spectra.common.config.SystemConfigValueProvider;
import com.devops00.spectra.common.port.security.SecurityAuthenticationPort;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.port.security.SecuritySessionQueryPort;
import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.common.port.security.SecurityToken;
import com.devops00.spectra.common.port.security.SecurityUserLookupPort;
import com.devops00.spectra.framework.security.session.lifecycle.SecurityLoginFailureTracker;
import com.devops00.spectra.framework.security.session.lifecycle.SecuritySessionIssuer;
import com.devops00.spectra.framework.security.session.lifecycle.SecuritySessionRefresher;
import com.devops00.spectra.framework.security.session.query.SecuritySessionQuery;
import com.devops00.spectra.framework.security.session.query.SecuritySessionReader;
import com.devops00.spectra.framework.security.session.lifecycle.SecuritySessionRevoker;
import com.devops00.spectra.framework.security.session.token.SecurityTokenAccessor;
import com.devops00.spectra.framework.security.session.query.SecuritySessionContextAccessor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/**
 * Security Session 适配端口配置
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/2/19 22:37
 */
@Configuration
public class SecuritySessionPortConfiguration {

    /**
     * 处理内部业务逻辑（{@code securityContextAccessor}）。
     */
    @Bean
    public SecurityContextAccessor securityContextAccessor(SecuritySessionReader sessionReader,
                                                           SecurityTokenAccessor tokenAccessor,
                                                           ObjectProvider<SystemConfigValueProvider> systemConfigValueProvider) {
        return new SecuritySessionContextAccessor(sessionReader, tokenAccessor, systemConfigValueProvider);
    }

    /**
     * 处理内部业务逻辑（{@code securitySessionRevocationPort}）。
     */
    @Bean
    public SecuritySessionRevocationPort securitySessionRevocationPort(SecuritySessionRevoker sessionRevoker) {
        return new SecuritySessionRevocationPort() {
            @Override
            public void revokeUserSessions(UUID userId) {
                sessionRevoker.deleteByUserId(userId);
            }

            @Override
            public void revokeUserSessionsExceptToken(UUID userId, String accessToken) {
                sessionRevoker.deleteByUserIdExceptToken(userId, accessToken);
            }
        };
    }

    /**
     * 处理内部业务逻辑（{@code securitySessionQueryPort}）。
     */
    @Bean
    public SecuritySessionQueryPort securitySessionQueryPort(SecuritySessionQuery sessionQuery) {
        return sessionQuery::listOnlineUsers;
    }

    /**
     * 处理内部业务逻辑（{@code securityAuthenticationPort}）。
     */
    @Bean
    public SecurityAuthenticationPort securityAuthenticationPort(SecuritySessionIssuer sessionIssuer,
                                                                 SecuritySessionRefresher sessionRefresher,
                                                                 SecuritySessionRevoker sessionRevoker,
                                                                 SecurityLoginFailureTracker loginFailureTracker) {
        return new SecurityAuthenticationPort() {
            @Override
            public SecurityToken login(SecurityPrincipal user) {
                return sessionIssuer.createToken(user);
            }

            @Override
            public void logout(String token) {
                sessionRevoker.deleteToken(token);
            }

            @Override
            public void logoutByRefreshToken(String refreshToken) {
                sessionRevoker.deleteByRefreshToken(refreshToken);
            }

            @Override
            public SecurityToken refreshByRefreshToken(String refreshToken) {
                return sessionRefresher.refreshByRefreshToken(refreshToken);
            }

            @Override
            public boolean isLockedOut(String username) {
                return loginFailureTracker.isLockedOut(username);
            }

            @Override
            public void recordLoginFail(String username) {
                loginFailureTracker.recordLoginFail(username);
            }

            @Override
            public void clearLoginFail(String username) {
                loginFailureTracker.clearLoginFail(username);
            }
        };
    }

    /**
     * 处理内部业务逻辑（{@code securityUserLookupPort}）。
     */
    @Bean
    public SecurityUserLookupPort securityUserLookupPort(SecuritySessionReader sessionReader) {
        return token -> sessionReader.getCurrentUser(token);
    }

}
