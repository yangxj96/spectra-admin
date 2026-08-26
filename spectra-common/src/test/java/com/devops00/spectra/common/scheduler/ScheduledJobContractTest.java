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

package com.devops00.spectra.common.scheduler;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 调度处理器公共契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/26
 */
class ScheduledJobContractTest {

    @Test
    void descriptorMustExposeGlobalHandlerIdentityAndExecutionPolicy() {
        var descriptor = ScheduledJobDescriptor.builder()
                .jobKey("sample.job")
                .handlerKey("sample.handler")
                .jobType(ScheduledJobType.OPS)
                .runScope(ScheduledRunScope.SINGLETON)
                .scheduleKind(ScheduledScheduleKind.MANUAL)
                .effectType(ScheduledEffectType.DB_ONLY)
                .build();

        assertThat(descriptor.jobKey()).isEqualTo("sample.job");
        assertThat(descriptor.handlerKey()).isEqualTo("sample.handler");
        assertThat(descriptor.jobType()).isEqualTo(ScheduledJobType.OPS);
    }

    @Test
    void executionContextCarriesInstantAndSystemJobIdentity() {
        var context = ScheduledJobContext.builder()
                .executionId(UUID.randomUUID())
                .jobKey("sample.job")
                .handlerKey("sample.handler")
                .fireKey("sample.job:fire-1")
                .scheduledAt(Instant.parse("2026-08-26T00:00:00Z"))
                .build();

        assertThat(context.scheduledAt()).isEqualTo(Instant.parse("2026-08-26T00:00:00Z"));
        assertThat(context.actorType()).isEqualTo("SYSTEM_JOB");
    }
}
