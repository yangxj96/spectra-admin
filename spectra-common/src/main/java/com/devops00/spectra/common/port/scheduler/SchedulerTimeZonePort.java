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

package com.devops00.spectra.common.port.scheduler;

import java.time.ZoneId;

/**
 * 为跨模块调用方提供调度时区解析能力的稳定端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/7
 */
public interface SchedulerTimeZonePort {

    /**
     * 解析系统配置的默认时区。
     *
     * @return 调度时区
     */
    ZoneId resolve();

    /**
     * 解析指定时区配置。
     *
     * @param configuredValue 时区配置值
     * @return 调度时区
     */
    ZoneId resolve(String configuredValue);
}
