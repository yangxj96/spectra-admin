/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.audit.archive;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** 安全审计归档 manifest 的 PostgreSQL 状态访问端口。 */
@Repository
public class SecurityAuditArchiveManifestRepository {

    private static final String TABLE = "spectra_security.sec_security_audit_archive_manifest";

    private static final String SELECT_COLUMNS = "id, manifest_id, partition_name, range_start, range_end, "
            + "object_uri, content_sha256, content_length, row_count, state, archived_at, verified_at, last_error, "
            + "attempts, available_at, lease_owner, lease_until, created_by, created_at, updated_by, updated_at";

    private static final String INSERT_SQL = "INSERT INTO " + TABLE
            + " (manifest_id, partition_name, range_start, range_end, state, available_at, created_by, created_at, updated_by, updated_at, version)"
            + " VALUES (?, ?, ?, ?, 'PLANNED', ?, ?, ?, ?, ?, 0)"
            + " ON CONFLICT (partition_name) DO NOTHING";

    private static final String CLAIM_SQL = """
            WITH candidates AS (
                SELECT id
                FROM spectra_security.sec_security_audit_archive_manifest
                WHERE deleted IS NULL
                  AND state IN ('PLANNED', 'ARCHIVED', 'RESTORE_PENDING', 'FAILED')
                  AND attempts <= ?
                  AND available_at <= ?
                  AND (lease_until IS NULL OR lease_until <= ?)
                ORDER BY available_at, created_at, id
                LIMIT ?
                FOR UPDATE SKIP LOCKED
            )
            UPDATE spectra_security.sec_security_audit_archive_manifest manifest
            SET lease_owner = ?, lease_until = ?, attempts = manifest.attempts + 1,
                updated_at = ?, version = manifest.version + 1
            FROM candidates
            WHERE manifest.id = candidates.id
            RETURNING manifest.id, manifest.manifest_id, manifest.partition_name,
                      manifest.range_start, manifest.range_end, manifest.object_uri,
                      manifest.content_sha256, manifest.content_length, manifest.row_count, manifest.state,
                      manifest.archived_at, manifest.verified_at, manifest.last_error,
                      manifest.attempts, manifest.available_at, manifest.lease_owner,
                      manifest.lease_until, manifest.created_by, manifest.created_at,
                      manifest.updated_by, manifest.updated_at
            """;

    private static final String FIND_SQL = "SELECT " + SELECT_COLUMNS + " FROM " + TABLE
            + " WHERE manifest_id = ? AND deleted IS NULL";

    private static final String CLAIM_ONE_SQL = "UPDATE " + TABLE
            + " SET lease_owner = ?, lease_until = ?, attempts = attempts + 1, updated_at = ?, version = version + 1"
            + " WHERE manifest_id = ? AND deleted IS NULL"
            + " AND state IN ('ARCHIVED', 'RESTORE_PENDING') AND attempts <= ? AND available_at <= ?"
            + " AND (lease_until IS NULL OR lease_until <= ?)";

    private static final String MARK_ARCHIVED_SQL = "UPDATE " + TABLE
            + " SET state = 'ARCHIVED', object_uri = ?, content_sha256 = ?, content_length = ?, row_count = ?, archived_at = ?,"
            + " available_at = ?, lease_owner = NULL, lease_until = NULL, last_error = NULL,"
            + " updated_by = ?, updated_at = ?, version = version + 1"
            + " WHERE manifest_id = ? AND state IN ('PLANNED', 'FAILED') AND lease_owner = ? AND deleted IS NULL";

    private static final String MARK_VERIFIED_SQL = "UPDATE " + TABLE
            + " SET state = 'VERIFIED', verified_at = ?, available_at = ?, lease_owner = NULL, lease_until = NULL,"
            + " last_error = NULL, updated_by = ?, updated_at = ?, version = version + 1"
            + " WHERE manifest_id = ? AND state = 'ARCHIVED' AND lease_owner = ? AND deleted IS NULL";

    private static final String MARK_RESTORED_SQL = "UPDATE " + TABLE
            + " SET state = 'RESTORED', verified_at = ?, available_at = ?, lease_owner = NULL, lease_until = NULL,"
            + " last_error = NULL, updated_by = ?, updated_at = ?, version = version + 1"
            + " WHERE manifest_id = ? AND state = 'RESTORE_PENDING' AND lease_owner = ? AND deleted IS NULL";

    private static final String MARK_FAILED_SQL = "UPDATE " + TABLE
            + " SET state = 'FAILED', available_at = ?, last_error = ?, lease_owner = NULL, lease_until = NULL,"
            + " updated_by = ?, updated_at = ?, version = version + 1"
            + " WHERE manifest_id = ? AND lease_owner = ? AND deleted IS NULL";

    private static final String RENEW_LEASE_SQL = "UPDATE " + TABLE
            + " SET lease_until = ?, updated_at = ?, version = version + 1"
            + " WHERE manifest_id = ? AND lease_owner = ? AND deleted IS NULL"
            + " AND state IN ('PLANNED', 'FAILED', 'ARCHIVED', 'RESTORE_PENDING')"
            + " AND lease_until > ?";

    private static final String REQUEST_RESTORE_SQL = "UPDATE " + TABLE
            + " SET state = 'RESTORE_PENDING', available_at = ?, last_error = NULL, updated_by = ?,"
            + " updated_at = ?, version = version + 1"
            + " WHERE manifest_id = ? AND state = 'VERIFIED' AND deleted IS NULL";

    private static final String REPLAY_FAILED_SQL = "UPDATE " + TABLE
            + " SET state = 'PLANNED', object_uri = NULL, content_sha256 = NULL, content_length = NULL, row_count = NULL,"
            + " archived_at = NULL, verified_at = NULL, attempts = 0, available_at = ?, last_error = NULL,"
            + " lease_owner = NULL, lease_until = NULL, updated_by = ?, updated_at = ?, version = version + 1"
            + " WHERE manifest_id = ? AND state = 'FAILED' AND deleted IS NULL";

    private static final String PENDING_COUNT_SQL = "SELECT COUNT(*) FROM " + TABLE
            + " WHERE deleted IS NULL AND state IN ('PLANNED', 'ARCHIVED', 'RESTORE_PENDING')";

    private static final String OLDEST_PENDING_SQL = "SELECT MIN(available_at) FROM " + TABLE
            + " WHERE deleted IS NULL AND state IN ('PLANNED', 'ARCHIVED', 'RESTORE_PENDING')";

    private final JdbcTemplate jdbcTemplate;

    public SecurityAuditArchiveManifestRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 创建一个按审计分区唯一的归档计划。 */
    public int createPlan(UUID manifestId,
                          String partitionName,
                          Instant rangeStart,
                          Instant rangeEnd,
                          UUID operatorId,
                          Instant now) {
        return jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_SQL);
            setUuid(statement, 1, manifestId);
            statement.setString(2, partitionName);
            statement.setTimestamp(3, timestamp(rangeStart));
            statement.setTimestamp(4, timestamp(rangeEnd));
            statement.setTimestamp(5, timestamp(now));
            setUuid(statement, 6, operatorId);
            statement.setTimestamp(7, timestamp(now));
            setUuid(statement, 8, operatorId);
            statement.setTimestamp(9, timestamp(now));
            return statement;
        });
    }

    public ArchiveManifest find(UUID manifestId) {
        List<ArchiveManifest> records = jdbcTemplate.query(FIND_SQL, this::map, manifestId);
        return records.isEmpty() ? null : records.getFirst();
    }

    /** 使用 PostgreSQL 行锁和 SKIP LOCKED 获取一批待处理 manifest。 */
    public List<ArchiveManifest> claimBatch(String owner,
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
        }, this::map);
    }

    /** 手工校验接口对单个现有归档获取同一租约，避免绕过 worker 的状态检查。 */
    public ArchiveManifest claimOne(UUID manifestId,
                                    String owner,
                                    Instant now,
                                    Instant leaseUntil,
                                    int maxAttempts) {
        int updated = jdbcTemplate.update(CLAIM_ONE_SQL, owner, timestamp(leaseUntil), timestamp(now), manifestId,
                maxAttempts, timestamp(now), timestamp(now));
        return updated == 1 ? find(manifestId) : null;
    }

    public int markArchived(UUID manifestId,
                            String owner,
                            String objectUri,
                            String contentSha256,
                            long contentLength,
                            long rowCount,
                            Instant archivedAt,
                            UUID operatorId,
                            Instant now) {
        return jdbcTemplate.update(MARK_ARCHIVED_SQL, objectUri, contentSha256, contentLength, rowCount,
                timestamp(archivedAt), timestamp(now), operatorId, timestamp(now), manifestId, owner);
    }

    public int markVerified(UUID manifestId, String owner, UUID operatorId, Instant now) {
        return jdbcTemplate.update(MARK_VERIFIED_SQL, timestamp(now), timestamp(now), operatorId,
                timestamp(now), manifestId, owner);
    }

    public int markRestored(UUID manifestId, String owner, UUID operatorId, Instant now) {
        return jdbcTemplate.update(MARK_RESTORED_SQL, timestamp(now), timestamp(now), operatorId,
                timestamp(now), manifestId, owner);
    }

    public int markFailed(UUID manifestId,
                          String owner,
                          UUID operatorId,
                          Instant now,
                          Instant nextAvailableAt,
                          String error) {
        return jdbcTemplate.update(MARK_FAILED_SQL, timestamp(nextAvailableAt), truncate(error), operatorId,
                timestamp(now), manifestId, owner);
    }

    /** 在对象存储读写仍进行时延长 manifest 租约。 */
    public int renewLease(UUID manifestId, String owner, Instant leaseUntil, Instant now) {
        return jdbcTemplate.update(RENEW_LEASE_SQL, timestamp(leaseUntil), timestamp(now), manifestId, owner,
                timestamp(now));
    }

    public int requestRestore(UUID manifestId, UUID operatorId, Instant now) {
        return jdbcTemplate.update(REQUEST_RESTORE_SQL, timestamp(now), operatorId, timestamp(now), manifestId);
    }

    public int replayFailed(UUID manifestId, UUID operatorId, Instant now) {
        return jdbcTemplate.update(REPLAY_FAILED_SQL, timestamp(now), operatorId, timestamp(now), manifestId);
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

    /** 读取有效的默认安全审计总保留年限。 */
    public int totalRetentionYears() {
        Integer years = jdbcTemplate.queryForObject(
                "SELECT total_retention_years FROM spectra_security.sec_security_audit_retention_policy"
                        + " WHERE policy_key = 'DEFAULT' AND state = 'ACTIVE'",
                Integer.class);
        if (years == null || years <= 0) {
            throw new IllegalStateException("未配置有效的安全审计总保留年限");
        }
        return years;
    }

    /** 读取有效的默认安全审计归档后端标识。 */
    public String archiveBackend() {
        String backend = jdbcTemplate.queryForObject(
                "SELECT archive_backend FROM spectra_security.sec_security_audit_retention_policy"
                        + " WHERE policy_key = 'DEFAULT' AND state = 'ACTIVE'",
                String.class);
        if (backend == null || backend.isBlank()) {
            throw new IllegalStateException("未配置有效的安全审计归档后端");
        }
        return backend.trim();
    }

    private ArchiveManifest map(java.sql.ResultSet resultSet, int ignored) throws java.sql.SQLException {
        return new ArchiveManifest(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("manifest_id", UUID.class),
                resultSet.getString("partition_name"),
                resultSet.getTimestamp("range_start").toInstant(),
                resultSet.getTimestamp("range_end").toInstant(),
                resultSet.getString("object_uri"),
                trim(resultSet.getString("content_sha256")),
                resultSet.getObject("content_length", Long.class),
                resultSet.getObject("row_count", Long.class),
                resultSet.getString("state"),
                toInstant(resultSet.getTimestamp("archived_at")),
                toInstant(resultSet.getTimestamp("verified_at")),
                resultSet.getString("last_error"),
                resultSet.getInt("attempts"),
                resultSet.getTimestamp("available_at").toInstant(),
                resultSet.getString("lease_owner"),
                toInstant(resultSet.getTimestamp("lease_until")),
                resultSet.getObject("created_by", UUID.class),
                resultSet.getTimestamp("created_at").toInstant(),
                resultSet.getObject("updated_by", UUID.class),
                resultSet.getTimestamp("updated_at").toInstant());
    }

    private static Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String truncate(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replaceAll("[\\r\\n\\t]+", " ").trim();
        return normalized.length() <= 2000 ? normalized : normalized.substring(0, 2000);
    }

    private static Timestamp timestamp(Instant instant) {
        return Timestamp.from(instant);
    }

    private static void setUuid(PreparedStatement statement, int index, UUID value) throws java.sql.SQLException {
        if (value == null) {
            statement.setNull(index, Types.OTHER);
        } else {
            statement.setObject(index, value);
        }
    }

    /** 归档 manifest 的数据库视图。 */
    public record ArchiveManifest(UUID id,
                                  UUID manifestId,
                                  String partitionName,
                                  Instant rangeStart,
                                  Instant rangeEnd,
                                  String objectUri,
                                  String contentSha256,
                                  Long contentLength,
                                  Long rowCount,
                                  String state,
                                  Instant archivedAt,
                                  Instant verifiedAt,
                                  String lastError,
                                  int attempts,
                                  Instant availableAt,
                                  String leaseOwner,
                                  Instant leaseUntil,
                                  UUID createdBy,
                                  Instant createdAt,
                                  UUID updatedBy,
                                  Instant updatedAt) {
    }
}
