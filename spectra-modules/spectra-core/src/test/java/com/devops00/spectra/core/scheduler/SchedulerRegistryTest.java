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

import com.devops00.spectra.common.scheduler.ScheduledEffectType;
import com.devops00.spectra.common.scheduler.ScheduledJobDescriptor;
import com.devops00.spectra.common.scheduler.ScheduledJobHandler;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.common.scheduler.ScheduledTriggerType;
import com.devops00.spectra.core.scheduler.service.impl.ScheduledJobRegistryImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SchedulerRegistryTest {

    @Test
    void indexesRegisteredHandlersByJobKey() {
        var handler = handler("test.job");

        var registry = new ScheduledJobRegistryImpl(List.of(handler), List.of());

        assertEquals(handler, registry.findJobHandler("test.job").orElseThrow());
        assertEquals(1, registry.descriptors().size());
    }

    @Test
    void rejectsDuplicateJobKeysAcrossHandlerTypes() {
        var discrete = handler("duplicate.job");
        var loop = mock(com.devops00.spectra.common.scheduler.ScheduledLoopHandler.class);
        when(loop.descriptor()).thenReturn(descriptor("duplicate.job", ScheduledJobType.LOOP));

        assertThrows(IllegalStateException.class, () -> new ScheduledJobRegistryImpl(List.of(discrete), List.of(loop)));
    }

    private static ScheduledJobHandler handler(String jobKey) {
        var handler = mock(ScheduledJobHandler.class);
        when(handler.descriptor()).thenReturn(descriptor(jobKey, ScheduledJobType.SYSTEM));
        return handler;
    }

    private static ScheduledJobDescriptor descriptor(String jobKey, ScheduledJobType type) {
        return ScheduledJobDescriptor.builder()
                .jobKey(jobKey)
                .handlerKey(jobKey)
                .name(jobKey)
                .module("test")
                .jobType(type)
                .runScope(type == ScheduledJobType.LOOP ? ScheduledRunScope.PER_INSTANCE : ScheduledRunScope.SINGLETON)
                .scheduleKind(ScheduledScheduleKind.FIXED_DELAY)
                .effectType(ScheduledEffectType.DB_ONLY)
                .build();
    }
}
