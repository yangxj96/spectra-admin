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

package com.devops00.spectra.security.starter.configuration;

import com.devops00.spectra.security.base.change.SecurityAuthenticationPort;
import com.devops00.spectra.security.base.change.SecuritySessionQueryPort;
import com.devops00.spectra.security.base.change.SecuritySessionRevocationPort;
import com.devops00.spectra.security.base.change.SecurityUserLookupPort;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.holder.SecurityLoginFailureTracker;
import com.devops00.spectra.security.base.holder.SecuritySessionIssuer;
import com.devops00.spectra.security.base.holder.SecuritySessionQuery;
import com.devops00.spectra.security.base.holder.SecuritySessionReader;
import com.devops00.spectra.security.base.holder.SecuritySessionRevoker;
import com.devops00.spectra.security.base.holder.SecurityTokenAccessor;
import com.devops00.spectra.security.base.holder.SecurityUserLoader;
import com.devops00.spectra.security.base.mfa.SecurityMfaChallengePort;
import com.devops00.spectra.security.base.policy.SecuritySessionPolicyProvider;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.starter.holder.SecuritySessionContextAccessor;
import com.devops00.spectra.security.starter.strategy.RedisSecuritySessionRepository;
import com.devops00.spectra.security.starter.strategy.RedisMfaLoginChallengeRepository;
import com.devops00.spectra.security.starter.web.javabean.converter.UserOnlineConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import tools.jackson.databind.ObjectMapper;

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

    @Bean
    public SecurityContextAccessor securityContextAccessor(SecuritySessionReader sessionReader,
                                                           SecurityTokenAccessor tokenAccessor) {
        return new SecuritySessionContextAccessor(sessionReader, tokenAccessor);
    }

    @Bean
    public SecuritySessionRevocationPort securitySessionRevocationPort(SecuritySessionRevoker sessionRevoker) {
        return sessionRevoker::deleteByUserId;
    }

    @Bean
    public SecuritySessionQueryPort securitySessionQueryPort(SecuritySessionQuery sessionQuery) {
        return sessionQuery::listOnlineUsers;
    }

    @Bean
    public SecurityAuthenticationPort securityAuthenticationPort(SecuritySessionIssuer sessionIssuer,
                                                                 SecuritySessionRevoker sessionRevoker,
                                                                 SecurityLoginFailureTracker loginFailureTracker) {
        return new SecurityAuthenticationPort() {
            @Override
            public com.devops00.spectra.security.base.javabean.vo.TokenVO login(
                                                                                com.devops00.spectra.security.base.javabean.entity.SecurityUser user) {
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
            public com.devops00.spectra.security.base.javabean.vo.TokenVO refreshByRefreshToken(String refreshToken) {
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
                                                                         ObjectProvider<SecurityUserLoader> securityUserLoaderProvider) {
        return new RedisSecuritySessionRepository(om, redis, properties, userOnlineConverter,
                sessionPolicyProvider.getIfAvailable(),
                securityUserLoaderProvider.getIfAvailable());
    }

    @Bean
    @ConditionalOnProperty(prefix = "spectra.security", name = "sec-mode", havingValue = "REDIS", matchIfMissing = true)
    public SecurityMfaChallengePort securityMfaChallengePort(
                                                             @Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redis,
                                                             SecurityProperties properties) {
        return new RedisMfaLoginChallengeRepository(redis, properties);
    }
}
