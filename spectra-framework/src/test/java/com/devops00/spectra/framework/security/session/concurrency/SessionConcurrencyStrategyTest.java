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

package com.devops00.spectra.framework.security.session.concurrency;

import com.devops00.spectra.common.security.policy.SessionConcurrencyMode;
import com.devops00.spectra.common.security.policy.SessionPolicy;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.session.SecuritySessionStore;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 会话并发策略的行为等价测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class SessionConcurrencyStrategyTest {

    @Test
    void shouldResolveAllPersistedConcurrencyModes() {
        var store = mock(SecuritySessionStore.class);
        var resolver = new SessionConcurrencyStrategyResolver(List.of(
                new AllowSessionConcurrencyStrategy(),
                new KickOldSessionConcurrencyStrategy(store),
                new RejectNewSessionConcurrencyStrategy()));

        assertThat(resolver.resolve(SessionConcurrencyMode.ALLOW))
                .isInstanceOf(AllowSessionConcurrencyStrategy.class);
        assertThat(resolver.resolve(SessionConcurrencyMode.KICK_OLD))
                .isInstanceOf(KickOldSessionConcurrencyStrategy.class);
        assertThat(resolver.resolve(SessionConcurrencyMode.REJECT_NEW))
                .isInstanceOf(RejectNewSessionConcurrencyStrategy.class);
    }

    @Test
    void resolverShouldRejectDuplicateOrMissingConcurrencyModes() {
        var store = mock(SecuritySessionStore.class);

        assertThatThrownBy(() -> new SessionConcurrencyStrategyResolver(List.of(
                new AllowSessionConcurrencyStrategy(),
                new AllowSessionConcurrencyStrategy(),
                new KickOldSessionConcurrencyStrategy(store),
                new RejectNewSessionConcurrencyStrategy())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("会话并发模式重复注册: ALLOW");
        assertThatThrownBy(() -> new SessionConcurrencyStrategyResolver(List.of(
                new AllowSessionConcurrencyStrategy(),
                new RejectNewSessionConcurrencyStrategy())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("会话并发策略未覆盖全部持久化模式");
    }

    @Test
    void allowShouldNotReadOrRevokeExistingSessions() {
        var revoked = new ArrayList<String>();

        new AllowSessionConcurrencyStrategy().enforce(
                policy(SessionConcurrencyMode.ALLOW, 1), "web", Set.of("old-session"), revoked::add);

        assertThat(revoked).isEmpty();
    }

    @Test
    void kickOldShouldRevokeOnlyActiveSessionsFromTheSameClient() {
        var store = mock(SecuritySessionStore.class);
        when(store.hash("读取活动安全会话", SecurityRedisKey.SESSION.format("web-session")))
                .thenReturn(Map.of("clientType", "web"));
        when(store.hash("读取活动安全会话", SecurityRedisKey.SESSION.format("app-session")))
                .thenReturn(Map.of("clientType", "app"));
        var revoked = new ArrayList<String>();

        new KickOldSessionConcurrencyStrategy(store).enforce(
                policy(SessionConcurrencyMode.KICK_OLD, 1), "web",
                Set.of("web-session", "app-session"), revoked::add);

        assertThat(revoked).containsExactly("web-session");
        verify(store).hash("读取活动安全会话", SecurityRedisKey.SESSION.format("web-session"));
        verify(store).hash("读取活动安全会话", SecurityRedisKey.SESSION.format("app-session"));
    }

    @Test
    void rejectNewShouldPreserveTheExistingCapacityError() {
        var activeTokens = Set.of("one", "two");

        assertThatThrownBy(() -> new RejectNewSessionConcurrencyStrategy().enforce(
                policy(SessionConcurrencyMode.REJECT_NEW, 2), "web", activeTokens, ignored -> {
                }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("已达到该账号的最大并发会话数");
    }

    @Test
    void rejectNewShouldAllowAUserBelowTheConfiguredCapacity() {
        new RejectNewSessionConcurrencyStrategy().enforce(
                policy(SessionConcurrencyMode.REJECT_NEW, 2), "web", Set.of("one"), ignored -> {
                });
    }

    /**
     * 处理策略相关数据。
     */
    private static SessionPolicy policy(SessionConcurrencyMode mode, int maxSessions) {
        return new SessionPolicy(mode, maxSessions, 900, 86_400, null, null);
    }
}
