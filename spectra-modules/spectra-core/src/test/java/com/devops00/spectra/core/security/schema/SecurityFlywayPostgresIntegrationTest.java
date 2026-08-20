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

package com.devops00.spectra.core.security.schema;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 真实 PostgreSQL 上的目标 schema/Flyway 门禁。
 * <p>
 * 该测试默认禁用，不会连接或修改开发机数据库。只有显式设置
 * {@code SPECTRA_SECURITY_FLYWAY_POSTGRES_TEST=true} 后才会执行；连接信息
 * 通过专用测试环境变量提供，并且必须使用可丢弃的隔离数据库。当前 V1 基线会创建数据库角色，
 * 测试只验证当前单一 V1 基线，不会创建额外数据库角色。
 */
@EnabledIfEnvironmentVariable(named = "SPECTRA_SECURITY_FLYWAY_POSTGRES_TEST", matches = "true")
class SecurityFlywayPostgresIntegrationTest {

    private static final String MIGRATION_LOCATION = "classpath:db/migration";

    @Test
    void shouldMigrateEmptyTargetDatabaseFromV1() throws SQLException {
        DatabaseConfig database = DatabaseConfig.from("SPECTRA_SECURITY_FLYWAY_DB_");
        Flyway.configure()
                .dataSource(database.url(), database.username(), database.password())
                .locations(MIGRATION_LOCATION)
                .baselineOnMigrate(false)
                .validateOnMigrate(true)
                .cleanDisabled(true)
                .load()
                .migrate();

        try (Connection connection = database.open()) {
            List<String> versions = new ArrayList<>();
            try (var statement = connection.createStatement();
                    var resultSet = statement.executeQuery(
                            "SELECT version FROM flyway_schema_history WHERE success = TRUE ORDER BY installed_rank")) {
                while (resultSet.next()) {
                    versions.add(resultSet.getString(1));
                }
            }

            assertEquals(List.of("1"), versions);
            assertTrue(tableExists(connection, "spectra_security", "sec_security_audit_event"));
            assertTrue(tableExists(connection, "spectra_security", "sec_security_audit_archive_manifest"));
            assertTrue(tableExists(connection, "spectra_security", "sec_assignment_permission_boundary"));
            assertTrue(tableExists(connection, "spectra_security", "sec_security_client"));
            assertTrue(tableExists(connection, "spectra_security", "sec_session_policy"));
            assertTrue(tableExists(connection, "spectra_security", "sec_password_policy"));
            assertFalse(tableExists(connection, "spectra_security", "security_audit_event"));
            assertFalse(tableExists(connection, "spectra_security", "permission"));
            assertTrue(constraintExists(connection, "spectra_security", "sec_permission", "pk_sec_permission"));
            assertTrue(constraintExists(
                    connection, "spectra_security", "sec_role_permission", "fk_sec_role_permission_role_id"));
            assertTrue(indexExists(connection, "spectra_security", "idx_sec_role_assignment_user_state"));
            assertTrue(indexExists(connection, "spectra_security", "uk_sec_scope_rule_department"));
            assertTrue(columnExists(connection, "spectra_core", "sys_log", "type"));
            assertTrue(columnExists(connection, "spectra_core", "sys_log", "explain"));
            assertTrue(columnExists(connection, "spectra_core", "sys_log", "status"));
            assertTrue(columnExists(connection, "spectra_core", "sys_log", "method"));
            assertTrue(columnExists(connection, "spectra_core", "sys_log", "url"));
            assertTrue(columnExists(connection, "spectra_core", "sys_log", "args"));
            assertTrue(columnExists(connection, "spectra_core", "sys_log", "result"));
            assertTrue(columnExists(connection, "spectra_core", "sys_log", "time_cost"));
            assertEquals("text", columnType(connection, "spectra_core", "sys_log", "explain"));
            assertFalse(tableExists(connection, "spectra_core", "sys_account"));
            assertFalse(tableExists(connection, "spectra_core", "sys_user_data_scope"));
            assertFalse(tableExists(connection, "spectra_core", "sys_user_data_scope_target"));
            assertFalse(tableExists(connection, "spectra_core", "sys_role_data_scope"));
            assertFalse(tableExists(connection, "spectra_core", "sys_role_data_scope_target"));
            assertFalse(tableExists(connection, "spectra_core", "sys_rel_user_role"));
            assertFalse(tableExists(connection, "spectra_core", "sys_rel_role_authority"));
            assertFalse(tableExists(connection, "spectra_core", "sys_rel_role_menu"));
            assertFalse(tableExists(connection, "spectra_core", "sys_authority"));
            assertFalse(tableExists(connection, "spectra_core", "sys_role"));
            assertFalse(tableExists(connection, "spectra_core", "sys_notification"));
            assertFalse(tableExists(connection, "spectra_core", "sys_notification_setting"));
            try (var statement = connection.createStatement();
                    var resultSet = statement.executeQuery("SELECT COUNT(*) FROM spectra_security.sec_permission")) {
                resultSet.next();
                assertEquals(115, resultSet.getInt(1));
            }
            try (var statement = connection.createStatement();
                    var resultSet = statement.executeQuery(
                            "SELECT COUNT(*) FROM spectra_security.sec_security_client WHERE state = 'ACTIVE'")) {
                resultSet.next();
                assertEquals(3, resultSet.getInt(1));
            }
            try (var statement = connection.createStatement();
                    var resultSet = statement.executeQuery("SELECT COUNT(*) FROM spectra_security.sec_session_policy")) {
                resultSet.next();
                assertEquals(3, resultSet.getInt(1));
            }
            try (var statement = connection.createStatement();
                    var resultSet = statement.executeQuery(
                            "SELECT min_length FROM spectra_security.sec_password_policy WHERE policy_key = 'SYSTEM'")) {
                assertTrue(resultSet.next());
                assertEquals(12, resultSet.getInt(1));
            }
        }
    }

    @Test
    void shouldRejectNonEmptyDatabaseWithoutBaseline() throws SQLException {
        DatabaseConfig database = DatabaseConfig.from("SPECTRA_SECURITY_FLYWAY_NONEMPTY_DB_");
        String marker = "security_flyway_marker_" + UUID.randomUUID().toString().replace("-", "");

        try (Connection connection = database.open(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE public." + marker + " (id integer NOT NULL)");
        }

        try {
            FlywayException failure = assertThrows(
                    FlywayException.class,
                    () -> Flyway.configure()
                            .dataSource(database.url(), database.username(), database.password())
                            .locations(MIGRATION_LOCATION)
                            .baselineOnMigrate(false)
                            .validateOnMigrate(true)
                            .cleanDisabled(true)
                            .load()
                            .migrate());
            assertTrue(failure.getMessage().contains("non-empty")
                    || failure.getMessage().contains("nonempty")
                    || failure.getMessage().contains("baseline"));
        } finally {
            try (Connection connection = database.open(); Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS public." + marker);
            }
        }
    }

    private static boolean tableExists(Connection connection, String schema, String table) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = ? AND table_name = ?)")) {
            statement.setString(1, schema);
            statement.setString(2, table);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBoolean(1);
            }
        }
    }

    private static boolean columnExists(Connection connection, String schema, String table, String column)
            throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM information_schema.columns "
                        + "WHERE table_schema = ? AND table_name = ? AND column_name = ?)")) {
            statement.setString(1, schema);
            statement.setString(2, table);
            statement.setString(3, column);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBoolean(1);
            }
        }
    }

    private static boolean constraintExists(Connection connection, String schema, String table, String constraint)
            throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM pg_constraint constraint_row "
                        + "JOIN pg_class table_row ON table_row.oid = constraint_row.conrelid "
                        + "JOIN pg_namespace schema_row ON schema_row.oid = table_row.relnamespace "
                        + "WHERE schema_row.nspname = ? AND table_row.relname = ? "
                        + "AND constraint_row.conname = ?)")) {
            statement.setString(1, schema);
            statement.setString(2, table);
            statement.setString(3, constraint);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBoolean(1);
            }
        }
    }

    private static boolean indexExists(Connection connection, String schema, String index) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM pg_class index_row "
                        + "JOIN pg_namespace schema_row ON schema_row.oid = index_row.relnamespace "
                        + "WHERE schema_row.nspname = ? AND index_row.relname = ? AND index_row.relkind = 'i')")) {
            statement.setString(1, schema);
            statement.setString(2, index);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBoolean(1);
            }
        }
    }

    private static String columnType(Connection connection, String schema, String table, String column)
            throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT data_type FROM information_schema.columns "
                        + "WHERE table_schema = ? AND table_name = ? AND column_name = ?")) {
            statement.setString(1, schema);
            statement.setString(2, table);
            statement.setString(3, column);
            try (var resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getString(1);
            }
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
