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

import com.devops00.spectra.common.port.security.UserOnlineVO;
import com.devops00.spectra.common.security.policy.SecuritySessionPolicyProvider;
import com.devops00.spectra.framework.security.session.converter.UserOnlineConverter;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 在线用户查询批量读取契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class SecurityOnlineUserQueryServiceTest {

    @Test
    void shouldReadAllSessionSummariesWithOneMultiGet() {
        RedisTemplate<String, Object> redis = mock();
        SetOperations<String, Object> sets = mock();
        ValueOperations<String, Object> values = mock();
        HashOperations<String, Object, Object> hashes = mock();
        when(redis.opsForSet()).thenReturn(sets);
        when(redis.opsForValue()).thenReturn(values);
        when(redis.opsForHash()).thenReturn(hashes);
        when(sets.members(SecurityRedisKey.ONLINE_SESSIONS.getPattern()))
                .thenReturn(Set.of("digest-a", "digest-b"));
        when(values.multiGet(anyList())).thenReturn(List.of(summary("user-a", "session-handle-a"),
                summary("user-b", "session-handle-b")));
        UserOnlineConverter converter = mock();
        when(converter.toVO(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(mock(UserOnlineVO.class));

        ObjectProvider<SecuritySessionPolicyProvider> policies = mock();
        var service = new SecurityOnlineUserQueryService(
                new SecuritySessionStore(redis, new SecurityProperties(), policies), converter);

        assertThat(service.listOnlineUsers()).hasSize(2);
        verify(values).multiGet(anyList());
        verify(hashes, never()).entries(org.mockito.ArgumentMatchers.anyString());
        verify(converter).toVO("user-a", "user-a", "web", "127.0.0.1", "session-handle-a", 1_700_000_000_000L);
    }

    @Test
    void shouldRemoveExpiredSessionIndexWhenSummaryHasExpired() {
        String accessDigest = "expired-digest";
        RedisTemplate<String, Object> redis = mock();
        SetOperations<String, Object> sets = mock();
        ValueOperations<String, Object> values = mock();
        when(redis.opsForSet()).thenReturn(sets);
        when(redis.opsForValue()).thenReturn(values);
        when(sets.members(SecurityRedisKey.ONLINE_SESSIONS.getPattern())).thenReturn(Set.of(accessDigest));
        when(values.multiGet(List.of(SecurityRedisKey.SESSION_SUMMARY.format(accessDigest))))
                .thenReturn(Arrays.asList((Object) null));
        when(sets.remove(SecurityRedisKey.ONLINE_SESSIONS.getPattern(), accessDigest)).thenReturn(1L);

        UserOnlineConverter converter = mock();
        ObjectProvider<SecuritySessionPolicyProvider> policies = mock();
        var service = new SecurityOnlineUserQueryService(
                new SecuritySessionStore(redis, new SecurityProperties(), policies), converter);

        assertThat(service.listOnlineUsers()).isEmpty();
        verify(sets).remove(SecurityRedisKey.ONLINE_SESSIONS.getPattern(), accessDigest);
        verify(converter, never()).toVO(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void shouldAttachStableHandleToAnActiveLegacySummary() {
        String familyId = "legacy-family";
        String summaryKey = SecurityRedisKey.SESSION_SUMMARY.format("legacy-digest");
        String refreshFamilyKey = SecurityRedisKey.REFRESH_FAMILY.format(familyId);
        Duration summaryRemaining = Duration.ofSeconds(3600);
        Duration refreshFamilyRemaining = Duration.ofSeconds(43200);
        var valuesByKey = new ConcurrentHashMap<String, Object>();
        RedisTemplate<String, Object> redis = mock();
        SetOperations<String, Object> sets = mock();
        ValueOperations<String, Object> values = mock();
        HashOperations<String, Object, Object> hashes = mock();
        when(redis.opsForSet()).thenReturn(sets);
        when(redis.opsForValue()).thenReturn(values);
        when(redis.opsForHash()).thenReturn(hashes);
        when(redis.expire(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(Duration.class)))
                .thenReturn(true);
        when(redis.getExpire(summaryKey, TimeUnit.SECONDS)).thenReturn(3600L);
        when(redis.getExpire(refreshFamilyKey, TimeUnit.SECONDS)).thenReturn(43200L);
        when(sets.members(SecurityRedisKey.ONLINE_SESSIONS.getPattern())).thenReturn(Set.of("legacy-digest"));
        when(hashes.entries(SecurityRedisKey.SESSION.format("legacy-digest")))
                .thenReturn(Map.of("familyId", familyId));
        when(values.multiGet(anyList())).thenReturn(List.of(Map.of("userId", "user-a", "username", "user-a",
                "clientType", "web", "ip", "127.0.0.1", "loginTime", 1_700_000_000_000L)));
        when(values.get(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> valuesByKey.get(invocation.getArgument(0)));
        when(values.setIfAbsent(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(Duration.class)))
                .thenAnswer(invocation -> valuesByKey.putIfAbsent(invocation.getArgument(0), invocation.getArgument(1)) == null);
        org.mockito.ArgumentCaptor<Object> summaryCaptor = org.mockito.ArgumentCaptor.forClass(Object.class);
        org.mockito.Mockito.doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Object value = invocation.getArgument(1);
            valuesByKey.put(key, value);
            return null;
        })
                .when(values)
                .set(org.mockito.ArgumentMatchers.eq(summaryKey), summaryCaptor.capture(),
                        org.mockito.ArgumentMatchers.eq(summaryRemaining));
        UserOnlineConverter converter = mock();
        when(converter.toVO(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(mock(UserOnlineVO.class));
        ObjectProvider<SecuritySessionPolicyProvider> policies = mock();
        var service = new SecurityOnlineUserQueryService(
                new SecuritySessionStore(redis, new SecurityProperties(), policies), converter);

        assertThat(service.listOnlineUsers()).hasSize(1);
        assertThat(service.listOnlineUsers()).hasSize(1);

        org.mockito.ArgumentCaptor<String> sessionIdCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(converter, org.mockito.Mockito.times(2)).toVO(org.mockito.ArgumentMatchers.eq("user-a"),
                org.mockito.ArgumentMatchers.eq("user-a"), org.mockito.ArgumentMatchers.eq("web"),
                org.mockito.ArgumentMatchers.eq("127.0.0.1"),
                sessionIdCaptor.capture(), org.mockito.ArgumentMatchers.eq(1_700_000_000_000L));
        assertThat(sessionIdCaptor.getAllValues()).hasSize(2).allMatch(value -> !value.isBlank());
        assertThat(sessionIdCaptor.getAllValues().get(1)).isEqualTo(sessionIdCaptor.getAllValues().getFirst());
        assertThat(summaryCaptor.getAllValues()).hasSize(2);
        assertThat(((Map<?, ?>) summaryCaptor.getAllValues().getFirst()).get("sessionId"))
                .isEqualTo(sessionIdCaptor.getAllValues().getFirst());
        assertThat(((Map<?, ?>) summaryCaptor.getAllValues().get(1)).get("sessionId"))
                .isEqualTo(sessionIdCaptor.getAllValues().getFirst());
        verify(values, org.mockito.Mockito.times(2)).setIfAbsent(
                org.mockito.ArgumentMatchers.eq(SecurityRedisKey.FAMILY_HANDLE.format(familyId)),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(refreshFamilyRemaining));
    }

    /**
     * 处理安全用户查询相关数据。
     */
    private static Map<String, Object> summary(String userId, String sessionId) {
        return Map.of("userId", userId, "username", userId, "clientType", "web", "ip", "127.0.0.1",
                "sessionId", sessionId,
                "loginTime", 1_700_000_000_000L);
    }
}
