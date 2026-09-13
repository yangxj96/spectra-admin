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
import com.devops00.spectra.framework.security.session.converter.UserOnlineConverter;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.token.TokenDigestService;
import com.devops00.spectra.framework.security.session.concurrency.AllowSessionConcurrencyStrategy;
import com.devops00.spectra.framework.security.session.concurrency.KickOldSessionConcurrencyStrategy;
import com.devops00.spectra.framework.security.session.concurrency.RejectNewSessionConcurrencyStrategy;
import com.devops00.spectra.framework.security.session.concurrency.SessionConcurrencyStrategyResolver;
import com.devops00.spectra.common.port.security.SecurityUserLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 安全 Session 脏数据必须 fail-closed 的回归测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class SecuritySessionMalformedDataTest {

    @Test
    void shouldRejectMalformedLoginFailureCountAsSecurityRedisDataError() {
        RedisTemplate<String, Object> redis = mock();
        ValueOperations<String, Object> values = mock();
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(SecurityRedisKey.LOGIN_FAIL.format("root@example.com")))
                .thenReturn("not-a-number");

        var repository = new SecurityLoginFailureStore(new SecuritySessionStore(redis, new SecurityProperties(),
                mock(ObjectProvider.class)));

        assertThrows(SecurityRedisUnavailableException.class,
                () -> repository.isLockedOut("root@example.com"));
    }

    @Test
    void shouldRejectMalformedRefreshUserIdAsSecurityRedisDataError() {
        RedisTemplate<String, Object> redis = mock();
        HashOperations<String, Object, Object> hashes = mock();
        when(redis.opsForHash()).thenReturn(hashes);
        String refreshDigest = TokenDigestService.digest("refresh-token");
        when(hashes.entries(SecurityRedisKey.REFRESH_TOKEN.format(refreshDigest)))
                .thenReturn(Map.of("accessToken", "access-digest", "userId", "not-a-uuid", "familyId", "family"));

        var store = new SecuritySessionStore(redis, new SecurityProperties(), mock(ObjectProvider.class));
        var repository = new SecuritySessionRefreshService(store,
                new SecuritySessionIssueService(store, new SecuritySessionRevocationService(store), resolver(store)),
                new SecuritySessionRevocationService(store), mock(SecurityUserLoader.class));

        assertThrows(SecurityRedisUnavailableException.class,
                () -> repository.refreshByRefreshToken("refresh-token"));
    }

    @Test
    void shouldRejectMalformedOnlineSessionTimestampAsSecurityRedisDataError() {
        RedisTemplate<String, Object> redis = mock();
        HashOperations<String, Object, Object> hashes = mock();
        SetOperations<String, Object> sets = mock();
        ValueOperations<String, Object> values = mock();
        when(redis.opsForHash()).thenReturn(hashes);
        when(redis.opsForSet()).thenReturn(sets);
        when(redis.opsForValue()).thenReturn(values);
        String userId = "00000000-0000-0000-0000-000000000001";
        String accessDigest = "access-digest";
        when(sets.members(SecurityRedisKey.ONLINE_SESSIONS.getPattern())).thenReturn(Set.of(accessDigest));
        when(values.multiGet(java.util.List.of(SecurityRedisKey.SESSION_SUMMARY.format(accessDigest))))
                .thenReturn(java.util.List.of(Map.of(
                        "userId", userId,
                        "username", "root",
                        "clientType", "web",
                        "ip", "127.0.0.1",
                        "loginTime", "not-a-number")));

        var repository = new SecurityOnlineUserQueryService(
                new SecuritySessionStore(redis, new SecurityProperties(), mock(ObjectProvider.class)),
                mock(UserOnlineConverter.class));

        assertThrows(SecurityRedisUnavailableException.class, repository::listOnlineUsers);
    }

    /**
     * 处理安全会话相关数据。
     */
    private static SessionConcurrencyStrategyResolver resolver(SecuritySessionStore store) {
        return new SessionConcurrencyStrategyResolver(List.of(
                new AllowSessionConcurrencyStrategy(),
                new KickOldSessionConcurrencyStrategy(store),
                new RejectNewSessionConcurrencyStrategy()));
    }
}
