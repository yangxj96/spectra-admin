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

package com.devops00.spectra.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class ServiceSplitContractTest {

    private static final Path JAVA_ROOT = Path.of("src", "main", "java", "com", "devops00", "spectra", "core");

    @Test
    void publicEntryServicesMustKeepAThinDependencySurface() throws IOException {
        assertDependencyCountAtMost("user/imports/service/impl/UserImportServiceImpl.java", 8);
        assertDependencyCountAtMost("scheduler/quartz/service/impl/QuartzJobManagementServiceImpl.java", 8);
        assertDependencyCountAtMost("system/service/impl/ServiceMonitorServiceImpl.java", 8);
        assertDependencyCountAtMost("security/authorization/service/impl/AuthorizationAssignmentChangeServiceImpl.java", 8);
    }

    @Test
    void eachUseCaseGroupMustHaveAnIndependentService() {
        assertFilesExist(List.of(
                "user/imports/service/impl/UserImportPreviewService.java",
                "user/imports/service/impl/UserImportExecutionService.java",
                "user/imports/service/impl/UserImportResultService.java",
                "scheduler/quartz/service/QuartzJobManagementService.java",
                "scheduler/history/service/QuartzJobExecutionHistoryService.java",
                "system/service/impl/ServiceMonitorEvaluationService.java",
                "system/service/impl/ServiceMonitorQueryService.java",
                "security/authorization/service/impl/AuthorizationImpactService.java",
                "security/authorization/service/impl/AuthorizationAuditService.java"));
    }

    @Test
    void asynchronousAndTransactionalBoundariesMustBeVisibleInDedicatedBeans() throws IOException {
        String execution = readSource("user/imports/service/impl/UserImportExecutionService.java");
        String worker = readSource("user/imports/service/impl/UserImportExecutionWorker.java");
        String control = readSource("scheduler/quartz/service/QuartzJobManagementService.java");
        Assertions.assertThat(execution).contains("userImportTaskExecutor");
        Assertions.assertThat(worker).contains("@Transactional");
        Assertions.assertThat(worker).contains("CHUNK_SIZE");
        Assertions.assertThat(control).contains("QuartzJobManagementService");
    }

    private void assertDependencyCountAtMost(String relativePath, int maximum) throws IOException {
        String source = readSource(relativePath);
        long dependencies = source.lines()
                .filter(line -> line.stripLeading().startsWith("private final "))
                .count();
        Assertions.assertThat(dependencies)
                .as("公开入口依赖过多: %s", relativePath)
                .isLessThanOrEqualTo(maximum);
    }

    private void assertFilesExist(List<String> relativePaths) {
        for (String relativePath : relativePaths) {
            Path path = JAVA_ROOT.resolve(relativePath);
            Assertions.assertThat(Files.isRegularFile(path))
                    .as("缺少拆分后的职责 Service: %s", path)
                    .isTrue();
        }
    }

    private String readSource(String relativePath) throws IOException {
        Path path = JAVA_ROOT.resolve(relativePath);
        Assertions.assertThat(Files.isRegularFile(path))
                .as("缺少服务实现: %s", path)
                .isTrue();
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
