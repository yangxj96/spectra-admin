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

package com.devops00.spectra.core.scheduler;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 调度 V4/V5/V6 迁移的真实 PostgreSQL 契约测试。
 * <p>
 * 测试默认禁用，只有显式提供隔离数据库连接并设置
 * {@code SPECTRA_SCHEDULER_FLYWAY_POSTGRES_TEST=true} 后才会执行。
 */
@EnabledIfEnvironmentVariable(named = "SPECTRA_SCHEDULER_FLYWAY_POSTGRES_TEST", matches = "true")
class SchedulerMigrationTest {

    private static final String MIGRATION_LOCATION = "classpath:db/migration";
    private static final List<String> TABLES = List.of(
            "scheduler_job",
            "scheduler_execution",
            "scheduler_loop_runtime",
            "scheduler_control_command",
            "scheduler_loop_error",
            "scheduler_operation_audit");
    private static final List<String> BASE_COLUMNS = List.of(
            "deleted",
            "created_by",
            "created_at",
            "updated_by",
            "updated_at",
            "version");

    @Test
    void shouldCreateSchedulerTablesAndSeedJobs() throws SQLException {
        DatabaseConfig database = migrate();

        try (Connection connection = database.open()) {
            assertEquals(List.of("1", "2", "3", "4", "5", "6"), migrationVersions(connection));
            for (String table : TABLES) {
                assertTrue(tableExists(connection, table), "missing table: " + table);
                for (String column : BASE_COLUMNS) {
                    assertTrue(columnExists(connection, table, column), table + "." + column);
                }
            }

            List<String> jobKeys = new ArrayList<>();
            try (var statement = connection.createStatement();
                    var resultSet = statement.executeQuery(
                            "SELECT job_key FROM spectra_core.scheduler_job ORDER BY job_key")) {
                while (resultSet.next()) {
                    jobKeys.add(resultSet.getString(1));
                }
            }
            assertEquals(List.of(
                    "notification.cleanup-sensitive-payload",
                    "notification.task-worker",
                    "oa.contract.milestone-reminder",
                    "system.monitor.collect-snapshot",
                    "system.monitor.diagnostic-cleanup"), jobKeys);
            assertEquals("timestamp with time zone", columnType(connection, "scheduler_job", "deleted"));
            assertEquals("jsonb", columnType(connection, "scheduler_job", "execution_policy"));
            assertTrue(indexExists(connection, "uk_scheduler_job_job_key"));
            assertTrue(indexExists(connection, "uk_scheduler_execution_fire_key"));
            assertTrue(indexExists(connection, "uk_scheduler_control_command_idempotency_key"));
            assertTrue(indexExists(connection, "uk_scheduler_loop_runtime_session_key"));
            assertTrue(indexExists(connection, "uk_scheduler_operation_audit_idempotency_key"));
        }
    }

    @Test
    void shouldCommentEverySchedulerColumn() throws SQLException {
        DatabaseConfig database = migrate();

        try (Connection connection = database.open();
                var statement = connection.prepareStatement(
                        "SELECT COUNT(*) "
                                + "FROM information_schema.columns column_row "
                                + "JOIN pg_namespace schema_row ON schema_row.nspname = column_row.table_schema "
                                + "JOIN pg_class table_row ON table_row.relnamespace = schema_row.oid "
                                + "AND table_row.relname = column_row.table_name "
                                + "JOIN pg_attribute attribute_row ON attribute_row.attrelid = table_row.oid "
                                + "AND attribute_row.attname = column_row.column_name "
                                + "LEFT JOIN pg_description description_row ON description_row.objoid = table_row.oid "
                                + "AND description_row.objsubid = attribute_row.attnum "
                                + "WHERE column_row.table_schema = 'spectra_core' "
                                + "AND column_row.table_name IN ("
                                + "'scheduler_job', 'scheduler_execution', 'scheduler_loop_runtime', "
                                + "'scheduler_control_command', 'scheduler_loop_error', 'scheduler_operation_audit') "
                                + "AND description_row.description IS NULL")) {
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                assertEquals(0, resultSet.getInt(1), "调度表存在未注释字段");
            }
        }
    }

    @Test
    void shouldEnforceArchivedAndIdempotencyContracts() throws SQLException {
        DatabaseConfig database = migrate();
        UUID executionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID commandId = UUID.randomUUID();
        String jobKey = "test.scheduler.uniqueness." + jobId;

        try (Connection connection = database.open(); Statement statement = connection.createStatement()) {
            try {
                statement.executeUpdate(
                        "UPDATE spectra_core.scheduler_job SET definition_status = 'ARCHIVED' "
                                + "WHERE job_key = 'oa.contract.milestone-reminder'");
                try (var resultSet = statement.executeQuery(
                        "SELECT COUNT(*) FROM spectra_core.scheduler_job "
                                + "WHERE definition_status <> 'ARCHIVED' "
                                + "AND job_key = 'oa.contract.milestone-reminder'")) {
                    resultSet.next();
                    assertEquals(0, resultSet.getInt(1));
                }

                statement.executeUpdate("INSERT INTO spectra_core.scheduler_job "
                        + "(id, job_key, name, module, handler_key, job_type, run_scope, definition_status, "
                        + "desired_state, schedule_kind, fixed_delay_ms, execution_policy, parameters, revision, "
                        + "created_at, updated_at, version) VALUES "
                        + "('" + jobId + "', '" + jobKey + "', 'test', 'test', 'test', 'SYSTEM', "
                        + "'SINGLETON', 'REGISTERED', 'DISABLED', 'MANUAL', NULL, '{}'::jsonb, '{}'::jsonb, 1, now(), now(), 0)");
                statement.executeUpdate("INSERT INTO spectra_core.scheduler_execution "
                        + "(id, job_id, fire_key, trigger_type, status, job_revision, handler_version, "
                        + "schedule_kind_snapshot, effect_type, scheduled_at, queued_at, attempt_no, max_attempts, "
                        + "resolution_status, created_at, updated_at, version) VALUES "
                        + "('" + executionId + "', '" + jobId + "', 'test-fire-key-" + jobId + "', 'MANUAL', 'QUEUED', "
                        + "1, '1.0.0', 'MANUAL', 'DB_ONLY', now(), now(), 1, 1, 'UNRESOLVED', now(), now(), 0)");
                assertTrue(duplicateFails(statement,
                        "INSERT INTO spectra_core.scheduler_execution "
                                + "(id, job_id, fire_key, trigger_type, status, job_revision, handler_version, "
                                + "schedule_kind_snapshot, effect_type, scheduled_at, queued_at, attempt_no, max_attempts, "
                                + "resolution_status, created_at, updated_at, version) VALUES "
                                + "('" + UUID.randomUUID() + "', '" + jobId + "', 'test-fire-key-" + jobId
                                + "', 'MANUAL', 'QUEUED', 1, '1.0.0', 'MANUAL', 'DB_ONLY', now(), now(), 1, 1, "
                                + "'UNRESOLVED', now(), now(), 0)"));
                statement.executeUpdate("INSERT INTO spectra_core.scheduler_control_command "
                        + "(id, job_id, command_type, status, idempotency_key, reason, requested_at, created_at, "
                        + "updated_at, version) VALUES ('" + commandId + "', '" + jobId + "', 'START', 'REQUESTED', "
                        + "'test-idempotency-" + jobId + "', 'migration contract test', now(), now(), now(), 0)");
                assertTrue(duplicateFails(statement,
                        "INSERT INTO spectra_core.scheduler_control_command "
                                + "(id, job_id, command_type, status, idempotency_key, reason, requested_at, created_at, "
                                + "updated_at, version) VALUES ('" + UUID.randomUUID() + "', '" + jobId + "', 'START', "
                                + "'REQUESTED', 'test-idempotency-" + jobId + "', 'migration contract test', now(), "
                                + "now(), now(), 0)"));
            } finally {
                statement.executeUpdate("DELETE FROM spectra_core.scheduler_control_command WHERE id = '" + commandId + "'");
                statement.executeUpdate("DELETE FROM spectra_core.scheduler_execution WHERE id = '" + executionId + "'");
                statement.executeUpdate("DELETE FROM spectra_core.scheduler_job WHERE id = '" + jobId + "'");
                statement.executeUpdate(
                        "UPDATE spectra_core.scheduler_job SET definition_status = 'REGISTERED' "
                                + "WHERE job_key = 'oa.contract.milestone-reminder'");
            }
        }
    }

    private static DatabaseConfig migrate() {
        DatabaseConfig database = DatabaseConfig.from("SPECTRA_SCHEDULER_FLYWAY_DB_");
        Flyway.configure()
                .dataSource(database.url(), database.username(), database.password())
                .locations(MIGRATION_LOCATION)
                .baselineOnMigrate(false)
                .validateOnMigrate(true)
                .cleanDisabled(true)
                .load()
                .migrate();
        return database;
    }

    private static List<String> migrationVersions(Connection connection) throws SQLException {
        List<String> versions = new ArrayList<>();
        try (var statement = connection.createStatement();
                var resultSet = statement.executeQuery(
                        "SELECT version FROM flyway_schema_history WHERE success = TRUE ORDER BY installed_rank")) {
            while (resultSet.next()) {
                versions.add(resultSet.getString(1));
            }
        }
        return versions;
    }

    private static boolean tableExists(Connection connection, String table) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM information_schema.tables "
                        + "WHERE table_schema = 'spectra_core' AND table_name = ?)")) {
            statement.setString(1, table);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBoolean(1);
            }
        }
    }

    private static boolean columnExists(Connection connection, String table, String column) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM information_schema.columns "
                        + "WHERE table_schema = 'spectra_core' AND table_name = ? AND column_name = ?)")) {
            statement.setString(1, table);
            statement.setString(2, column);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBoolean(1);
            }
        }
    }

    private static String columnType(Connection connection, String table, String column) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT data_type FROM information_schema.columns "
                        + "WHERE table_schema = 'spectra_core' AND table_name = ? AND column_name = ?")) {
            statement.setString(1, table);
            statement.setString(2, column);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getString(1);
            }
        }
    }

    private static boolean indexExists(Connection connection, String index) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM pg_class index_row "
                        + "JOIN pg_namespace schema_row ON schema_row.oid = index_row.relnamespace "
                        + "WHERE schema_row.nspname = 'spectra_core' AND index_row.relname = ? "
                        + "AND index_row.relkind = 'i')")) {
            statement.setString(1, index);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBoolean(1);
            }
        }
    }

    private static boolean duplicateFails(Statement statement, String sql) {
        try {
            statement.executeUpdate(sql);
            return false;
        } catch (SQLException expected) {
            return true;
        }
    }

    private record DatabaseConfig(String url, String username, String password) {

        private static DatabaseConfig from(String prefix) {
            String url = environment(prefix + "URL");
            String username = environment(prefix + "USERNAME");
            String password = environment(prefix + "PASSWORD");
            assumeTrue(!url.isBlank() && !username.isBlank(), "未提供专用 PostgreSQL 集成测试连接信息");
            return new DatabaseConfig(url, username, password);
        }

        private Connection open() throws SQLException {
            return DriverManager.getConnection(url, username, password);
        }
    }

    private static String environment(String name) {
        return System.getenv().getOrDefault(name, "").trim();
    }
}
