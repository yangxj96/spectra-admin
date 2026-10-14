/*
 * Copyright 2018-2026 yangxj96
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.devops00.spectra.core.user.service.impl;

import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.common.exception.BusinessRuleViolationException;
import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.common.port.security.SecurityToken;
import com.devops00.spectra.common.security.policy.PasswordPolicy;
import com.devops00.spectra.common.security.policy.SecuritySessionPolicyProvider;
import com.devops00.spectra.common.security.policy.SessionPolicy;
import com.devops00.spectra.core.audit.AuditRecordFactory;
import com.devops00.spectra.core.security.authentication.service.PasswordCredentialService;
import com.devops00.spectra.core.security.authentication.javabean.entity.PasswordCredential;
import com.devops00.spectra.core.security.root.LastEffectiveDevOpsGuard;
import com.devops00.spectra.core.system.javabean.from.SecuritySessionRevokeFrom;
import com.devops00.spectra.core.system.service.impl.CacheManagementServiceImpl;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.javabean.from.ChangePasswordFrom;
import com.devops00.spectra.framework.security.configuration.redis.SecJacksonConfiguration;
import com.devops00.spectra.framework.security.configuration.redis.SecRedisConfiguration;
import com.devops00.spectra.framework.security.configuration.session.SecuritySessionPortConfiguration;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.token.TokenDigestService;
import com.devops00.spectra.framework.security.session.SecuritySessionIssueService;
import com.devops00.spectra.framework.security.session.SecuritySessionReaderService;
import com.devops00.spectra.framework.security.session.SecuritySessionRefreshService;
import com.devops00.spectra.framework.security.session.SecuritySessionRevocationService;
import com.devops00.spectra.framework.security.session.SecuritySessionStore;
import com.devops00.spectra.framework.security.session.concurrency.AllowSessionConcurrencyStrategy;
import com.devops00.spectra.framework.security.session.concurrency.KickOldSessionConcurrencyStrategy;
import com.devops00.spectra.framework.security.session.concurrency.RejectNewSessionConcurrencyStrategy;
import com.devops00.spectra.framework.security.session.concurrency.SessionConcurrencyStrategyResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 从 Core 改密和管理员下线用例进入 Framework 撤销端口，验证跨连接的 Refresh 失效。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/11
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class CoreSessionRevocationRedisTest {

    private LettuceConnectionFactory issuingFactory;
    private LettuceConnectionFactory revokingFactory;
    private RedisTemplate<String, Object> issuingRedis;
    private SecuritySessionIssueService issuer;
    private SecuritySessionRefreshService refresher;
    private SecuritySessionReaderService reader;
    private SecuritySessionRevocationPort revocationPort;
    private SecurityPrincipal principal;
    private String keyPrefix;
    private String operatorToken;

    @BeforeEach
    void setUp() {
        var configuration = testRedisConfiguration();
        keyPrefix = "spectra:test:core-base003:" + UUID.randomUUID() + ":";
        issuingFactory = connect(configuration);
        revokingFactory = connect(configuration);
        issuingRedis = template(issuingFactory);

        var policies = new StaticListableBeanFactory();
        policies.addBean("policy", (SecuritySessionPolicyProvider) code -> SessionPolicy.defaults(30, 300));
        var provider = policies.getBeanProvider(SecuritySessionPolicyProvider.class);
        var properties = new SecurityProperties();
        var issuingStore = new SecuritySessionStore(issuingRedis, properties, provider);
        var revokingStore = new SecuritySessionStore(template(revokingFactory), properties, provider);
        principal = mock(SecurityPrincipal.class);
        when(principal.getId()).thenReturn(UUID.randomUUID());
        when(principal.getUsername()).thenReturn("synthetic-session-user");
        when(principal.getAuthorityNames()).thenReturn(List.of());

        var issuingRevoker = new SecuritySessionRevocationService(issuingStore, () -> null);
        var revokingRevoker = new SecuritySessionRevocationService(revokingStore, () -> operatorToken);
        revocationPort = new SecuritySessionPortConfiguration().securitySessionRevocationPort(revokingRevoker);
        var strategies = new SessionConcurrencyStrategyResolver(List.of(new AllowSessionConcurrencyStrategy(),
                new RejectNewSessionConcurrencyStrategy(), new KickOldSessionConcurrencyStrategy(issuingStore)));
        var clock = Clock.systemUTC();
        issuer = new SecuritySessionIssueService(issuingStore, issuingRevoker, strategies, clock);
        refresher = new SecuritySessionRefreshService(issuingStore, issuer, issuingRevoker, id -> principal, clock);
        reader = new SecuritySessionReaderService(issuingStore, id -> principal, clock);
    }

    @AfterEach
    void tearDown() {
        try {
            if (issuingFactory != null && keyPrefix != null) {
                clearOwnKeys();
            }
        } finally {
            if (revokingFactory != null) {
                revokingFactory.destroy();
            }
            if (issuingFactory != null) {
                issuingFactory.destroy();
            }
        }
    }

    @Test
    void passwordChangeRevokesRefreshEvenWhenAccessExpired() {
        SecurityToken web = issuer.createToken(principal, ClientType.WEB);
        SecurityToken app = issuer.createToken(principal, ClientType.APP);
        issuingRedis.delete(SecurityRedisKey.SESSION.format(TokenDigestService.digest(web.getAccessToken())));

        userService().changePassword(principal.getId(),
                new ChangePasswordFrom("old-pass-1", "new-pass-2", "new-pass-2"));

        assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(web.getRefreshToken()));
        assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(app.getRefreshToken()));
        assertNull(reader.getCurrentUser(app.getAccessToken()));
    }

    @Test
    void rejectedPasswordChangeKeepsExistingRefreshUsable() {
        SecurityToken token = issuer.createToken(principal, ClientType.WEB);

        assertThrows(BusinessRuleViolationException.class, () -> userService().changePassword(principal.getId(),
                new ChangePasswordFrom("wrong-pass", "new-pass-2", "new-pass-2")));

        assertNotNull(refresher.refreshByRefreshToken(token.getRefreshToken()));
    }

    @Test
    void administratorClientRevocationCoversEveryAllowedRefresh() {
        SecurityToken firstWeb = issuer.createToken(principal, ClientType.WEB);
        SecurityToken secondWeb = issuer.createToken(principal, ClientType.WEB);
        SecurityToken app = issuer.createToken(principal, ClientType.APP);
        operatorToken = app.getAccessToken();
        issuingRedis.delete(SecurityRedisKey.SESSION.format(TokenDigestService.digest(firstWeb.getAccessToken())));
        var admin = new CacheManagementServiceImpl(null, null, null, revocationPort, null, null, null, null, null);

        assertEquals("SUCCEEDED", admin.revokeSession(new SecuritySessionRevokeFrom(
                principal.getId(), ClientType.WEB, "synthetic", true)).status());

        assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(firstWeb.getRefreshToken()));
        assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(secondWeb.getRefreshToken()));
        assertNotNull(refresher.refreshByRefreshToken(app.getRefreshToken()));
    }

    @Test
    void administratorRevocationPropagatesUnknownRedisOutcome() {
        var failingPort = mock(SecuritySessionRevocationPort.class);
        UUID userId = principal.getId();
        org.mockito.Mockito.doThrow(new SecurityRedisUnavailableException("synthetic Redis failure", null))
                .when(failingPort)
                .revokeUserClientSessions(userId, ClientType.WEB);
        var admin = new CacheManagementServiceImpl(null, null, null, failingPort, null, null, null, null, null);

        assertThrows(SecurityRedisUnavailableException.class, () -> admin.revokeSession(
                new SecuritySessionRevokeFrom(principal.getId(), ClientType.WEB, "synthetic", true)));
    }

    private UserServiceImpl userService() {
        var service = mock(UserServiceImpl.class, CALLS_REAL_METHODS);
        UUID userId = principal.getId();
        var user = new User();
        user.setId(userId);
        doReturn(user).when(service).getById(userId);
        var credential = new PasswordCredential();
        credential.setPasswordHash("old-hash");
        var credentials = mock(PasswordCredentialService.class);
        when(credentials.getByUserId(userId)).thenReturn(credential);
        var encoder = mock(PasswordEncoder.class);
        when(encoder.matches("old-pass-1", "old-hash")).thenReturn(true);
        when(encoder.matches("new-pass-2", "old-hash")).thenReturn(false);
        when(encoder.encode("new-pass-2")).thenReturn("new-hash");
        ReflectionTestUtils.setField(service, "passwordCredentialService", credentials);
        ReflectionTestUtils.setField(service, "lastEffectiveDevOpsGuard", mock(LastEffectiveDevOpsGuard.class));
        ReflectionTestUtils.setField(service, "passwordEncoder", encoder);
        ReflectionTestUtils.setField(service, "securityPasswordPolicyProvider",
                (com.devops00.spectra.common.security.policy.SecurityPasswordPolicyProvider) () -> new PasswordPolicy(8, false, false, false, false,
                        null));
        ReflectionTestUtils.setField(service, "securitySessionRevocationPort", revocationPort);
        ReflectionTestUtils.setField(service, "securityContextAccessor", mock(SecurityContextAccessor.class));
        ReflectionTestUtils.setField(service, "auditRecordFactory", mock(AuditRecordFactory.class));
        ReflectionTestUtils.setField(service, "auditService", mock(AuditService.class));
        return service;
    }

    private static RedisStandaloneConfiguration testRedisConfiguration() {
        if (!"5".equals(System.getenv("REDIS_DB"))) {
            throw new IllegalStateException("真实依赖测试只允许 REDIS_DB=5");
        }
        String host = System.getenv("REDIS_HOST");
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("真实依赖测试缺少 REDIS_HOST");
        }
        int port;
        try {
            port = Integer.parseInt(System.getenv("REDIS_PORT"));
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("真实依赖测试缺少有效 REDIS_PORT", exception);
        }
        if (port < 1 || port > 65535) {
            throw new IllegalStateException("真实依赖测试的 REDIS_PORT 超出范围");
        }
        var configuration = new RedisStandaloneConfiguration(host, port);
        configuration.setDatabase(5);
        String password = System.getenv("REDIS_PASSWORD");
        if (password != null && !password.isBlank()) {
            configuration.setPassword(RedisPassword.of(password));
        }
        return configuration;
    }

    private static LettuceConnectionFactory connect(RedisStandaloneConfiguration configuration) {
        var factory = new LettuceConnectionFactory(configuration);
        factory.afterPropertiesSet();
        factory.start();
        return factory;
    }

    private RedisTemplate<String, Object> template(LettuceConnectionFactory factory) {
        var redis = new SecRedisConfiguration().redisTemplate(factory,
                new SecJacksonConfiguration().redisObjectMapper(), new SecurityProperties());
        var serializer = new StringRedisSerializer();
        redis.setKeySerializer(new RedisSerializer<String>() {
            @Override
            public byte[] serialize(String value) throws SerializationException {
                return serializer.serialize(keyPrefix + value);
            }

            @Override
            public String deserialize(byte[] bytes) throws SerializationException {
                String value = serializer.deserialize(bytes);
                if (value == null || !value.startsWith(keyPrefix)) {
                    throw new SerializationException("Redis 测试键不属于当前用例");
                }
                return value.substring(keyPrefix.length());
            }
        });
        return redis;
    }

    private void clearOwnKeys() {
        var ownedKeys = new ArrayList<byte[]>();
        try (var connection = issuingFactory.getConnection();
                var keys = connection.scan(ScanOptions.scanOptions().match(keyPrefix + "*").count(100).build())) {
            while (keys.hasNext()) {
                byte[] key = keys.next();
                if (!new String(key, StandardCharsets.UTF_8).startsWith(keyPrefix)) {
                    throw new IllegalStateException("Redis 扫描结果不属于当前测试用例");
                }
                ownedKeys.add(key);
            }
            for (byte[] key : ownedKeys) {
                connection.keyCommands().del(key);
            }
        }
    }
}
