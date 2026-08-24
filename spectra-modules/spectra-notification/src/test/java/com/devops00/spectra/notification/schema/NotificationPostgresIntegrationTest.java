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

package com.devops00.spectra.notification.schema;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * 在真实 PostgreSQL 上验证通知表的事务、唯一约束和 Worker 领取语义。
 *
 * <p>测试只在显式设置 {@code SPECTRA_NOTIFICATION_POSTGRES_TEST=true} 时启用，连接信息从
 * {@code DB_URL}、{@code DB_USERNAME} 和 {@code DB_PASSWORD} 读取。每个测试使用随机测试数据并在结束时清理，
 * 不依赖本机已有业务数据。</p>
 */
@EnabledIfEnvironmentVariable(named = "SPECTRA_NOTIFICATION_POSTGRES_TEST", matches = "true")
class NotificationPostgresIntegrationTest {

    private static final String SCHEMA = "spectra_notification";

    @Test
    void shouldRollbackRequestAndTaskTogether() throws Exception {
        var requestId = UUID.randomUUID();
        var taskId = UUID.randomUUID();
        try (var connection = openConnection()) {
            connection.setAutoCommit(false);
            insertRequest(connection, requestId, "rollback-" + requestId);
            insertTask(connection, requestId, taskId, "rollback-recipient", Instant.now().plusSeconds(60),
                    "PENDING");
            connection.rollback();
        }
        try (var connection = openConnection()) {
            assertEquals(0, count(connection, "ntf_request", requestId));
            assertEquals(0, count(connection, "ntf_task", taskId));
        }
    }

    @Test
    void shouldAllowOnlyOneConcurrentRequestIdempotencyKey() throws Exception {
        var firstRequestId = UUID.randomUUID();
        var secondRequestId = UUID.randomUUID();
        var key = "concurrent-" + UUID.randomUUID();
        try (var first = openConnection(); var second = openConnection()) {
            first.setAutoCommit(false);
            second.setAutoCommit(false);
            insertRequest(first, firstRequestId, key);

            var started = new CountDownLatch(1);
            try (var executor = Executors.newSingleThreadExecutor()) {
                var duplicate = executor.submit(() -> {
                    started.countDown();
                    try {
                        insertRequest(second, secondRequestId, key);
                        second.commit();
                        return null;
                    } catch (Throwable exception) {
                        second.rollback();
                        return exception;
                    }
                });
                assertTrue(started.await(2, TimeUnit.SECONDS));
                Thread.sleep(100);
                assertFalse(duplicate.isDone(), "第二个事务应在唯一索引上等待第一个事务");
                first.commit();
                var failure = duplicate.get(5, TimeUnit.SECONDS);
                assertNotNull(failure, "重复幂等键必须被 PostgreSQL 唯一约束拒绝");
                var sqlException = assertInstanceOf(SQLException.class, failure);
                assertEquals("23505", sqlException.getSQLState());
            }
        }
        try (var connection = openConnection()) {
            assertEquals(1, countByKey(connection, key));
            deleteRequest(connection, firstRequestId);
        }
    }

    @Test
    void shouldRejectDuplicateTaskRecipientAndChannel() throws Exception {
        var requestId = UUID.randomUUID();
        var firstTaskId = UUID.randomUUID();
        var secondTaskId = UUID.randomUUID();
        var recipientKey = "same-recipient-" + UUID.randomUUID();
        try (var connection = openConnection()) {
            insertRequest(connection, requestId, "task-unique-" + requestId);
            insertTask(connection, requestId, firstTaskId, recipientKey, Instant.now().plusSeconds(60),
                    "PENDING");
            try {
                insertTask(connection, requestId, secondTaskId, recipientKey, Instant.now().plusSeconds(60),
                        "PENDING");
                fail("同一 Request、接收人和渠道的重复任务必须失败");
            } catch (SQLException exception) {
                assertEquals("23505", exception.getSQLState());
            }
        } finally {
            try (var connection = openConnection()) {
                deleteTask(connection, firstTaskId);
                deleteRequest(connection, requestId);
            }
        }
    }

    @Test
    void shouldAllowOnlyOneInboxMessagePerTask() throws Exception {
        var requestId = UUID.randomUUID();
        var taskId = UUID.randomUUID();
        try (var connection = openConnection()) {
            insertRequest(connection, requestId, "inbox-unique-" + requestId);
            insertTask(connection, requestId, taskId, "inbox-recipient", Instant.now().plusSeconds(60),
                    "PENDING");
            insertInboxMessage(connection, requestId, taskId);
            try {
                insertInboxMessage(connection, requestId, taskId);
                fail("同一任务不得生成重复站内信");
            } catch (SQLException exception) {
                assertEquals("23505", exception.getSQLState());
            }
        } finally {
            try (var connection = openConnection()) {
                deleteInboxMessage(connection, taskId);
                deleteTask(connection, taskId);
                deleteRequest(connection, requestId);
            }
        }
    }

    @Test
    void shouldSkipTaskLockedByAnotherWorker() throws Exception {
        var requestId = UUID.randomUUID();
        var taskId = UUID.randomUUID();
        try (var first = openConnection(); var second = openConnection()) {
            first.setAutoCommit(false);
            insertRequest(first, requestId, "worker-lock-" + requestId);
            insertTask(first, requestId, taskId, "worker-recipient", Instant.now().minusSeconds(1),
                    "PENDING");
            first.commit();
            first.setAutoCommit(false);
            second.setAutoCommit(false);

            assertEquals(List.of(taskId), selectPendingTaskIds(first));
            assertTrue(selectPendingTaskIds(second).isEmpty(), "第二个 Worker 不应领取已加行锁的任务");

            first.rollback();
            second.rollback();
        } finally {
            try (var connection = openConnection()) {
                deleteTask(connection, taskId);
                deleteRequest(connection, requestId);
            }
        }
    }

    @Test
    void shouldRecoverExpiredWorkerLeaseWithCompareAndSet() throws Exception {
        var requestId = UUID.randomUUID();
        var taskId = UUID.randomUUID();
        var completedTaskId = UUID.randomUUID();
        var lockedAt = Instant.now().minusSeconds(600);
        try (var connection = openConnection()) {
            insertRequest(connection, requestId, "lease-" + requestId);
            insertTask(connection, requestId, taskId, "lease-recipient", Instant.now().minusSeconds(60),
                    "PROCESSING");
            try (var lock = connection.prepareStatement(
                    "UPDATE spectra_notification.ntf_task SET locked_by = ?, locked_at = ? WHERE id = ?")) {
                lock.setString(1, "crashed-worker");
                setInstant(lock, 2, lockedAt);
                lock.setObject(3, taskId);
                assertEquals(1, lock.executeUpdate());
            }
            try (var update = connection.prepareStatement("""
                    UPDATE spectra_notification.ntf_task
                       SET status = 'RETRYING', scheduled_at = ?, next_retry_at = ?, last_error_code = ?,
                           locked_by = NULL, locked_at = NULL
                     WHERE id = ? AND status = 'PROCESSING' AND locked_at < ?
                    """)) {
                setInstant(update, 1, Instant.now());
                setInstant(update, 2, Instant.now());
                update.setString(3, "WORKER_LEASE_EXPIRED");
                update.setObject(4, taskId);
                setInstant(update, 5, Instant.now().minusSeconds(300));
                assertEquals(1, update.executeUpdate());
            }
            try (var query = connection.prepareStatement(
                    "SELECT status, locked_by, locked_at, last_error_code FROM spectra_notification.ntf_task WHERE id = ?")) {
                query.setObject(1, taskId);
                try (var result = query.executeQuery()) {
                    assertTrue(result.next());
                    assertEquals("RETRYING", result.getString("status"));
                    assertNull(result.getString("locked_by"));
                    assertNull(result.getObject("locked_at"));
                    assertEquals("WORKER_LEASE_EXPIRED", result.getString("last_error_code"));
                }
            }
            insertTask(connection, requestId, completedTaskId, "completed-recipient",
                    Instant.now().minusSeconds(60), "SENT");
            try (var lock = connection.prepareStatement(
                    "UPDATE spectra_notification.ntf_task SET locked_by = ?, locked_at = ? WHERE id = ?")) {
                lock.setString(1, "crashed-worker");
                setInstant(lock, 2, lockedAt);
                lock.setObject(3, completedTaskId);
                assertEquals(1, lock.executeUpdate());
            }
            try (var update = connection.prepareStatement("""
                    UPDATE spectra_notification.ntf_task
                       SET status = 'RETRYING', locked_by = NULL, locked_at = NULL
                     WHERE id = ? AND status = 'PROCESSING' AND locked_at < ?
                    """)) {
                update.setObject(1, completedTaskId);
                setInstant(update, 2, Instant.now().minusSeconds(300));
                assertEquals(0, update.executeUpdate(), "已完成任务不得被租约恢复 CAS 覆盖");
            }
        } finally {
            try (var connection = openConnection()) {
                deleteTask(connection, taskId);
                deleteTask(connection, completedTaskId);
                deleteRequest(connection, requestId);
            }
        }
    }

    @Test
    void shouldExcludeExpiredTaskFromPendingQueue() throws Exception {
        var requestId = UUID.randomUUID();
        var taskId = UUID.randomUUID();
        try (var connection = openConnection()) {
            insertRequest(connection, requestId, "expired-" + requestId);
            insertTask(connection, requestId, taskId, "expired-recipient", Instant.now().minusSeconds(60),
                    "PENDING", Instant.now().minusSeconds(1));
            try (var query = connection.prepareStatement("""
                    SELECT COUNT(*)
                      FROM spectra_notification.ntf_task
                     WHERE id = ?
                       AND deleted IS NULL
                       AND status IN ('PENDING', 'RETRYING')
                       AND scheduled_at <= ?
                       AND (next_retry_at IS NULL OR next_retry_at <= ?)
                       AND (expires_at IS NULL OR expires_at > ?)
                    """)) {
                var now = Instant.now();
                query.setObject(1, taskId);
                setInstant(query, 2, now);
                setInstant(query, 3, now);
                setInstant(query, 4, now);
                try (var result = query.executeQuery()) {
                    assertTrue(result.next());
                    assertEquals(0, result.getLong(1));
                }
            }
        } finally {
            try (var connection = openConnection()) {
                deleteTask(connection, taskId);
                deleteRequest(connection, requestId);
            }
        }
    }

    @Test
    void shouldKeepNotificationIndexesAvailable() throws Exception {
        try (var connection = openConnection();
                var statement = connection.createStatement();
                var result = statement.executeQuery("""
                        SELECT indexname
                          FROM pg_indexes
                         WHERE schemaname = 'spectra_notification'
                           AND indexname IN ('UK_NTF_REQUEST_IDEMPOTENCY', 'UK_NTF_TASK_RECIPIENT_CHANNEL',
                               'IDX_NTF_TASK_PENDING', 'IDX_NTF_INBOX_RECEIVER_UNREAD')
                        """)) {
            var indexes = new HashSet<String>();
            while (result.next()) {
                indexes.add(result.getString(1));
            }
            assertEquals(Set.of("UK_NTF_REQUEST_IDEMPOTENCY", "UK_NTF_TASK_RECIPIENT_CHANNEL", "IDX_NTF_TASK_PENDING",
                    "IDX_NTF_INBOX_RECEIVER_UNREAD"), indexes);
        }
    }

    @Test
    void shouldClearExpiredSensitivePayloadsWithoutTouchingBusinessContent() throws Exception {
        var requestId = UUID.randomUUID();
        var taskId = UUID.randomUUID();
        try (var connection = openConnection()) {
            insertRequest(connection, requestId, "cleanup-" + requestId);
            insertTask(connection, requestId, taskId, "cleanup-recipient", Instant.now().minusSeconds(60),
                    "SENT", Instant.now().minusSeconds(1));
            try (var requestUpdate = connection.prepareStatement("""
                    UPDATE spectra_notification.ntf_request
                       SET status = 'SUCCEEDED', sensitive_parameters_ciphertext = 'request-secret',
                           encryption_key_id = 'key-1', expires_at = ?
                     WHERE id = ?
                    """);
                    var taskUpdate = connection.prepareStatement("""
                            UPDATE spectra_notification.ntf_task
                               SET sensitive_parameters_ciphertext = 'task-secret', expires_at = ?
                             WHERE id = ?
                            """)) {
                setInstant(requestUpdate, 1, Instant.now().minusSeconds(1));
                requestUpdate.setObject(2, requestId);
                requestUpdate.executeUpdate();
                setInstant(taskUpdate, 1, Instant.now().minusSeconds(1));
                taskUpdate.setObject(2, taskId);
                taskUpdate.executeUpdate();
            }
            executeSensitivePayloadCleanup(connection, Instant.now(), Instant.now().minusSeconds(60), 100);
            try (var query = connection.prepareStatement("""
                    SELECT r.sensitive_parameters_ciphertext, r.encryption_key_id,
                           t.sensitive_parameters_ciphertext, t.title, t.content
                      FROM spectra_notification.ntf_request r
                      JOIN spectra_notification.ntf_task t ON t.notification_request_id = r.id
                     WHERE r.id = ? AND t.id = ?
                    """)) {
                query.setObject(1, requestId);
                query.setObject(2, taskId);
                try (var result = query.executeQuery()) {
                    assertTrue(result.next());
                    assertNull(result.getString(1));
                    assertNull(result.getString(2));
                    assertNull(result.getString(3));
                    assertEquals("Integration title", result.getString(4));
                    assertEquals("Integration content", result.getString(5));
                }
            }
        } finally {
            try (var connection = openConnection()) {
                deleteTask(connection, taskId);
                deleteRequest(connection, requestId);
            }
        }
    }

    private Connection openConnection() throws Exception {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(requiredEnvironment("DB_URL"), requiredEnvironment("DB_USERNAME"),
                requiredEnvironment("DB_PASSWORD"));
    }

    private void executeSensitivePayloadCleanup(Connection connection, Instant now, Instant cutoff, int limit)
            throws SQLException {
        try (var requestCleanup = connection.prepareStatement("""
                WITH candidates AS (
                    SELECT id
                      FROM spectra_notification.ntf_request
                     WHERE deleted IS NULL
                       AND sensitive_parameters_ciphertext IS NOT NULL
                       AND (expires_at IS NOT NULL AND expires_at <= ?
                            OR (status IN ('SUCCEEDED', 'PARTIAL', 'FAILED', 'CANCELLED', 'EXPIRED')
                                AND updated_at <= ?))
                     ORDER BY updated_at NULLS FIRST
                     LIMIT ?
                )
                UPDATE spectra_notification.ntf_request target
                   SET sensitive_parameters_ciphertext = NULL, encryption_key_id = NULL,
                       updated_at = CURRENT_TIMESTAMP
                 WHERE target.id IN (SELECT id FROM candidates)
                """);
                var taskCleanup = connection.prepareStatement("""
                        WITH candidates AS (
                            SELECT id
                              FROM spectra_notification.ntf_task
                             WHERE deleted IS NULL
                               AND sensitive_parameters_ciphertext IS NOT NULL
                               AND (expires_at IS NOT NULL AND expires_at <= ?
                                    OR (status IN ('SENT', 'FAILED', 'BLOCKED', 'UNKNOWN', 'EXPIRED', 'CANCELLED')
                                        AND updated_at <= ?))
                             ORDER BY updated_at NULLS FIRST
                             LIMIT ?
                        )
                        UPDATE spectra_notification.ntf_task target
                           SET sensitive_parameters_ciphertext = NULL, updated_at = CURRENT_TIMESTAMP
                         WHERE target.id IN (SELECT id FROM candidates)
                        """)) {
            setInstant(requestCleanup, 1, now);
            setInstant(requestCleanup, 2, cutoff);
            requestCleanup.setInt(3, limit);
            requestCleanup.executeUpdate();
            setInstant(taskCleanup, 1, now);
            setInstant(taskCleanup, 2, cutoff);
            taskCleanup.setInt(3, limit);
            taskCleanup.executeUpdate();
        }
    }

    private String requiredEnvironment(String name) {
        var value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("缺少真实 PostgreSQL 集成测试环境变量: " + name);
        }
        return value;
    }

    private void insertRequest(Connection connection, UUID requestId, String idempotencyKey)
            throws SQLException {
        try (var insert = connection.prepareStatement("""
                INSERT INTO spectra_notification.ntf_request
                    (id, external_request_id, idempotency_key, purpose, template_group_code,
                     source_module, business_type, business_id, initiator_type, parameters, status,
                     recipient_count, task_count, scheduled_at)
                VALUES (?, ?, ?, 'SYSTEM_NOTICE', 'integration-test', 'notification', 'integration', ?,
                        'SYSTEM', '{}'::jsonb, 'ACCEPTED', 0, 0, ?)
                """)) {
            insert.setObject(1, requestId);
            insert.setString(2, "external-" + requestId);
            insert.setString(3, idempotencyKey);
            insert.setString(4, requestId.toString());
            setInstant(insert, 5, Instant.now());
            insert.executeUpdate();
        }
    }

    private void insertTask(Connection connection, UUID requestId, UUID taskId, String recipientKey,
                            Instant scheduledAt, String status)
            throws SQLException {
        insertTask(connection, requestId, taskId, recipientKey, scheduledAt, status, null);
    }

    private void insertTask(Connection connection, UUID requestId, UUID taskId, String recipientKey,
                            Instant scheduledAt, String status, Instant expiresAt)
            throws SQLException {
        try (var insert = connection.prepareStatement("""
                INSERT INTO spectra_notification.ntf_task
                    (id, notification_request_id, channel, receiver_user_id, recipient_key_hash,
                     purpose, title, content, scheduled_at, expires_at, status)
                VALUES (?, ?, 'IN_APP', ?, ?, 'SYSTEM_NOTICE', 'Integration title', 'Integration content', ?, ?, ?)
                """)) {
            insert.setObject(1, taskId);
            insert.setObject(2, requestId);
            insert.setObject(3, UUID.randomUUID());
            insert.setString(4, recipientKey);
            setInstant(insert, 5, scheduledAt);
            if (expiresAt == null) {
                insert.setNull(6, Types.TIMESTAMP_WITH_TIMEZONE);
            } else {
                setInstant(insert, 6, expiresAt);
            }
            insert.setString(7, status);
            insert.executeUpdate();
        }
    }

    private void insertInboxMessage(Connection connection, UUID requestId, UUID taskId)
            throws SQLException {
        try (var insert = connection.prepareStatement("""
                INSERT INTO spectra_notification.ntf_inbox_message
                    (id, notification_task_id, notification_request_id, receiver_user_id,
                     purpose, title, content)
                VALUES (?, ?, ?, ?, 'SYSTEM_NOTICE', 'Inbox title', 'Inbox content')
                """)) {
            insert.setObject(1, UUID.randomUUID());
            insert.setObject(2, taskId);
            insert.setObject(3, requestId);
            insert.setObject(4, UUID.randomUUID());
            insert.executeUpdate();
        }
    }

    private List<UUID> selectPendingTaskIds(Connection connection) throws SQLException {
        try (var query = connection.prepareStatement("""
                SELECT id
                  FROM spectra_notification.ntf_task
                 WHERE deleted IS NULL
                   AND status IN ('PENDING', 'RETRYING')
                   AND scheduled_at <= ?
                   AND (next_retry_at IS NULL OR next_retry_at <= ?)
                   AND (expires_at IS NULL OR expires_at > ?)
                 ORDER BY priority DESC, scheduled_at ASC, created_at ASC
                 LIMIT 1
                 FOR UPDATE SKIP LOCKED
                """)) {
            var now = Instant.now();
            setInstant(query, 1, now);
            setInstant(query, 2, now);
            setInstant(query, 3, now);
            try (var result = query.executeQuery()) {
                var ids = new ArrayList<UUID>();
                while (result.next()) {
                    ids.add(result.getObject(1, UUID.class));
                }
                return ids;
            }
        }
    }

    private long count(Connection connection, String table, UUID id) throws SQLException {
        try (var query = connection.prepareStatement("SELECT COUNT(*) FROM " + SCHEMA + "." + table + " WHERE id = ?")) {
            query.setObject(1, id);
            try (var result = query.executeQuery()) {
                result.next();
                return result.getLong(1);
            }
        }
    }

    private void setInstant(PreparedStatement statement, int index, Instant value) throws SQLException {
        statement.setTimestamp(index, Timestamp.from(value));
    }

    private long countByKey(Connection connection, String idempotencyKey) throws SQLException {
        try (var query = connection.prepareStatement(
                "SELECT COUNT(*) FROM spectra_notification.ntf_request WHERE idempotency_key = ?")) {
            query.setString(1, idempotencyKey);
            try (var result = query.executeQuery()) {
                result.next();
                return result.getLong(1);
            }
        }
    }

    private void deleteTask(Connection connection, UUID taskId) throws SQLException {
        try (var delete = connection.prepareStatement("DELETE FROM spectra_notification.ntf_task WHERE id = ?")) {
            delete.setObject(1, taskId);
            delete.executeUpdate();
        }
    }

    private void deleteInboxMessage(Connection connection, UUID taskId) throws SQLException {
        try (var delete = connection.prepareStatement(
                "DELETE FROM spectra_notification.ntf_inbox_message WHERE notification_task_id = ?")) {
            delete.setObject(1, taskId);
            delete.executeUpdate();
        }
    }

    private void deleteRequest(Connection connection, UUID requestId) throws SQLException {
        try (var delete = connection.prepareStatement("DELETE FROM spectra_notification.ntf_request WHERE id = ?")) {
            delete.setObject(1, requestId);
            delete.executeUpdate();
        }
    }
}
