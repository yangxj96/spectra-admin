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

package com.devops00.spectra.framework.security.session;

import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.port.security.SecurityToken;
import com.devops00.spectra.common.port.security.SecurityUserLoader;
import com.devops00.spectra.common.security.policy.SecuritySessionPolicyProvider;
import com.devops00.spectra.common.security.policy.SessionPolicy;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.store.RefreshTokenRotationStore;
import com.devops00.spectra.framework.security.redis.token.TokenDigestService;
import com.devops00.spectra.framework.security.session.concurrency.AllowSessionConcurrencyStrategy;
import com.devops00.spectra.framework.security.session.concurrency.KickOldSessionConcurrencyStrategy;
import com.devops00.spectra.framework.security.session.concurrency.RejectNewSessionConcurrencyStrategy;
import com.devops00.spectra.framework.security.session.concurrency.SessionConcurrencyStrategyResolver;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Refresh Token 轮换后在线管理会话句柄仍保持稳定的回归测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class SecuritySessionRefreshServiceTest {

    @Test
    void shouldKeepTheSameManagementHandleAfterRefreshRotation() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        String familyId = "stable-family-id";
        RedisTemplate<String, Object> redis = mock();
        HashOperations<String, Object, Object> hashes = mock();
        SetOperations<String, Object> sets = mock();
        ValueOperations<String, Object> values = mock();
        var hashesByKey = new ConcurrentHashMap<String, Map<Object, Object>>();
        var valuesByKey = new ConcurrentHashMap<String, Object>();
        List<Map<?, ?>> summaries = new ArrayList<>();
        when(redis.opsForHash()).thenReturn(hashes);
        when(redis.opsForSet()).thenReturn(sets);
        when(redis.opsForValue()).thenReturn(values);
        when(redis.hasKey(anyString())).thenReturn(false);
        when(redis.expire(anyString(), any(Duration.class))).thenReturn(true);
        when(redis.delete(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            hashesByKey.remove(key);
            valuesByKey.remove(key);
            return true;
        });
        when(hashes.entries(anyString())).thenAnswer(invocation -> hashesByKey.getOrDefault(invocation.getArgument(0), Map.of()));
        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Map<?, ?> map = invocation.getArgument(1);
            hashesByKey.put(key, new ConcurrentHashMap<>(map));
            return null;
        }).when(hashes).putAll(anyString(), anyMap());
        when(sets.members(anyString())).thenReturn(Set.of());
        when(values.get(anyString())).thenAnswer(invocation -> valuesByKey.get(invocation.getArgument(0)));
        when(values.setIfAbsent(anyString(), any(), any(Duration.class)))
                .thenAnswer(invocation -> valuesByKey.putIfAbsent(invocation.getArgument(0), invocation.getArgument(1)) == null);
        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Object value = invocation.getArgument(1);
            valuesByKey.put(key, value);
            if (key.startsWith(SecurityRedisKey.SESSION_SUMMARY.getPattern().replace("%s", ""))) {
                summaries.add((Map<?, ?>) value);
            }
            return null;
        }).when(values).set(anyString(), any(), any(Duration.class));

        SecuritySessionPolicyProvider policyProvider = mock();
        when(policyProvider.find("web")).thenReturn(SessionPolicy.defaults(900, 86400));
        ObjectProvider<SecuritySessionPolicyProvider> policies = mock();
        when(policies.getIfAvailable()).thenReturn(policyProvider);
        var store = new SecuritySessionStore(redis, new SecurityProperties(), policies);
        var revocation = new SecuritySessionRevocationService(store, () -> null);
        var issuer = new SecuritySessionIssueService(store, revocation, resolver(store));
        SecurityPrincipal user = mock();
        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("root");
        when(user.getAuthorityNames()).thenReturn(List.of("ROLE_DEV_OPS"));

        SecurityToken firstToken = issuer.createToken(user, ClientType.WEB, familyId);
        String firstDigest = TokenDigestService.digest(firstToken.getAccessToken());
        SecurityUserLoader userLoader = mock();
        when(userLoader.load(userId)).thenReturn(user);
        var refresher = new SecuritySessionRefreshService(store, issuer, revocation, userLoader);
        try (MockedStatic<RefreshTokenRotationStore> rotation = org.mockito.Mockito.mockStatic(
                RefreshTokenRotationStore.class)) {
            rotation.when(() -> RefreshTokenRotationStore.claim(eq(redis),
                    eq(SecurityRedisKey.REFRESH_TOKEN.format(TokenDigestService.digest(firstToken.getRefreshToken()))),
                    eq(SecurityRedisKey.REFRESH_CLAIM.format(TokenDigestService.digest(firstToken.getRefreshToken()))),
                    eq(86400L))).thenReturn(RefreshTokenRotationStore.ClaimResult.CLAIMED);
            rotation.when(() -> RefreshTokenRotationStore.compareAndDelete(any(), anyString(), anyString()))
                    .thenReturn(true);

            SecurityToken refreshed = refresher.refreshByRefreshToken(firstToken.getRefreshToken());

            assertThat(refreshed.getAccessToken()).isNotEqualTo(firstToken.getAccessToken());
            assertThat(summaries).hasSize(2);
            assertThat(((Map<?, ?>) summaries.get(0)).get("sessionId")).isNotNull();
            assertThat(((Map<?, ?>) summaries.get(1)).get("sessionId"))
                    .isEqualTo(((Map<?, ?>) summaries.get(0)).get("sessionId"));
            assertThat(hashesByKey).doesNotContainKey(SecurityRedisKey.SESSION.format(firstDigest));
        }
    }

    private static SessionConcurrencyStrategyResolver resolver(SecuritySessionStore store) {
        return new SessionConcurrencyStrategyResolver(List.of(
                new AllowSessionConcurrencyStrategy(),
                new KickOldSessionConcurrencyStrategy(store),
                new RejectNewSessionConcurrencyStrategy()));
    }
}
