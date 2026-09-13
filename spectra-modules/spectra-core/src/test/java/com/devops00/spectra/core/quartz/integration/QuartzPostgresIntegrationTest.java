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

package com.devops00.spectra.core.quartz.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.quartz.Scheduler;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 在真实 PostgreSQL 上验证 Quartz JDBC Cluster 的最终迁移和 JobStore 配置。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Testcontainers
@Tag("manual-integration")
@EnabledIfEnvironmentVariable(named = "SPECTRA_QUARTZ_FLYWAY_POSTGRES_TEST", matches = "true")
class QuartzPostgresIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("spectra_quartz_postgres")
            .withUsername("quartz_test")
            .withPassword("quartz_test");

    @BeforeAll
    static void migrateSchema() {
        QuartzPostgresIntegrationSupport.migrate(POSTGRES);
    }

    @Test
    void schedulerMustUsePersistentClusteredJobStoreAndBothSchemas() throws Exception {
        Scheduler scheduler = QuartzPostgresIntegrationSupport.scheduler(POSTGRES, "postgres-node");
        try {
            var metadata = scheduler.getMetaData();
            assertThat(metadata.isJobStoreSupportsPersistence()).isTrue();
            assertThat(metadata.isJobStoreClustered()).isTrue();
            assertThat(metadata.getJobStoreClass().getName())
                    .isEqualTo("org.quartz.impl.jdbcjobstore.JobStoreTX");
            assertThat(metadata.getThreadPoolSize()).isEqualTo(2);
        } finally {
            scheduler.shutdown(true);
        }
        try (var connection = QuartzPostgresIntegrationSupport.connection(POSTGRES);
                var statement = connection.prepareStatement(
                        "SELECT EXISTS (SELECT 1 FROM information_schema.schemata "
                                + "WHERE schema_name = 'spectra_quartz'), "
                                + "EXISTS (SELECT 1 FROM information_schema.tables "
                                + "WHERE table_schema = 'spectra_core' "
                                + "AND table_name = 'quartz_job_execution_history')")) {
            try (var result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat(result.getBoolean(1)).isTrue();
                assertThat(result.getBoolean(2)).isTrue();
            }
        }
    }
}
