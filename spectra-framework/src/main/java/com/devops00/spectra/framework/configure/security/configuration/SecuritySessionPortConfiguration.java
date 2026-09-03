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

package com.devops00.spectra.framework.configure.security.configuration;

import com.devops00.spectra.common.config.SystemConfigValueProvider;
import com.devops00.spectra.common.port.security.SecurityAuthenticationPort;
import com.devops00.spectra.common.port.security.SecuritySessionQueryPort;
import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.common.port.security.SecurityUserLookupPort;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.framework.configure.security.holder.SecurityLoginFailureTracker;
import com.devops00.spectra.framework.configure.security.holder.SecuritySessionIssuer;
import com.devops00.spectra.framework.configure.security.holder.SecuritySessionQuery;
import com.devops00.spectra.framework.configure.security.holder.SecuritySessionReader;
import com.devops00.spectra.framework.configure.security.holder.SecuritySessionRevoker;
import com.devops00.spectra.framework.configure.security.holder.SecurityTokenAccessor;
import com.devops00.spectra.common.port.security.SecurityUserLoader;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.port.security.SecurityToken;
import com.devops00.spectra.common.security.policy.SecuritySessionPolicyProvider;
import com.devops00.spectra.framework.configure.security.properties.SecurityProperties;
import com.devops00.spectra.framework.configure.security.holder.SecuritySessionContextAccessor;
import com.devops00.spectra.framework.configure.security.strategy.RedisSecuritySessionRepository;
import com.devops00.spectra.framework.configure.security.converter.UserOnlineConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

/**
 * Security Session 适配端口配置
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/2/19 22:37
 */
@Slf4j
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
                return sessionIssuer.refreshByRefreshToken(refreshToken);
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

    /**
     * 使用 Redis 提供安全会话和认证端口的具体实现。
     *
     * @param om                  Security使用的ObjectMapper
     * @param redis               Security使用的RedisTemplate
     * @param properties          安全配置
     * @param userOnlineConverter 在线用户转换器
     */
    @Bean(name = "sec")
    @ConditionalOnProperty(prefix = "spectra.security", name = "sec-mode", havingValue = "REDIS", matchIfMissing = true)
    public RedisSecuritySessionRepository redisSecuritySessionRepository(
                                                                         @Qualifier("securityObjectMapper") ObjectMapper om,
                                                                         @Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redis,
                                                                         SecurityProperties properties,
                                                                         UserOnlineConverter userOnlineConverter,
                                                                         ObjectProvider<SecuritySessionPolicyProvider> sessionPolicyProvider,
                                                                         SecurityUserLoader securityUserLoader) {
        return new RedisSecuritySessionRepository(om, redis, properties, userOnlineConverter,
                sessionPolicyProvider.getIfAvailable(),
                securityUserLoader);
    }

}
