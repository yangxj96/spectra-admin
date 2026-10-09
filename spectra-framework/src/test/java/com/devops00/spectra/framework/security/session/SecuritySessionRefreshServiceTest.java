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

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.common.port.security.SecurityUserLoader;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证刷新 Token 的认证失败能返回面向客户端的明确错误。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/15
 */
class SecuritySessionRefreshServiceTest {

    @Test
    void shouldExposeMissingRefreshTokenAsAuthenticationFailure() {
        var store = mock(SecuritySessionStore.class);
        when(store.hash(anyString(), anyString())).thenReturn(Map.of());
        var service = new SecuritySessionRefreshService(
                store,
                mock(SecuritySessionIssueService.class),
                mock(SecuritySessionRevocationService.class),
                mock(SecurityUserLoader.class));

        var exception = assertThrows(BadCredentialsException.class,
                () -> service.refreshByRefreshToken("invalid-refresh-token"));

        assertEquals("刷新token无效或已过期", exception.getMessage());
    }

    @Test
    void unknownUserIndexMembershipRejectsRefresh() {
        var store = mock(SecuritySessionStore.class);
        RedisTemplate<String, Object> redis = mock();
        SetOperations<String, Object> sets = mock();
        when(store.hash(anyString(), anyString())).thenReturn(Map.of(
                "accessToken", "access-digest",
                "userId", UUID.randomUUID().toString(),
                "clientType", "web",
                "familyId", "family-id"));
        when(store.redis()).thenReturn(redis);
        when(redis.opsForSet()).thenReturn(sets);
        when(sets.isMember(anyString(), eq("access-digest"))).thenReturn(null);
        var service = new SecuritySessionRefreshService(
                store,
                mock(SecuritySessionIssueService.class),
                mock(SecuritySessionRevocationService.class),
                mock(SecurityUserLoader.class));

        assertThrows(SecurityRedisUnavailableException.class,
                () -> service.refreshByRefreshToken("synthetic-refresh-token"));
    }
}
