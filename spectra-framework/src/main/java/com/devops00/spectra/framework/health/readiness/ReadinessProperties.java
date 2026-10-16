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

package com.devops00.spectra.framework.health.readiness;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 运行期就绪探针参数。
 *
 * @param probeIntervalMs 正常探测间隔（毫秒），缺省或非正时取 5000
 * @param maxIntervalMs   故障退避后最大间隔（毫秒），小于探测间隔时取至少 30000
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@ConfigurationProperties(prefix = "spectra.readiness")
public record ReadinessProperties(long probeIntervalMs, long maxIntervalMs) {

    public ReadinessProperties {
        probeIntervalMs = probeIntervalMs <= 0 ? 5000L : probeIntervalMs;
        maxIntervalMs = maxIntervalMs < probeIntervalMs ? Math.max(probeIntervalMs, 30000L) : maxIntervalMs;
    }
}
