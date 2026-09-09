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

package com.devops00.spectra.core.scheduler.quartz.configuration;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.spi.TriggerFiredBundle;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** SpringQuartzJobFactory 的依赖注入契约。 */
class SpringQuartzJobFactoryTest {

    @Test
    void newJobMustUseSpringAutowireAndInitialization() throws Exception {
        var beanFactory = Mockito.mock(AutowireCapableBeanFactory.class);
        var factory = new SpringQuartzJobFactory(beanFactory);
        var detail = JobBuilder.newJob(SampleJob.class).withIdentity("sample").build();
        var bundle = Mockito.mock(TriggerFiredBundle.class);
        when(bundle.getJobDetail()).thenReturn(detail);
        when(beanFactory.initializeBean(any(SampleJob.class), any(String.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var job = factory.newJob(bundle, Mockito.mock(Scheduler.class));

        assertThat(job).isInstanceOf(SampleJob.class);
        verify(beanFactory).autowireBean(any(SampleJob.class));
        verify(beanFactory).initializeBean(any(SampleJob.class), any(String.class));
    }

    /** 测试用无参 Quartz Job。 */
    public static class SampleJob implements Job {

        @Override
        public void execute(JobExecutionContext context) {
        }
    }
}
