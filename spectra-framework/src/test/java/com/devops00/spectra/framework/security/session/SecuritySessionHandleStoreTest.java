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

import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.token.TokenDigestService;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 会话句柄在 Redis 中稳定映射到 Token Family 的回归测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class SecuritySessionHandleStoreTest {

    @Test
    void shouldReuseOpaqueHandleForTheSameFamilyAndIndexOnlyItsDigest() {
        String familyId = "family-id";
        Duration ttl = Duration.ofHours(12);
        var valuesByKey = new ConcurrentHashMap<String, Object>();
        RedisTemplate<String, Object> redis = mock();
        ValueOperations<String, Object> values = mock();
        when(redis.opsForValue()).thenReturn(values);
        when(redis.expire(anyString(), eq(ttl))).thenReturn(true);
        when(values.setIfAbsent(anyString(), any(), eq(ttl)))
                .thenAnswer(invocation -> valuesByKey.putIfAbsent(invocation.getArgument(0), invocation.getArgument(1)) == null);
        when(values.get(anyString())).thenAnswer(invocation -> valuesByKey.get(invocation.getArgument(0)));
        var store = new SecuritySessionHandleStore(redis);

        String first = store.createOrGet(familyId, ttl);
        String second = store.createOrGet(familyId, ttl);

        assertThat(first).isNotBlank().isEqualTo(second);
        assertThat(store.resolveFamily(first)).isEqualTo(familyId);
        verify(values).setIfAbsent(eq(SecurityRedisKey.SESSION_HANDLE.format(TokenDigestService.digest(first))),
                eq(familyId), eq(ttl));
        assertThat(SecurityRedisKey.SESSION_HANDLE.format(TokenDigestService.digest(first))).doesNotContain(first);
    }

    @Test
    void shouldRejectAnUnknownOrExpiredHandle() {
        String handle = "opaque-session-handle";
        RedisTemplate<String, Object> redis = mock();
        ValueOperations<String, Object> values = mock();
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(eq(SecurityRedisKey.SESSION_HANDLE.format(TokenDigestService.digest(handle)))))
                .thenReturn(null);

        var store = new SecuritySessionHandleStore(redis);

        assertThatThrownBy(() -> store.resolveFamily(handle)).isInstanceOf(IllegalArgumentException.class);
    }
}
