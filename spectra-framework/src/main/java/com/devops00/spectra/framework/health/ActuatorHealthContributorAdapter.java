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

package com.devops00.spectra.framework.health;

import com.devops00.spectra.common.health.DependencyHealthResult;
import com.devops00.spectra.common.health.DependencyHealthSnapshot;
import com.devops00.spectra.common.health.DependencyHealthSnapshotProvider;
import com.devops00.spectra.common.health.DependencyHealthStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Spring Boot Actuator 技术适配器。
 *
 * <p>适配器只把 core 提供的统一快照转换成 Actuator {@link Health}，不执行 contributor、不聚合状态，也不
 * 读取业务模块实现。详情只复制公共协议中的安全字段。</p>
 */
@Component("spectraHealth")
@ConditionalOnBean(DependencyHealthSnapshotProvider.class)
public class ActuatorHealthContributorAdapter implements HealthIndicator {

    private final DependencyHealthSnapshotProvider snapshotProvider;

    public ActuatorHealthContributorAdapter(DependencyHealthSnapshotProvider snapshotProvider) {
        this.snapshotProvider = snapshotProvider;
    }

    @Override
    public Health health() {
        try {
            var snapshot = snapshotProvider.snapshot();
            var builder = Health.status(new Status(snapshot.status().name()))
                    .withDetail("status", snapshot.status().name())
                    .withDetail("checkedAt", snapshot.checkedAt().toString())
                    .withDetail("latencyMs", snapshot.latency().toMillis());
            for (var result : snapshot.results()) {
                builder.withDetail(result.contributorName(), details(result));
            }
            return builder.build();
        } catch (RuntimeException exception) {
            return Health.down()
                    .withDetail("status", DependencyHealthStatus.DOWN.name())
                    .withDetail("errorCode", "HEALTH_AGGREGATION_FAILED")
                    .withDetail("message", "统一健康聚合失败")
                    .build();
        }
    }

    private static Map<String, Object> details(DependencyHealthResult result) {
        var details = new LinkedHashMap<String, Object>();
        details.put("module", result.moduleName());
        details.put("dependencyType", result.dependencyType());
        details.put("status", result.status().name());
        details.put("latencyMs", result.latency().toMillis());
        details.put("checkedAt", result.checkedAt().toString());
        if (result.errorCode() != null) {
            details.put("errorCode", result.errorCode());
        }
        if (result.safeSummary() != null) {
            details.put("message", result.safeSummary());
        }
        return Map.copyOf(details);
    }
}
