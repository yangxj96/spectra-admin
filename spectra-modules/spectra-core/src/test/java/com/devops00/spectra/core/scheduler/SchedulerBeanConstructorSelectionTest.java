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

import com.devops00.spectra.core.scheduler.service.LoopErrorAggregator;
import com.devops00.spectra.core.scheduler.service.SchedulerControlCommandService;
import com.devops00.spectra.core.scheduler.service.SchedulerExecutionService;
import com.devops00.spectra.core.scheduler.worker.DiscreteDispatchWorker;
import com.devops00.spectra.core.scheduler.worker.LoopController;
import com.devops00.spectra.core.scheduler.worker.SingletonLoopLeaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** 调度组件存在测试时钟构造器时，必须明确选择生产构造器。 */
class SchedulerBeanConstructorSelectionTest {

    private static final Map<Class<?>, Integer> PRODUCTION_CONSTRUCTOR_PARAMETER_COUNTS = Map.of(
            SchedulerExecutionService.class, 7,
            SchedulerControlCommandService.class, 5,
            SingletonLoopLeaseService.class, 2,
            LoopController.class, 9,
            DiscreteDispatchWorker.class, 5,
            LoopErrorAggregator.class, 1);

    @Test
    void schedulerBeansSelectExactlyOneProductionConstructor() {
        for (var entry : PRODUCTION_CONSTRUCTOR_PARAMETER_COUNTS.entrySet()) {
            var autowiredConstructors = Arrays.stream(entry.getKey().getDeclaredConstructors())
                    .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
                    .toList();

            assertThat(autowiredConstructors)
                    .as("Bean %s", entry.getKey().getSimpleName())
                    .hasSize(1);
            assertThat(autowiredConstructors.getFirst().getParameterCount())
                    .as("Bean %s", entry.getKey().getSimpleName())
                    .isEqualTo(entry.getValue());
        }
    }
}
