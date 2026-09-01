/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.schema;

import com.devops00.spectra.core.security.audit.outbox.SecurityChangeOutboxRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 安全变更 outbox 的真实 PostgreSQL 契约测试。
 *
 * <p>该测试只在 Maven 的 {@code integration} profile 中执行，使用 Testcontainers 创建可丢弃的
 * PostgreSQL 数据库。测试只验证 Flyway 迁移、事务回滚和幂等唯一约束；生产数据不得作为测试库。</p>
 */
@Tag("integration")
@Testcontainers
class OutboxPostgresIntegrationTest {

    private static final String MIGRATION_LOCATION = "classpath:db/migration";
    private static final String OUTBOX_TABLE = "spectra_security.sec_security_change_outbox";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("spectra_security_outbox")
            .withUsername("postgres")
            .withPassword("integration");

    @Test
    void migrationMustExposeLeaseAndIdempotencyContract() throws SQLException {
        DatabaseConfig database = migrate();
        try (Connection connection = database.open()) {
            assertTrue(columnExists(connection, "spectra_security", "sec_security_change_outbox", "idempotency_key"));
            assertTrue(columnExists(connection, "spectra_security", "sec_security_change_outbox", "state"));
            assertTrue(columnExists(connection, "spectra_security", "sec_security_change_outbox", "available_at"));
            assertTrue(columnExists(connection, "spectra_security", "sec_security_change_outbox", "lease_owner"));
            assertTrue(columnExists(connection, "spectra_security", "sec_security_change_outbox", "lease_until"));
            assertTrue(indexExists(connection, "spectra_security", "uk_sec_security_change_outbox_idempotency_key"));
            assertTrue(indexExists(connection, "spectra_security", "idx_sec_security_change_outbox_pending"));
            assertTrue(indexExists(connection, "spectra_security", "idx_sec_security_change_outbox_lease"));
            assertTrue(columnExists(connection, "spectra_security", "sec_security_audit_archive_manifest", "content_length"));
            assertTrue(columnExists(connection, "spectra_security", "sec_security_audit_archive_manifest", "lease_until"));
        }
    }

    @Test
    void transactionRollbackMustLeaveNoSecurityOutboxEvent() throws SQLException {
        DatabaseConfig database = migrate();
        UUID eventId = UUID.randomUUID();
        String idempotencyKey = "rollback-" + UUID.randomUUID();
        try (Connection connection = database.open()) {
            connection.setAutoCommit(false);
            insert(connection, eventId, idempotencyKey, "{\"event\":\"rollback\"}");
            connection.rollback();
            assertEquals(0, countByEventId(connection, eventId));
        }
    }

    @Test
    void duplicateIdempotencyKeyMustBeAcceptedOnlyOnce() throws SQLException {
        DatabaseConfig database = migrate();
        String idempotencyKey = "duplicate-" + UUID.randomUUID();
        try (Connection connection = database.open()) {
            connection.setAutoCommit(false);
            assertEquals(1, insert(connection, UUID.randomUUID(), idempotencyKey, "{\"event\":\"one\"}"));
            assertEquals(0, insert(connection, UUID.randomUUID(), idempotencyKey, "{\"event\":\"same\"}"));
            assertEquals(1, countByIdempotencyKey(connection, idempotencyKey));
            connection.rollback();
        }
    }

    @Test
    void replacementWorkerCanTakeOverExpiredLeaseAndFenceCrashedOwner() throws SQLException {
        DatabaseConfig database = migrate();
        SecurityChangeOutboxRepository repository = repository(database);
        UUID eventId = UUID.randomUUID();
        String idempotencyKey = "lease-recovery-" + UUID.randomUUID();
        Instant now = Instant.now();
        try {
            assertEquals(1, repository.enqueue(eventId, idempotencyKey, "USER_CREATED", "USER", null,
                    "{\"event\":\"lease-recovery\"}", "correlation-lease", now, null));

            var firstClaim = repository.claimBatch("worker-a", now, now.plusSeconds(30), 10, 10);
            assertEquals(1, firstClaim.size());
            assertEquals(eventId, firstClaim.getFirst().eventId());
            assertEquals(1, firstClaim.getFirst().attempts());

            Instant afterRestart = now.plusSeconds(31);
            var replacementClaim = repository.claimBatch("worker-b", afterRestart, afterRestart.plusSeconds(30), 10, 10);
            assertEquals(1, replacementClaim.size());
            assertEquals(eventId, replacementClaim.getFirst().eventId());
            assertEquals(2, replacementClaim.getFirst().attempts());
            assertEquals(0, repository.markProcessed(eventId, "worker-a", afterRestart));
            assertEquals(1, repository.markProcessed(eventId, "worker-b", afterRestart));
        } finally {
            try (var connection = database.open();
                    var statement = connection.prepareStatement(
                            "DELETE FROM " + OUTBOX_TABLE + " WHERE id = ?")) {
                statement.setObject(1, eventId);
                statement.executeUpdate();
            }
        }
    }

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

    private static SecurityChangeOutboxRepository repository(DatabaseConfig database) {
        var dataSource = new DriverManagerDataSource(database.url(), database.username(), database.password());
        return new SecurityChangeOutboxRepository(new JdbcTemplate(dataSource));
    }

    private static int insert(Connection connection, UUID eventId, String idempotencyKey, String payload)
            throws SQLException {
        String sql = "INSERT INTO " + OUTBOX_TABLE
                + " (id, idempotency_key, event_type, aggregate_type, aggregate_id, payload, state, available_at,"
                + " attempts, created_at, updated_at, version)"
                + " VALUES (?, ?, 'USER_CREATED', 'USER', NULL, ?::jsonb, 'PENDING', CURRENT_TIMESTAMP,"
                + " 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)"
                + " ON CONFLICT (idempotency_key) DO NOTHING";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, eventId);
            statement.setString(2, idempotencyKey);
            statement.setString(3, payload);
            return statement.executeUpdate();
        }
    }

    private static int countByEventId(Connection connection, UUID eventId) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM " + OUTBOX_TABLE + " WHERE id = ?")) {
            statement.setObject(1, eventId);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private static int countByIdempotencyKey(Connection connection, String idempotencyKey) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM " + OUTBOX_TABLE + " WHERE idempotency_key = ?")) {
            statement.setString(1, idempotencyKey);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
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

    private record DatabaseConfig(String url, String username, String password) {

        private static DatabaseConfig from(PostgreSQLContainer<?> container) {
            return new DatabaseConfig(container.getJdbcUrl(), container.getUsername(), container.getPassword());
        }

        private Connection open() throws SQLException {
            return DriverManager.getConnection(url, username, password);
        }
    }

}
