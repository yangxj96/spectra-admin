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

package com.devops00.spectra.core.quartz.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证 Quartz 包结构迁移会修复已持久化的内置 Job 类名。 */
class QuartzJobClassMigrationContractTest {

    @Test
    void migrationMustRepairFlattenedQuartzExecutionHistoryCleanupJobClass() throws IOException {
        try (var resource = QuartzJobClassMigrationContractTest.class
                .getResourceAsStream("/db/migration/V18__repair_quartz_job_class_names.sql")) {
            assertThat(resource).as("缺少 Quartz Job 类名修复迁移").isNotNull();
            String sql = new String(resource.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(sql).containsIgnoringCase("UPDATE spectra_quartz.QRTZ_JOB_DETAILS");
            assertThat(sql).containsIgnoringCase("job_name = 'system.scheduler.execution-history-cleanup'");
            assertThat(sql).containsIgnoringCase("job_group = 'SPECTRA_BUILTIN'");
            assertThat(sql).containsIgnoringCase(
                    "com.devops00.spectra.core.scheduler.quartz.job.QuartzExecutionHistoryCleanupJob");
            assertThat(sql).containsIgnoringCase("com.devops00.spectra.core.quartz.job.QuartzExecutionHistoryCleanupJob");
        }
    }
}
