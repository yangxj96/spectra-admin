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

import org.jspecify.annotations.NullMarked;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.stereotype.Component;

/**
 * 将运行期依赖就绪状态反馈给健康端点。
 *
 * <p>依赖可用时报告 UP；运行故障未就绪时报告 OUT_OF_SERVICE 并附带诊断原因，供编排与运维观测。
 * 进程保持存活，与退出型启动门区分。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@Component("spectraReadinessHealthIndicator")
@NullMarked
public class ReadinessHealthIndicator implements HealthIndicator {

    private static final String OUT_OF_SERVICE = "OUT_OF_SERVICE";

    private final DependencyReadinessState state;

    public ReadinessHealthIndicator(DependencyReadinessState state) {
        this.state = state;
    }

    @Override
    public Health health() {
        if (state.isReady()) {
            return Health.up().build();
        }
        String reason = state.reason();
        return Health.status(new Status(OUT_OF_SERVICE))
                .withDetail("reason", reason == null ? "dependency_unavailable" : reason)
                .build();
    }
}
