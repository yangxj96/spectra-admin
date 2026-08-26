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

package com.devops00.spectra.core.scheduler.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/** 调度内核基础设施配置。 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SchedulerProperties.class)
public class SchedulerConfiguration {

    /** 创建调度内核统一使用的 UTC 时钟。 */
    @Bean
    public Clock schedulerClock() {
        return Clock.systemUTC();
    }

    /** 创建生命周期由 Spring 管理的调度内核虚拟线程 tick 执行器。 */
    @Bean(name = "schedulerTickExecutor", destroyMethod = "shutdownNow")
    public ScheduledExecutorService schedulerTickExecutor() {
        return Executors.newSingleThreadScheduledExecutor(
                Thread.ofVirtual().name("spectra-scheduler-tick-", 0).factory());
    }
}
