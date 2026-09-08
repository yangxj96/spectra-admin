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
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.security.policy.SecuritySessionPolicyProvider;
import com.devops00.spectra.common.security.policy.SessionPolicy;
import com.devops00.spectra.framework.security.session.concurrency.AllowSessionConcurrencyStrategy;
import com.devops00.spectra.framework.security.session.concurrency.KickOldSessionConcurrencyStrategy;
import com.devops00.spectra.framework.security.session.concurrency.RejectNewSessionConcurrencyStrategy;
import com.devops00.spectra.framework.security.session.concurrency.SessionConcurrencyStrategyResolver;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.devops00.spectra.framework.security.redis.key.SecurityRedisKey.SESSION;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Session 多 Key 写入失败时必须清理已写入状态的回归测试。 */
class SecuritySessionIssueServicePartialWriteTest {

    @Test
    void shouldRemoveWrittenSessionWhenAChildRedisWriteFails() {
        RedisTemplate<String, Object> redis = mock();
        HashOperations<String, Object, Object> hashes = mock();
        SetOperations<String, Object> sets = mock();
        ValueOperations<String, Object> values = mock();
        when(redis.opsForHash()).thenReturn(hashes);
        when(redis.opsForSet()).thenReturn(sets);
        when(redis.opsForValue()).thenReturn(values);
        when(sets.members(anyString())).thenReturn(Set.of());
        doThrow(new DataAccessResourceFailureException("redis unavailable"))
                .when(values)
                .set(anyString(), any(), any(Duration.class));

        SecuritySessionPolicyProvider policyProvider = mock();
        when(policyProvider.find("web")).thenReturn(SessionPolicy.defaults(900, 86400));
        ObjectProvider<SecuritySessionPolicyProvider> policies = mock();
        when(policies.getIfAvailable()).thenReturn(policyProvider);
        var store = new SecuritySessionStore(redis, new com.devops00.spectra.framework.security.properties.SecurityProperties(),
                policies);
        var issuer = new SecuritySessionIssueService(store, mock(SecuritySessionRevocationService.class),
                new SessionConcurrencyStrategyResolver(List.of(
                        new AllowSessionConcurrencyStrategy(),
                        new KickOldSessionConcurrencyStrategy(store),
                        new RejectNewSessionConcurrencyStrategy())));
        SecurityPrincipal user = mock(SecurityPrincipal.class);
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("root");
        when(user.getAuthorityNames()).thenReturn(List.of("ROLE_DEV_OPS"));

        assertThrows(SecurityRedisUnavailableException.class,
                () -> issuer.createToken(user, com.devops00.spectra.common.constant.ClientType.WEB));

        ArgumentCaptor<String> sessionKey = ArgumentCaptor.forClass(String.class);
        verify(hashes, atLeastOnce()).putAll(sessionKey.capture(), any());
        verify(redis).delete(SESSION.format(sessionKey.getValue().substring("sec:sess:".length())));
    }
}
