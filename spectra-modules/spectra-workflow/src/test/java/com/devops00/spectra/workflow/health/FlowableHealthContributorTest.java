/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.workflow.health;

import com.devops00.spectra.common.health.DependencyHealthStatus;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Flowable 公共健康协议回归。 */
class FlowableHealthContributorTest {

    @Test
    void shouldReportFlowableEngineAvailability() {
        var repositoryService = mock(RepositoryService.class);
        var query = mock(ProcessDefinitionQuery.class);
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(query);
        when(query.count()).thenReturn(0L);

        var result = new FlowableHealthContributor(repositoryService).check();

        assertEquals(DependencyHealthStatus.UP, result.status());
        assertEquals("FLOWABLE", result.dependencyType());
    }
}
