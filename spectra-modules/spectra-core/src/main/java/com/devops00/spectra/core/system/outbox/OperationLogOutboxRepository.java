/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.system.outbox;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** 普通操作日志 outbox 的 PostgreSQL 访问端口。 */
@Repository
public class OperationLogOutboxRepository {

    private static final String TABLE = "spectra_core.sys_operation_log_outbox";

    private static final String ENQUEUE_SQL = "INSERT INTO " + TABLE
            + " (event_id, idempotency_key, payload, status, available_at, attempts, created_by, created_at, updated_by, updated_at, version)"
            + " VALUES (?, ?, ?::jsonb, 'PENDING', ?, 0, ?, ?, ?, ?, 0)"
            + " ON CONFLICT (idempotency_key) DO NOTHING";

    private static final String CLAIM_SQL = """
            WITH candidates AS (
                SELECT event_id
                FROM spectra_core.sys_operation_log_outbox
                WHERE deleted IS NULL
                  AND processed_at IS NULL
                  AND status IN ('PENDING', 'PROCESSING')
                  AND attempts < ?
                  AND available_at <= ?
                  AND (lease_until IS NULL OR lease_until <= ?)
                ORDER BY available_at, event_id
                LIMIT ?
                FOR UPDATE SKIP LOCKED
            )
            UPDATE spectra_core.sys_operation_log_outbox outbox
            SET status = 'PROCESSING',
                lease_owner = ?,
                lease_until = ?,
                attempts = outbox.attempts + 1,
                updated_at = ?,
                version = outbox.version + 1
            FROM candidates
            WHERE outbox.event_id = candidates.event_id
            RETURNING outbox.event_id, outbox.idempotency_key, outbox.payload::text, outbox.attempts
            """;

    private static final String MARK_PROCESSED_SQL = "UPDATE " + TABLE
            + " SET status = 'PROCESSED', processed_at = ?, lease_owner = NULL, lease_until = NULL,"
            + " updated_at = ?, version = version + 1"
            + " WHERE event_id = ? AND status = 'PROCESSING' AND lease_owner = ? AND processed_at IS NULL";

    private static final String MARK_RETRY_SQL = "UPDATE " + TABLE
            + " SET status = 'PENDING', available_at = ?, last_error = ?, lease_owner = NULL, lease_until = NULL,"
            + " updated_at = ?, version = version + 1"
            + " WHERE event_id = ? AND status = 'PROCESSING' AND lease_owner = ? AND processed_at IS NULL";

    private static final String MARK_DEAD_LETTER_SQL = "UPDATE " + TABLE
            + " SET status = 'DEAD_LETTER', last_error = ?, lease_owner = NULL, lease_until = NULL,"
            + " updated_at = ?, version = version + 1"
            + " WHERE event_id = ? AND status = 'PROCESSING' AND lease_owner = ? AND processed_at IS NULL";

    private static final String PENDING_COUNT_SQL = "SELECT COUNT(*) FROM " + TABLE
            + " WHERE processed_at IS NULL AND deleted IS NULL";

    private final JdbcTemplate jdbcTemplate;

    public OperationLogOutboxRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 在当前业务事务中写入一条待消费事件；重复幂等键视为已接受。
     */
    public int enqueue(UUID eventId,
                       String idempotencyKey,
                       String payload,
                       Instant availableAt,
                       UUID operatorId) {
        return jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(ENQUEUE_SQL);
            statement.setObject(1, eventId);
            statement.setString(2, idempotencyKey);
            statement.setString(3, payload);
            statement.setTimestamp(4, timestamp(availableAt));
            setUuid(statement, 5, operatorId);
            statement.setTimestamp(6, timestamp(availableAt));
            setUuid(statement, 7, operatorId);
            statement.setTimestamp(8, timestamp(availableAt));
            return statement;
        });
    }

    /**
     * 使用 PostgreSQL 行锁和 SKIP LOCKED 获取并租约一批待处理事件。
     */
    public List<OperationLogOutboxEvent> claimBatch(String owner,
                                                    Instant now,
                                                    Instant leaseUntil,
                                                    int batchSize,
                                                    int maxAttempts) {
        return jdbcTemplate.query(connection -> {
            PreparedStatement statement = connection.prepareStatement(CLAIM_SQL);
            statement.setInt(1, maxAttempts);
            statement.setTimestamp(2, timestamp(now));
            statement.setTimestamp(3, timestamp(now));
            statement.setInt(4, batchSize);
            statement.setString(5, owner);
            statement.setTimestamp(6, timestamp(leaseUntil));
            statement.setTimestamp(7, timestamp(now));
            return statement;
        }, (resultSet, rowNumber) -> new OperationLogOutboxEvent(
                resultSet.getObject("event_id", UUID.class),
                resultSet.getString("idempotency_key"),
                resultSet.getString("payload"),
                resultSet.getInt("attempts")));
    }

    /** 标记事件已成功写入 sys_log。 */
    public int markProcessed(UUID eventId, String owner, Instant now) {
        return jdbcTemplate.update(MARK_PROCESSED_SQL,
                timestamp(now), timestamp(now), eventId, owner);
    }

    /** 标记事件可重试并清除当前租约。 */
    public int markRetry(UUID eventId, String owner, Instant now, Instant availableAt, String error) {
        return jdbcTemplate.update(MARK_RETRY_SQL,
                timestamp(availableAt), error, timestamp(now), eventId, owner);
    }

    /** 标记事件永久失败，保留事件和错误原因等待人工处置。 */
    public int markDeadLetter(UUID eventId, String owner, Instant now, String error) {
        return jdbcTemplate.update(MARK_DEAD_LETTER_SQL,
                error, timestamp(now), eventId, owner);
    }

    /** 返回尚未成功处理的事件数量，包含重试和人工处置事件。 */
    public long pendingCount() {
        Long count = jdbcTemplate.queryForObject(PENDING_COUNT_SQL, Long.class);
        return count == null ? 0L : count;
    }

    private static Timestamp timestamp(Instant instant) {
        return Timestamp.from(instant);
    }

    private static void setUuid(PreparedStatement statement, int index, UUID value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.OTHER);
        } else {
            statement.setObject(index, value);
        }
    }

    /** 已领取的 outbox 事件。 */
    public record OperationLogOutboxEvent(UUID eventId,
                                          String idempotencyKey,
                                          String payload,
                                          int attempts) {
    }
}
