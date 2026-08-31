/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.core.scheduler.health;

import com.devops00.spectra.common.health.DependencyHealthStatus;
import com.devops00.spectra.core.scheduler.mapper.SchedulerJobMapper;
import com.devops00.spectra.core.scheduler.service.SchedulerBootstrapService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 调度器公共健康协议回归。 */
class SchedulerHealthIndicatorTest {

    @Test
    void shouldReturnUpWhenSchedulerIsReady() {
        var bootstrap = mock(SchedulerBootstrapService.class);
        var mapper = mock(SchedulerJobMapper.class);
        when(bootstrap.isReady()).thenReturn(true);
        when(bootstrap.getState()).thenReturn("READY");
        when(mapper.selectCount(null)).thenReturn(0L);

        var result = new SchedulerHealthIndicator(bootstrap, mapper).check();

        assertEquals(DependencyHealthStatus.UP, result.status());
    }

    @Test
    void shouldReturnDownWithoutRawDatabaseFailureMessage() {
        var bootstrap = mock(SchedulerBootstrapService.class);
        var mapper = mock(SchedulerJobMapper.class);
        when(mapper.selectCount(null)).thenThrow(new IllegalStateException("jdbc://secret-host/password=secret"));

        var result = new SchedulerHealthIndicator(bootstrap, mapper).check();

        assertEquals(DependencyHealthStatus.DOWN, result.status());
        assertEquals("DATABASE_UNAVAILABLE", result.errorCode());
        org.junit.jupiter.api.Assertions.assertFalse(result.safeSummary().contains("secret"));
    }
}
