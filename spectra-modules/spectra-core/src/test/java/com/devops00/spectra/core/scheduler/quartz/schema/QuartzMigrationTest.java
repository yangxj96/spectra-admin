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

package com.devops00.spectra.core.scheduler.quartz.schema;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** 使用隔离 PostgreSQL 验证完整 Flyway 链最终 schema。 */
@Testcontainers
@Tag("manual-integration")
@EnabledIfEnvironmentVariable(named = "SPECTRA_QUARTZ_FLYWAY_POSTGRES_TEST", matches = "true")
class QuartzMigrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("spectra_quartz_test")
            .withUsername("quartz_test")
            .withPassword("quartz_test");

    private static final List<String> QUARTZ_TABLES = List.of(
            "qrtz_job_details",
            "qrtz_triggers",
            "qrtz_simple_triggers",
            "qrtz_cron_triggers",
            "qrtz_simprop_triggers",
            "qrtz_blob_triggers",
            "qrtz_calendars",
            "qrtz_paused_trigger_grps",
            "qrtz_fired_triggers",
            "qrtz_scheduler_state",
            "qrtz_locks");

    private static final List<String> LEGACY_TABLES = List.of(
            "scheduler_operation_audit",
            "scheduler_loop_error",
            "scheduler_control_command",
            "scheduler_loop_runtime",
            "scheduler_execution",
            "scheduler_job");

    @Test
    void fullMigrationMustLeaveQuartzAndHistoryButNoLegacyTables() throws SQLException {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .baselineOnMigrate(false)
                .validateOnMigrate(true)
                .cleanDisabled(true)
                .load()
                .migrate();

        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
            assertThat(schemaExists(connection, "spectra_quartz")).isTrue();
            for (var table : QUARTZ_TABLES) {
                assertThat(tableExists(connection, "spectra_quartz", table)).as(table).isTrue();
            }
            assertThat(tableExists(connection, "spectra_core", "quartz_job_execution_history")).isTrue();
            assertHistoryColumnsFollowDatabaseStandard(connection);
            assertThat(indexExists(connection, "idx_quartz_history_job_started")).isTrue();
            assertThat(indexExists(connection, "idx_quartz_history_trigger_started")).isTrue();
            assertThat(indexExists(connection, "idx_quartz_history_status_started")).isTrue();
            assertThat(indexExists(connection, "idx_quartz_history_created_at")).isTrue();
            for (var table : LEGACY_TABLES) {
                assertThat(tableExists(connection, "spectra_core", table)).as(table).isFalse();
            }
        }
    }

    private void assertHistoryColumnsFollowDatabaseStandard(Connection connection) throws SQLException {
        var expectedColumns = List.of("id", "fire_instance_id", "job_key", "trigger_key", "job_type",
                "job_class_name", "trigger_type", "status", "scheduled_fire_at", "actual_fire_at", "started_at",
                "finished_at", "duration_ms", "scheduler_instance", "correlation_id", "parameter_version",
                "parameter_sha256", "result_summary", "error_code", "error_message", "created_by", "created_at",
                "updated_by", "updated_at", "deleted", "version");
        var actualColumns = new ArrayList<String>();
        var metadata = new LinkedHashMap<String, ColumnMetadata>();
        try (var statement = connection.prepareStatement(
                "SELECT column_name, data_type, datetime_precision, is_nullable, column_default "
                        + "FROM information_schema.columns "
                        + "WHERE table_schema = 'spectra_core' AND table_name = 'quartz_job_execution_history' "
                        + "ORDER BY ordinal_position")) {
            try (var result = statement.executeQuery()) {
                while (result.next()) {
                    var name = result.getString("column_name");
                    actualColumns.add(name);
                    metadata.put(name, new ColumnMetadata(result.getString("data_type"),
                            result.getObject("datetime_precision", Integer.class), result.getString("is_nullable"),
                            result.getString("column_default")));
                }
            }
        }
        assertThat(actualColumns).containsExactlyElementsOf(expectedColumns);

        assertColumn(metadata, "id", "uuid", null, "NO", "uuidv7()");
        assertColumn(metadata, "created_by", "uuid", null, "YES", null);
        assertColumn(metadata, "created_at", "timestamp with time zone", 6, "NO", "CURRENT_TIMESTAMP");
        assertColumn(metadata, "updated_by", "uuid", null, "YES", null);
        assertColumn(metadata, "updated_at", "timestamp with time zone", 6, "NO", "CURRENT_TIMESTAMP");
        assertColumn(metadata, "deleted", "timestamp with time zone", 6, "YES", null);
        assertColumn(metadata, "version", "bigint", null, "NO", "0");
    }

    private void assertColumn(Map<String, ColumnMetadata> metadata, String name, String type, Integer precision,
                              String nullable, String defaultFragment) {
        var actual = metadata.get(name);
        assertThat(actual).as("缺少历史表字段: %s", name).isNotNull();
        assertThat(actual.dataType()).as(name).isEqualTo(type);
        assertThat(actual.datetimePrecision()).as(name).isEqualTo(precision);
        assertThat(actual.nullable()).as(name).isEqualTo(nullable);
        if (defaultFragment != null) {
            assertThat(actual.defaultValue()).as(name).containsIgnoringCase(defaultFragment);
        } else {
            assertThat(actual.defaultValue()).as(name).isNull();
        }
    }

    private record ColumnMetadata(String dataType, Integer datetimePrecision, String nullable, String defaultValue) {
    }

    private boolean schemaExists(Connection connection, String schema) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = ?)")) {
            statement.setString(1, schema);
            try (var result = statement.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        }
    }

    private boolean tableExists(Connection connection, String schema, String table) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM information_schema.tables "
                        + "WHERE table_schema = ? AND table_name = ?)")) {
            statement.setString(1, schema);
            statement.setString(2, table);
            try (var result = statement.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        }
    }

    private boolean indexExists(Connection connection, String index) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM pg_class index_row "
                        + "JOIN pg_namespace schema_row ON schema_row.oid = index_row.relnamespace "
                        + "WHERE schema_row.nspname = 'spectra_core' AND index_row.relname = ?)")) {
            statement.setString(1, index);
            try (var result = statement.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        }
    }
}
