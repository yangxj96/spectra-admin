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

package com.devops00.spectra.framework.serialization.time;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * 应用统一时间基础设施配置。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Configuration(proxyBeanMethods = false)
public class TimeConfiguration {

    /**
     * 提供以 UTC 为基准的系统时钟，供审计、Outbox 和持久化时间戳使用。
     *
     * @return 不随系统默认时区变化的 UTC 时钟；该 Bean 始终返回非 null 值。
     */
    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }
}
