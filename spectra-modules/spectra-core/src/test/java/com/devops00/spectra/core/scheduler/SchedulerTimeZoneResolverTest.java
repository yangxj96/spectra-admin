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

import com.devops00.spectra.core.scheduler.service.SchedulerTimeZoneResolver;
import com.devops00.spectra.core.system.service.ConfiguredService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchedulerTimeZoneResolverTest {

    @Mock
    private ConfiguredService configuredService;

    @ParameterizedTest
    @CsvSource({"Asia/Shanghai,Asia/Shanghai", "UTC,UTC", "' ',UTC", "Invalid/Zone,UTC"})
    void resolvesSystemTimezoneWithUtcFallback(String configured, String expected) {
        var resolver = new SchedulerTimeZoneResolver(configuredService);

        assertEquals(ZoneId.of(expected), resolver.resolve(configured));
    }

    @Test
    void missingSystemTimezoneFallsBackToUtc() {
        when(configuredService.findValue("system.default-timezone")).thenReturn(Optional.empty());

        var resolver = new SchedulerTimeZoneResolver(configuredService);

        assertEquals(ZoneId.of("UTC"), resolver.resolve());
    }
}
