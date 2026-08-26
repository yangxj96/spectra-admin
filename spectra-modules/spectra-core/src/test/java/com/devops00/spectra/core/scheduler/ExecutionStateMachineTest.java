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

import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerExecutionStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerResolutionStatus;
import com.devops00.spectra.core.scheduler.service.ExecutionStateMachine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExecutionStateMachineTest {

    private final ExecutionStateMachine stateMachine = new ExecutionStateMachine();

    @Test
    void acceptsNormalExecutionAndSafeRetryTransitions() {
        assertEquals(SchedulerExecutionStatus.RUNNING,
                stateMachine.transition(SchedulerExecutionStatus.QUEUED, SchedulerExecutionStatus.RUNNING));
        assertEquals(SchedulerExecutionStatus.RETRY_WAIT,
                stateMachine.transition(SchedulerExecutionStatus.RUNNING, SchedulerExecutionStatus.RETRY_WAIT));
        assertEquals(SchedulerExecutionStatus.QUEUED,
                stateMachine.transition(SchedulerExecutionStatus.RETRY_WAIT, SchedulerExecutionStatus.QUEUED));
        assertEquals(SchedulerExecutionStatus.SUCCEEDED,
                stateMachine.transition(SchedulerExecutionStatus.RUNNING, SchedulerExecutionStatus.SUCCEEDED));
    }

    @Test
    void keepsUnknownAsAnImmutableOriginalState() {
        assertEquals(SchedulerResolutionStatus.CONFIRMED_SUCCESS,
                stateMachine.resolveUnknown(SchedulerExecutionStatus.UNKNOWN,
                        SchedulerResolutionStatus.CONFIRMED_SUCCESS));
        assertThrows(IllegalStateException.class,
                () -> stateMachine.transition(SchedulerExecutionStatus.UNKNOWN, SchedulerExecutionStatus.RUNNING));
        assertThrows(IllegalStateException.class,
                () -> stateMachine.resolveUnknown(SchedulerExecutionStatus.FAILED,
                        SchedulerResolutionStatus.CONFIRMED_FAILED));
    }
}
