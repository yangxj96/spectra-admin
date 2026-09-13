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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 统一审计表的真实 PostgreSQL 契约测试。
 *
 * <p>只使用 Testcontainers 创建的临时数据库，覆盖默认分区路由、事务回滚和不可变约束。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Tag("integration")
@Testcontainers
class UnifiedAuditSchemaPostgresIntegrationTest {

    private static final String MIGRATION_LOCATION = "classpath:db/migration";
    private static final String TABLE = "spectra_core.sys_audit_event";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("spectra_unified_audit")
            .withUsername("postgres")
            .withPassword("integration");

    @Test
    void rollbackMustRemoveAuditRowAndCommitMustRouteToDefaultPartition() throws SQLException {
        DatabaseConfig database = migrate();
        UUID rolledBackEvent = UUID.randomUUID();
        UUID committedEvent = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-09-13T01:00:00Z");

        try (Connection connection = database.open()) {
            connection.setAutoCommit(false);
            insert(connection, rolledBackEvent, occurredAt, "OPERATION", "TEST.ROLLBACK");
            connection.rollback();
            assertEquals(0, countByEventId(connection, rolledBackEvent));

            insert(connection, committedEvent, occurredAt, "SECURITY", "TEST.COMMIT");
            connection.commit();
            assertEquals(1, countByEventId(connection, committedEvent));
            assertEquals("spectra_core.sys_audit_event_default", partitionFor(connection, committedEvent));
        }
    }

    @Test
    void auditRowsMustRejectUpdateAndDelete() throws SQLException {
        DatabaseConfig database = migrate();
        UUID eventId = UUID.randomUUID();
        Instant occurredAt = Instant.now();
        try (Connection connection = database.open()) {
            insert(connection, eventId, occurredAt, "SECURITY", "TEST.IMMUTABLE");
            connection.commit();

            assertThrows(SQLException.class, () -> mutate(connection, eventId, occurredAt));
            connection.rollback();
            assertTrue(rowExists(connection, eventId, occurredAt));
            assertThrows(SQLException.class, () -> delete(connection, eventId, occurredAt));
            connection.rollback();
            assertTrue(rowExists(connection, eventId, occurredAt));
        }
    }

    @Test
    void operationAndSecurityRowsMustShareTheSameRelation() throws SQLException {
        DatabaseConfig database = migrate();
        UUID operationId = UUID.randomUUID();
        UUID securityId = UUID.randomUUID();
        Instant occurredAt = Instant.now();
        try (Connection connection = database.open()) {
            insert(connection, operationId, occurredAt, "OPERATION", "TEST.OPERATION");
            insert(connection, securityId, occurredAt, "SECURITY", "TEST.SECURITY");
            connection.commit();

            assertEquals(1, countByCategoryAndEventId(connection, "OPERATION", operationId));
            assertEquals(1, countByCategoryAndEventId(connection, "SECURITY", securityId));
            assertFalse(tableExists(connection, "spectra_security", "sec_security_audit_event"));
            assertFalse(tableExists(connection, "spectra_core", "sys_log"));
            assertFalse(tableExists(connection, "spectra_core", "sys_operation_log_outbox"));
        }
    }

    /**
     * 处理审计相关数据。
     */
    private static DatabaseConfig migrate() {
        DatabaseConfig database = DatabaseConfig.from(POSTGRES);
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

    /**
     * 保存审计。
     */
    private static void insert(Connection connection, UUID eventId, Instant occurredAt, String category,
                               String eventType)
            throws SQLException {
        String sql = "INSERT INTO " + TABLE
                + " (event_id, occurred_at, category, event_type, result, before_snapshot, after_snapshot)"
                + " VALUES (?, ?, ?, ?, 'SUCCEEDED', '{}'::jsonb, '{}'::jsonb)";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, eventId);
            statement.setObject(2, occurredAt);
            statement.setString(3, category);
            statement.setString(4, eventType);
            statement.executeUpdate();
        }
    }

    /**
     * 统计事件标识数量。
     */
    private static int countByEventId(Connection connection, UUID eventId) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT COUNT(*) FROM " + TABLE + " WHERE event_id = ?")) {
            statement.setObject(1, eventId);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    /**
     * 统计分类事件标识数量。
     */
    private static int countByCategoryAndEventId(Connection connection, String category, UUID eventId)
            throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM " + TABLE + " WHERE category = ? AND event_id = ?")) {
            statement.setString(1, category);
            statement.setObject(2, eventId);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    /**
     * 处理审计相关数据。
     */
    private static String partitionFor(Connection connection, UUID eventId) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT tableoid::regclass::text FROM " + TABLE + " WHERE event_id = ?")) {
            statement.setObject(1, eventId);
            try (var resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getString(1);
            }
        }
    }

    /**
     * 处理审计相关数据。
     */
    private static void mutate(Connection connection, UUID eventId, Instant occurredAt) throws SQLException {
        try (var statement = connection.prepareStatement(
                "UPDATE " + TABLE + " SET result = 'FAILED' WHERE event_id = ? AND occurred_at = ?")) {
            statement.setObject(1, eventId);
            statement.setObject(2, occurredAt);
            statement.executeUpdate();
        }
    }

    /**
     * 删除或清理审计。
     */
    private static void delete(Connection connection, UUID eventId, Instant occurredAt) throws SQLException {
        try (var statement = connection.prepareStatement(
                "DELETE FROM " + TABLE + " WHERE event_id = ? AND occurred_at = ?")) {
            statement.setObject(1, eventId);
            statement.setObject(2, occurredAt);
            statement.executeUpdate();
        }
    }

    /**
     * 处理行数据相关数据。
     */
    private static boolean rowExists(Connection connection, UUID eventId, Instant occurredAt) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM " + TABLE + " WHERE event_id = ? AND occurred_at = ?)")) {
            statement.setObject(1, eventId);
            statement.setObject(2, occurredAt);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBoolean(1);
            }
        }
    }

    /**
     * 处理审计相关数据。
     */
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

    /**
     * 为 {@code UnifiedAuditSchemaPostgresIntegrationTest} 测试提供 {@code DatabaseConfig} 测试类型。
     *
     * @param url      HTTP 请求地址
     * @param username 用户登录名
     * @param password 用于连接测试数据库的密码
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    private record DatabaseConfig(String url, String username, String password) {

        /**
         * 处理配置相关数据。
         */
        private static DatabaseConfig from(PostgreSQLContainer<?> container) {
            return new DatabaseConfig(container.getJdbcUrl(), container.getUsername(), container.getPassword());
        }

        /**
         * 打开配置。
         */
        private Connection open() throws SQLException {
            Connection connection = DriverManager.getConnection(url, username, password);
            connection.setAutoCommit(false);
            return connection;
        }
    }
}
