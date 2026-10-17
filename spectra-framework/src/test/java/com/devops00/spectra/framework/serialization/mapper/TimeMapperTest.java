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

package com.devops00.spectra.framework.serialization.mapper;

import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link TimeMapper#toInstant(String)} 严格 ISO 日期时间解析的验证。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class TimeMapperTest {

    private static TimeMapper mapper() {
        return mapper("UTC");
    }

    private static TimeMapper mapper(String zoneId) {
        var accessor = mock(SecurityContextAccessor.class);
        when(accessor.currentUserZoneId()).thenReturn(zoneId);
        return new TimeMapper(accessor);
    }

    @Test
    void offsetAndZonedInputsKeepAbsoluteInstant() {
        var mapper = mapper();
        assertEquals(Instant.parse("2020-01-01T00:00:00Z"), mapper.toInstant("2020-01-01T08:00:00+08:00"));
        assertEquals(Instant.parse("2020-01-01T00:00:00Z"),
                mapper.toInstant("2020-01-01T08:00:00+08:00[Asia/Shanghai]"));
    }

    @Test
    void localDateTimeUsesUserZone() {
        assertEquals(Instant.parse("2020-01-01T00:00:00Z"), mapper().toInstant("2020-01-01T00:00:00"));
        assertEquals(Instant.parse("2020-01-01T00:00:00.123Z"), mapper().toInstant("2020-01-01T00:00:00.123"));
    }

    @Test
    void dateOnlyAndTimeOnlyAreRejected() {
        var mapper = mapper();
        assertThrows(DateTimeParseException.class, () -> mapper.toInstant("2020-01-01"));
        assertThrows(DateTimeParseException.class, () -> mapper.toInstant("08:00:00"));
    }

    @Test
    void blankInputReturnsNull() {
        assertNull(mapper().toInstant("  "));
    }

    @Test
    void offsetDateTimeKeepsUserOffset() {
        var value = mapper().toOffsetDateTime(Instant.parse("2020-01-01T00:00:00Z").toEpochMilli());
        assertEquals(ZoneOffset.UTC, value.getOffset());
        assertEquals(Instant.parse("2020-01-01T00:00:00Z"), value.toInstant());
    }

    @Test
    void isoStringCarriesUserOffset() {
        var text = mapper("Asia/Shanghai").toString(Instant.parse("2020-01-01T00:00:00Z"));
        assertTrue(text.contains("+08:00"), text);
    }

    @Test
    void dstGapResolvesDeterministically() {
        assertEquals(Instant.parse("2024-03-10T07:30:00Z"),
                mapper("America/New_York").toInstant("2024-03-10T02:30:00"));
    }

    @Test
    void dstOverlapUsesEarlierOffset() {
        assertEquals(Instant.parse("2024-11-03T05:30:00Z"),
                mapper("America/New_York").toInstant("2024-11-03T01:30:00"));
    }

    @Test
    void dateConvertsToOffsetDateTime() {
        var value = mapper().toOffsetDateTime(Date.from(Instant.parse("2020-01-01T00:00:00Z")));
        assertEquals(Instant.parse("2020-01-01T00:00:00Z"), value.toInstant());
        assertEquals(ZoneOffset.UTC, value.getOffset());
    }

    @Test
    void localDateStartsAtUserZoneOffset() {
        var value = mapper("Asia/Shanghai").toOffsetDateTime(LocalDate.of(2020, 1, 1));
        assertEquals(Instant.parse("2019-12-31T16:00:00Z"), value.toInstant());
        assertEquals(ZoneOffset.ofHours(8), value.getOffset());
    }
}
