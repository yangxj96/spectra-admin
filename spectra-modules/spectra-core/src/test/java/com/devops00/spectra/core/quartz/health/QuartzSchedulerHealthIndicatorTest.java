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

package com.devops00.spectra.core.quartz.health;

import com.devops00.spectra.common.health.DependencyHealthStatus;
import com.devops00.spectra.core.quartz.configuration.QuartzSchedulerLifecycle;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.quartz.Scheduler;
import org.quartz.SchedulerMetaData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Quartz 健康检查的就绪和集群持久化契约。 */
class QuartzSchedulerHealthIndicatorTest {

    @Test
    void notReadySchedulerMustBeDownWithoutConnectionDetails() {
        var lifecycle = Mockito.mock(QuartzSchedulerLifecycle.class);
        when(lifecycle.isReady()).thenReturn(false);
        when(lifecycle.getState()).thenReturn(QuartzSchedulerLifecycle.State.NOT_READY);
        when(lifecycle.getFailureCode()).thenReturn("DATABASE_UNAVAILABLE");

        var result = new QuartzSchedulerHealthIndicator(lifecycle, Mockito.mock(Scheduler.class)).check();

        assertThat(result.status()).isEqualTo(DependencyHealthStatus.DOWN);
        assertThat(result.safeSummary()).doesNotContain("jdbc", "password");
    }

    @Test
    void persistentClusterSchedulerMustBeUp() throws Exception {
        var lifecycle = Mockito.mock(QuartzSchedulerLifecycle.class);
        var scheduler = Mockito.mock(Scheduler.class);
        var metadata = Mockito.mock(SchedulerMetaData.class);
        when(lifecycle.isReady()).thenReturn(true);
        when(scheduler.getMetaData()).thenReturn(metadata);
        when(metadata.isJobStoreSupportsPersistence()).thenReturn(true);
        when(metadata.isJobStoreClustered()).thenReturn(true);
        when(metadata.isStarted()).thenReturn(true);
        when(metadata.isInStandbyMode()).thenReturn(false);
        when(metadata.isShutdown()).thenReturn(false);
        when(metadata.getSchedulerInstanceId()).thenReturn("instance-1");

        var result = new QuartzSchedulerHealthIndicator(lifecycle, scheduler).check();

        assertThat(result.status()).isEqualTo(DependencyHealthStatus.UP);
        assertThat(result.safeSummary()).contains("clustered=true", "instance=instance-1");
    }
}
