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
 * 通过专用测试环境变量提供，并且必须使用可丢弃的隔离数据库。V2 会创建数据库角色，
 * 因此测试账号需要具备 CREATEROLE 权限。
 */
@EnabledIfEnvironmentVariable(named = "SPECTRA_SECURITY_FLYWAY_POSTGRES_TEST", matches = "true")
class SecurityFlywayPostgresIntegrationTest {

    private static final String MIGRATION_LOCATION = "classpath:db/migration";

    @Test
    void shouldMigrateEmptyTargetDatabaseFromV1AndV2() throws SQLException {
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

            assertEquals(List.of("1", "2"), versions);
            assertTrue(tableExists(connection, "spectra_security", "security_audit_event"));
            assertTrue(tableExists(connection, "spectra_security", "assignment_permission_boundary"));
            assertFalse(tableExists(connection, "spectra_core", "sys_account"));
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
