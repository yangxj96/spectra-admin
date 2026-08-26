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

import com.devops00.spectra.core.scheduler.service.ExecutionStateMachine;
import com.devops00.spectra.core.scheduler.service.LoopStateMachine;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

/** 调度状态机必须由 Spring 容器管理的组件注册契约测试。 */
class SchedulerStateMachineBeanRegistrationTest {

    @Test
    void stateMachinesAreRegisteredAsSpringComponents() {
        assertThat(ExecutionStateMachine.class.isAnnotationPresent(Component.class)).isTrue();
        assertThat(LoopStateMachine.class.isAnnotationPresent(Component.class)).isTrue();
    }
}
