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

package com.devops00.spectra.workflow.health;

import com.devops00.spectra.common.health.DependencyHealthContributor;
import com.devops00.spectra.common.health.DependencyHealthResult;
import com.devops00.spectra.common.health.DependencyHealthStatus;
import org.flowable.engine.RepositoryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/** Workflow 模块的 Flowable 引擎健康检查。 */
@Component("flowableHealthContributor")
@ConditionalOnProperty(prefix = "spectra.modules.workflow", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FlowableHealthContributor implements DependencyHealthContributor {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final RepositoryService repositoryService;

    public FlowableHealthContributor(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @Override
    public String contributorName() {
        return "flowable";
    }

    @Override
    public String moduleName() {
        return "workflow";
    }

    @Override
    public String dependencyType() {
        return "FLOWABLE";
    }

    @Override
    public Duration timeout() {
        return TIMEOUT;
    }

    @Override
    public DependencyHealthResult check() {
        var start = System.nanoTime();
        try {
            repositoryService.createProcessDefinitionQuery().count();
            return result(DependencyHealthStatus.UP, start, null, "Flowable 引擎检查正常");
        } catch (RuntimeException exception) {
            return result(DependencyHealthStatus.DOWN, start, "FLOWABLE_CHECK_FAILED", "Flowable 引擎检查失败");
        }
    }

    private DependencyHealthResult result(DependencyHealthStatus status, long start, String errorCode,
                                          String safeSummary) {
        return new DependencyHealthResult(contributorName(), moduleName(), dependencyType(), status,
                Duration.ofNanos(System.nanoTime() - start), Instant.now(), errorCode, safeSummary);
    }
}
