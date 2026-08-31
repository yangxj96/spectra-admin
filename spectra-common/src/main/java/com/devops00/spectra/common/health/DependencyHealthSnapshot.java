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

package com.devops00.spectra.common.health;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * 一次统一健康聚合的不可变快照。
 *
 * <p>快照只承载已经由聚合器完成的状态和分项结果；Actuator、ServiceMonitor 以及管理 API 都应消费同一快照，
 * 不得各自重新执行 contributor 或定义新的聚合优先级。</p>
 *
 * @param status    聚合后的统一状态
 * @param results   按 contributor 名称排序的分项结果
 * @param latency   本次聚合耗时
 * @param checkedAt 本次聚合完成时间（UTC）
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
public record DependencyHealthSnapshot(DependencyHealthStatus status,
                                       List<DependencyHealthResult> results,
                                       Duration latency,
                                       Instant checkedAt) {

    public DependencyHealthSnapshot {
        if (status == null) {
            throw new IllegalArgumentException("health status 不能为空");
        }
        results = results == null ? List.of() : List.copyOf(results);
        if (latency == null) {
            latency = Duration.ZERO;
        }
        if (latency.isNegative()) {
            throw new IllegalArgumentException("health aggregate latency 不能为负数");
        }
        if (checkedAt == null) {
            checkedAt = Instant.now();
        }
    }
}
