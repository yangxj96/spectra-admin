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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** 调度内核自身的固定轮询参数。 */
@Data
@ConfigurationProperties(prefix = "spectra.scheduler")
public class SchedulerProperties {

    /** 是否启用调度内核；数据库仍然是启动门禁。 */
    private boolean enabled = true;

    /** 调度内核 tick 间隔，不代表任一业务任务的执行频率。 */
    private Duration pollInterval = Duration.ofSeconds(10);

    /** 单次查询的最大到期任务数。 */
    private int dueBatchSize = 50;
}
