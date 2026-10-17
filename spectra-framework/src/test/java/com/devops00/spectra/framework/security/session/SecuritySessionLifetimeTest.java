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

import com.devops00.spectra.common.security.policy.SessionConcurrencyMode;
import com.devops00.spectra.common.security.policy.SessionPolicy;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SecuritySessionLifetime} 绝对/空闲 TTL 不被刷新重置的验证。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class SecuritySessionLifetimeTest {

    private static final long LOGIN = 1_700_000_000_000L;

    private static SessionPolicy policy(Long absoluteSeconds, Long idleSeconds) {
        return new SessionPolicy(SessionConcurrencyMode.ALLOW, 5, 300, 600, absoluteSeconds, idleSeconds);
    }

    @Test
    void absoluteTtlIsNotResetByTouch() {
        var lifetime = new SecuritySessionLifetime(LOGIN, LOGIN);
        long now = LOGIN + 1_000_001L;

        var touched = lifetime.touch(now);

        assertEquals(LOGIN, touched.loginTime());
        assertTrue(touched.expired(policy(1000L, null), now));
    }

    @Test
    void idleTtlExpiresWithoutActivity() {
        var lifetime = new SecuritySessionLifetime(LOGIN, LOGIN);

        assertTrue(lifetime.expired(policy(null, 500L), LOGIN + 500_001L));
    }

    @Test
    void touchExtendsIdleWindow() {
        var lifetime = new SecuritySessionLifetime(LOGIN, LOGIN);
        long now = LOGIN + 400_000L;

        var touched = lifetime.touch(now);

        assertFalse(touched.expired(policy(null, 500L), now + 400_000L));
    }

    @Test
    void refreshTtlIsBoundedByAbsoluteRemaining() {
        var lifetime = new SecuritySessionLifetime(LOGIN, LOGIN);

        assertEquals(Duration.ofSeconds(100), lifetime.refreshTtl(policy(1000L, null), LOGIN + 900_000L));
    }
}
