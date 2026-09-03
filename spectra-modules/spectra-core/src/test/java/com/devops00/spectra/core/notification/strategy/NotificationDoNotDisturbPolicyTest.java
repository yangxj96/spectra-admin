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

package com.devops00.spectra.core.notification.strategy;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 免打扰跨午夜和用户时区规则测试。
 */
class NotificationDoNotDisturbPolicyTest {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    @Test
    void shouldHandleCrossMidnightWindowInUserTimeZone() {
        var date = LocalDate.of(2026, 8, 13);
        var start = date.atTime(LocalTime.of(22, 0)).atZone(SHANGHAI).toInstant();
        var end = date.plusDays(1).atTime(LocalTime.of(8, 0)).atZone(SHANGHAI).toInstant();

        assertTrue(NotificationDoNotDisturbPolicy.isQuiet(true,
                date.atTime(LocalTime.of(23, 30)).atZone(SHANGHAI).toInstant(), start, end, SHANGHAI));
        assertTrue(NotificationDoNotDisturbPolicy.isQuiet(true,
                date.plusDays(1).atTime(LocalTime.of(7, 59)).atZone(SHANGHAI).toInstant(), start, end, SHANGHAI));
        assertFalse(NotificationDoNotDisturbPolicy.isQuiet(true,
                date.plusDays(1).atTime(LocalTime.of(8, 0)).atZone(SHANGHAI).toInstant(), start, end, SHANGHAI));
        assertFalse(NotificationDoNotDisturbPolicy.isQuiet(true,
                date.plusDays(1).atTime(LocalTime.of(12, 0)).atZone(SHANGHAI).toInstant(), start, end, SHANGHAI));
    }

    @Test
    void shouldFailSafeWhenWindowIsIncompleteAndAllowWhenDisabled() {
        var now = Instant.parse("2026-08-13T12:00:00Z");

        assertTrue(NotificationDoNotDisturbPolicy.isQuiet(true, now, now, null, ZoneId.of("UTC")));
        assertFalse(NotificationDoNotDisturbPolicy.isQuiet(false, now, now, now, ZoneId.of("UTC")));
        assertTrue(NotificationDoNotDisturbPolicy.isQuiet(true, now, now, now, ZoneId.of("UTC")));
    }

    @Test
    void shouldFallbackToUtcForMissingOrInvalidTimezone() {
        var now = Instant.parse("2026-08-13T22:30:00Z");
        var start = Instant.parse("2026-08-13T22:00:00Z");
        var end = Instant.parse("2026-08-13T23:00:00Z");

        assertEquals(ZoneId.of("UTC"), NotificationDoNotDisturbPolicy.resolveZone(null));
        assertEquals(ZoneId.of("UTC"), NotificationDoNotDisturbPolicy.resolveZone("not/a-zone"));
        assertTrue(NotificationDoNotDisturbPolicy.isQuiet(true, now, start, end,
                NotificationDoNotDisturbPolicy.resolveZone("not/a-zone")));
    }
}
