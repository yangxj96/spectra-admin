/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.audit.outbox;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** 安全变更 outbox 的 PostgreSQL 访问端口。 */
@Repository
public class SecurityChangeOutboxRepository {

    private static final String TABLE = "spectra_security.sec_security_change_outbox";

    private static final String ENQUEUE_SQL = "INSERT INTO " + TABLE
            + " (id, idempotency_key, event_type, aggregate_type, aggregate_id, payload, correlation_id,"
            + " state, available_at, attempts, created_by, created_at, updated_by, updated_at, version)"
            + " VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, 'PENDING', ?, 0, ?, ?, ?, ?, 0)"
            + " ON CONFLICT (idempotency_key) DO NOTHING";

    private static final String CLAIM_SQL = """
            WITH candidates AS (
                SELECT id
                FROM spectra_security.sec_security_change_outbox
                WHERE deleted IS NULL
                  AND processed_at IS NULL
                  AND state IN ('PENDING', 'PROCESSING')
                  AND attempts <= ?
                  AND available_at <= ?
                  AND (lease_until IS NULL OR lease_until <= ?)
                ORDER BY available_at, created_at, id
                LIMIT ?
                FOR UPDATE SKIP LOCKED
            )
            UPDATE spectra_security.sec_security_change_outbox outbox
            SET state = 'PROCESSING',
                lease_owner = ?,
                lease_until = ?,
                attempts = outbox.attempts + 1,
                updated_at = ?,
                version = outbox.version + 1
            FROM candidates
            WHERE outbox.id = candidates.id
            RETURNING outbox.id, outbox.idempotency_key, outbox.event_type, outbox.aggregate_type,
                      outbox.aggregate_id, outbox.payload::text, outbox.correlation_id, outbox.attempts
            """;

    private static final String MARK_PROCESSED_SQL = "UPDATE " + TABLE
            + " SET state = 'PROCESSED', processed_at = ?, lease_owner = NULL, lease_until = NULL,"
            + " last_error = NULL, updated_at = ?, version = version + 1"
            + " WHERE id = ? AND state = 'PROCESSING' AND lease_owner = ? AND processed_at IS NULL";

    private static final String MARK_RETRY_SQL = "UPDATE " + TABLE
            + " SET state = 'PENDING', available_at = ?, last_error = ?, lease_owner = NULL, lease_until = NULL,"
            + " updated_at = ?, version = version + 1"
            + " WHERE id = ? AND state = 'PROCESSING' AND lease_owner = ? AND processed_at IS NULL";

    private static final String MARK_DEAD_LETTER_SQL = "UPDATE " + TABLE
            + " SET state = 'DEAD_LETTER', last_error = ?, lease_owner = NULL, lease_until = NULL,"
            + " updated_at = ?, version = version + 1"
            + " WHERE id = ? AND state = 'PROCESSING' AND lease_owner = ? AND processed_at IS NULL";

    private static final String RENEW_LEASE_SQL = "UPDATE " + TABLE
            + " SET lease_until = ?, updated_at = ?, version = version + 1"
            + " WHERE id = ? AND state = 'PROCESSING' AND lease_owner = ? AND processed_at IS NULL"
            + " AND lease_until > ?";

    private static final String REPLAY_SQL = "UPDATE " + TABLE
            + " SET state = 'PENDING', processed_at = NULL, available_at = ?, attempts = 0, last_error = NULL,"
            + " lease_owner = NULL, lease_until = NULL, updated_by = ?, updated_at = ?, version = version + 1"
            + " WHERE id = ? AND state = 'DEAD_LETTER' AND deleted IS NULL";

    private static final String PENDING_COUNT_SQL = "SELECT COUNT(*) FROM " + TABLE
            + " WHERE processed_at IS NULL AND deleted IS NULL AND state <> 'PROCESSED'";

    private static final String OLDEST_PENDING_SQL = "SELECT MIN(available_at) FROM " + TABLE
            + " WHERE processed_at IS NULL AND deleted IS NULL AND state <> 'PROCESSED'";

    private final JdbcTemplate jdbcTemplate;

    public SecurityChangeOutboxRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 在当前安全变更事务中写入待消费事件；重复幂等键视为已接受。 */
    public int enqueue(UUID eventId,
                       String idempotencyKey,
                       String eventType,
                       String aggregateType,
                       UUID aggregateId,
                       String payload,
                       String correlationId,
                       Instant availableAt,
                       UUID operatorId) {
        return jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(ENQUEUE_SQL);
            statement.setObject(1, eventId);
            statement.setString(2, idempotencyKey);
            statement.setString(3, eventType);
            statement.setString(4, aggregateType);
            setUuid(statement, 5, aggregateId);
            statement.setString(6, payload);
            statement.setString(7, correlationId);
            statement.setTimestamp(8, timestamp(availableAt));
            setUuid(statement, 9, operatorId);
            statement.setTimestamp(10, timestamp(availableAt));
            setUuid(statement, 11, operatorId);
            statement.setTimestamp(12, timestamp(availableAt));
            return statement;
        });
    }

    /** 使用 PostgreSQL 行锁和 SKIP LOCKED 获取并租约一批待处理事件。 */
    public List<SecurityChangeOutboxEvent> claimBatch(String owner,
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
        }, (resultSet, ignored) -> new SecurityChangeOutboxEvent(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("idempotency_key"),
                resultSet.getString("event_type"),
                resultSet.getString("aggregate_type"),
                resultSet.getObject("aggregate_id", UUID.class),
                resultSet.getString("payload"),
                resultSet.getString("correlation_id"),
                resultSet.getInt("attempts")));
    }

    /** 确认事件已交给所有已注册的安全变更消费者。 */
    public int markProcessed(UUID eventId, String owner, Instant now) {
        return jdbcTemplate.update(MARK_PROCESSED_SQL, timestamp(now), timestamp(now), eventId, owner);
    }

    /** 标记事件可重试并清除当前租约。 */
    public int markRetry(UUID eventId, String owner, Instant now, Instant availableAt, String error) {
        return jdbcTemplate.update(MARK_RETRY_SQL, timestamp(availableAt), error, timestamp(now), eventId, owner);
    }

    /** 标记事件永久失败，保留事件和 payload 等待人工重放。 */
    public int markDeadLetter(UUID eventId, String owner, Instant now, String error) {
        return jdbcTemplate.update(MARK_DEAD_LETTER_SQL, error, timestamp(now), eventId, owner);
    }

    /** 在下游处理仍进行时延长租约；租约失效或事件已确认时返回 0。 */
    public int renewLease(UUID eventId, String owner, Instant leaseUntil, Instant now) {
        return jdbcTemplate.update(RENEW_LEASE_SQL, timestamp(leaseUntil), timestamp(now), eventId, owner,
                timestamp(now));
    }

    /** 仅允许人工将死信重新放回待处理队列。 */
    public int replay(UUID eventId, UUID operatorId, Instant now) {
        return jdbcTemplate.update(REPLAY_SQL, timestamp(now), operatorId, timestamp(now), eventId);
    }

    public long pendingCount() {
        Long count = jdbcTemplate.queryForObject(PENDING_COUNT_SQL, Long.class);
        return count == null ? 0L : count;
    }

    public Instant oldestPendingAt() {
        return jdbcTemplate.queryForObject(OLDEST_PENDING_SQL,
                (resultSet, ignored) -> resultSet.getTimestamp(1) == null
                        ? null
                        : resultSet.getTimestamp(1).toInstant());
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

    /** 已领取的安全变更 outbox 事件。 */
    public record SecurityChangeOutboxEvent(UUID eventId,
                                            String idempotencyKey,
                                            String eventType,
                                            String aggregateType,
                                            UUID aggregateId,
                                            String payload,
                                            String correlationId,
                                            int attempts) {
    }
}
