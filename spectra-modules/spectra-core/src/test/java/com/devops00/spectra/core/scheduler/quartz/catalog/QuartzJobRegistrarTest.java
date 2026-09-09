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

package com.devops00.spectra.core.scheduler.quartz.catalog;

import com.devops00.spectra.common.port.scheduler.quartz.QuartzTriggerTemplate;
import com.devops00.spectra.common.port.scheduler.quartz.QuartzJobDefinition;
import com.devops00.spectra.common.port.scheduler.quartz.QuartzParameterSchema;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 内置 Job/Trigger 只补缺失且不覆盖既有资源的契约。 */
class QuartzJobRegistrarTest {

    @Test
    void registrarMustCreateDurableProtectedJobWithoutRecovery() throws Exception {
        var definition = definition();
        var catalog = Mockito.mock(QuartzJobCatalog.class);
        when(catalog.definitions()).thenReturn(List.of(definition));
        var registrar = new QuartzJobRegistrar(catalog);
        var scheduler = Mockito.mock(Scheduler.class);
        when(scheduler.checkExists(any(org.quartz.JobKey.class))).thenReturn(false);
        when(scheduler.checkExists(any(org.quartz.TriggerKey.class))).thenReturn(false);

        registrar.register(scheduler);

        var jobCaptor = ArgumentCaptor.forClass(JobDetail.class);
        verify(scheduler).addJob(jobCaptor.capture(), Mockito.eq(false));
        assertThat(jobCaptor.getValue().isDurable()).isTrue();
        assertThat(jobCaptor.getValue().requestsRecovery()).isFalse();
        assertThat(jobCaptor.getValue().getJobDataMap().getBoolean("spectra.job.built-in")).isTrue();
        verify(scheduler).scheduleJob(any(Trigger.class));
    }

    @Test
    void registrarMustNotReplaceExistingResources() throws Exception {
        var catalog = Mockito.mock(QuartzJobCatalog.class);
        when(catalog.definitions()).thenReturn(List.of(definition()));
        var registrar = new QuartzJobRegistrar(catalog);
        var scheduler = Mockito.mock(Scheduler.class);
        when(scheduler.checkExists(any(org.quartz.JobKey.class))).thenReturn(true);
        when(scheduler.checkExists(any(org.quartz.TriggerKey.class))).thenReturn(true);

        registrar.register(scheduler);

        verify(scheduler, never()).addJob(any(JobDetail.class), Mockito.anyBoolean());
        verify(scheduler, never()).scheduleJob(any(Trigger.class));
    }

    private QuartzJobDefinition definition() {
        return new QuartzJobDefinition() {
            @Override
            public String typeKey() {
                return "sample.built-in";
            }

            @Override
            public String displayName() {
                return "Sample Built-in";
            }

            @Override
            public Class<? extends Job> jobClass() {
                return SampleJob.class;
            }

            @Override
            public boolean builtIn() {
                return true;
            }

            @Override
            public Optional<String> builtInJobKey() {
                return Optional.of("sample.built-in");
            }

            @Override
            public QuartzParameterSchema parameterSchema() {
                return QuartzParameterSchema.empty();
            }

            @Override
            public Optional<QuartzTriggerTemplate> defaultTrigger() {
                return Optional.of(QuartzTriggerTemplate.oneShot(null));
            }
        };
    }

    /** 测试用 Quartz Job。 */
    private static final class SampleJob implements Job {

        @Override
        public void execute(JobExecutionContext context) {
        }
    }
}
