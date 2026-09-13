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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 在真实 PostgreSQL 上验证自建执行历史表的审计字段、默认值和状态约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Testcontainers
@Tag("manual-integration")
@EnabledIfEnvironmentVariable(named = "SPECTRA_QUARTZ_FLYWAY_POSTGRES_TEST", matches = "true")
class QuartzHistoryIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("spectra_quartz_history")
            .withUsername("quartz_test")
            .withPassword("quartz_test");

    @BeforeAll
    static void migrateSchema() {
        QuartzPostgresIntegrationSupport.migrate(POSTGRES);
    }

    @Test
    void historyMustUseUuidV7DefaultAndStandardAuditColumns() throws SQLException {
        var fireInstanceId = "history-" + UUID.randomUUID();
        var startedAt = Instant.now();
        UUID id;
        try (var connection = QuartzPostgresIntegrationSupport.connection(POSTGRES);
                var statement = connection.prepareStatement(
                        "INSERT INTO spectra_core.quartz_job_execution_history "
                                + "(fire_instance_id, job_key, trigger_key, job_type, job_class_name, trigger_type, "
                                + "status, actual_fire_at, started_at) "
                                + "VALUES (?, ?, ?, ?, ?, ?, 'RUNNING', ?, ?) RETURNING id, created_at, updated_at, version")) {
            statement.setString(1, fireInstanceId);
            statement.setString(2, "integration.job");
            statement.setString(3, "integration.trigger");
            statement.setString(4, "integration.job");
            statement.setString(5, getClass().getName());
            statement.setString(6, "SIMPLE");
            statement.setTimestamp(7, Timestamp.from(startedAt));
            statement.setTimestamp(8, Timestamp.from(startedAt));
            try (var result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                id = result.getObject("id", UUID.class);
                assertThat(id.version()).isEqualTo(7);
                assertThat(result.getTimestamp("created_at")).isNotNull();
                assertThat(result.getTimestamp("updated_at")).isNotNull();
                assertThat(result.getLong("version")).isZero();
            }
        }
        try (var connection = QuartzPostgresIntegrationSupport.connection(POSTGRES);
                var statement = connection.prepareStatement(
                        "SELECT status FROM spectra_core.quartz_job_execution_history WHERE id = ?")) {
            statement.setObject(1, id);
            try (var result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat(result.getString(1)).isEqualTo("RUNNING");
            }
        }
    }

    @Test
    void historyMustRejectUnknownStatus() {
        assertThatThrownBy(() -> {
            try (var connection = QuartzPostgresIntegrationSupport.connection(POSTGRES);
                    var statement = connection.prepareStatement(
                            "INSERT INTO spectra_core.quartz_job_execution_history "
                                    + "(fire_instance_id, job_key, trigger_key, job_type, job_class_name, trigger_type, "
                                    + "status, actual_fire_at, started_at) "
                                    + "VALUES (?, 'integration.job', 'integration.trigger', 'integration.job', ?, "
                                    + "'SIMPLE', 'UNKNOWN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
                statement.setString(1, "invalid-status-" + UUID.randomUUID());
                statement.setString(2, getClass().getName());
                statement.executeUpdate();
            }
        }).isInstanceOf(SQLException.class);
    }
}
