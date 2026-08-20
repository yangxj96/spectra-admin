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

package com.devops00.spectra.core.security.authorization.service.impl;

import com.devops00.spectra.security.base.change.AuthorizationChangeToken;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Preview/Apply token 的签名、篡改和过期契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
class HmacAuthorizationChangeTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-14T00:00:00Z");

    @Test
    void tokenIsBoundToItsPayloadAndExpires() {
        var service = service(NOW);
        var token = new AuthorizationChangeToken(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), 7L, "request-hash", NOW.plusSeconds(60));

        var encoded = service.issue(token);
        assertEquals(token, service.verify(encoded));
        assertThrows(IllegalArgumentException.class, () -> service.verify(encoded + "x"));

        var expired = new HmacAuthorizationChangeTokenService("01234567890123456789012345678901",
                Clock.fixed(NOW.plusSeconds(60), ZoneOffset.UTC));
        assertThrows(IllegalArgumentException.class, () -> expired.verify(encoded));
    }

    @Test
    void shortSecretFailsClosed() {
        var service = new HmacAuthorizationChangeTokenService("short", Clock.fixed(NOW, ZoneOffset.UTC));
        assertThrows(IllegalStateException.class, () -> service.issue(new AuthorizationChangeToken(UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 1L, "hash", NOW.plusSeconds(1))));
    }

    @Test
    void tokenSupportsIdsGeneratedAfterPreview() {
        var service = service(NOW);
        var token = new AuthorizationChangeToken(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, null,
                7L, "request-hash", NOW.plusSeconds(60));

        assertEquals(token, service.verify(service.issue(token)));
    }

    private static HmacAuthorizationChangeTokenService service(Instant now) {
        return new HmacAuthorizationChangeTokenService("01234567890123456789012345678901",
                Clock.fixed(now, ZoneOffset.UTC));
    }
}
