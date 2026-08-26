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

package com.devops00.spectra.core.scheduler;

import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerLoopErrorEntity;
import com.devops00.spectra.core.scheduler.mapper.SchedulerLoopErrorMapper;
import com.devops00.spectra.core.scheduler.service.LoopErrorAggregator;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoopErrorAggregatorTest {

    @Test
    void sameFingerprintIsSuppressedBetweenLogIntervals() {
        var mapper = mock(SchedulerLoopErrorMapper.class);
        var jobId = UUID.randomUUID();
        var runtimeId = UUID.randomUUID();
        var first = error(Instant.parse("2026-08-26T00:00:00Z"), Instant.parse("2026-08-26T00:00:00Z"), 1L, 0L);
        var second = error(Instant.parse("2026-08-26T00:00:00Z"), Instant.parse("2026-08-26T00:00:10Z"), 2L, 1L);
        when(mapper.upsertOccurrence(any(), anyLong())).thenReturn(first, second);
        var aggregator = new LoopErrorAggregator(mapper);

        var firstOccurrence = aggregator.record(jobId, runtimeId, "instance-a", "PROVIDER_ERROR",
                "provider unavailable", Map.of("attempt", 1), java.time.Duration.ofMinutes(1), first.getLastSeenAt());
        var secondOccurrence = aggregator.record(jobId, runtimeId, "instance-a", "PROVIDER_ERROR",
                "provider unavailable", Map.of("attempt", 2), java.time.Duration.ofMinutes(1), second.getLastSeenAt());

        assertTrue(firstOccurrence.shouldLog());
        assertFalse(secondOccurrence.shouldLog());
        assertNotEquals(first.getId(), second.getId());
    }

    private static SchedulerLoopErrorEntity error(Instant lastLoggedAt, Instant lastSeenAt,
                                                  long occurrences, long suppressed) {
        var error = new SchedulerLoopErrorEntity();
        error.setId(UUID.randomUUID());
        error.setErrorCode("PROVIDER_ERROR");
        error.setLastLoggedAt(lastLoggedAt);
        error.setLastSeenAt(lastSeenAt);
        error.setOccurrenceCount(occurrences);
        error.setSuppressedCount(suppressed);
        return error;
    }
}
