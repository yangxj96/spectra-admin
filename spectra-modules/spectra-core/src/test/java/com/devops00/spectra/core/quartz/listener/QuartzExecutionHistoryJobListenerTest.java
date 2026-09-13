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

package com.devops00.spectra.core.quartz.listener;

import com.devops00.spectra.core.quartz.service.QuartzJobExecutionHistoryService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * 验证 {@code QuartzExecutionHistoryJobListenerTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class QuartzExecutionHistoryJobListenerTest {

    @Test
    void historyWriteFailureMustNotEscapeQuartzCallback() {
        QuartzJobExecutionHistoryService service = Mockito.mock(QuartzJobExecutionHistoryService.class);
        Mockito.doThrow(new IllegalStateException("sensitive database detail")).when(service).started(null);
        QuartzExecutionHistoryJobListener listener = new QuartzExecutionHistoryJobListener(service);
        Assertions.assertThatCode(() -> listener.jobToBeExecuted(null)).doesNotThrowAnyException();
    }

    @Test
    void listenerNameMustBeStable() {
        QuartzExecutionHistoryJobListener listener = new QuartzExecutionHistoryJobListener(
                Mockito.mock(QuartzJobExecutionHistoryService.class));
        Assertions.assertThat(listener.getName()).isEqualTo("spectraQuartzExecutionHistory");
    }
}
