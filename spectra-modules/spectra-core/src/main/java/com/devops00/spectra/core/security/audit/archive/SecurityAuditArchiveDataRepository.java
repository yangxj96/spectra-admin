/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.audit.archive;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

/** 从不可删除的安全审计事实表生成和复核归档内容。 */
@Repository
public class SecurityAuditArchiveDataRepository {

    private static final String TABLE = "spectra_security.sec_security_audit_event";

    private static final String ROW_SQL = "SELECT row_to_json(audit_event)::text AS payload"
            + " FROM " + TABLE + " audit_event"
            + " WHERE occurred_at >= ? AND occurred_at < ?"
            + " ORDER BY occurred_at, event_id";

    private static final long MAX_ARCHIVE_BYTES = 512L * 1024L * 1024L;

    private final JdbcTemplate jdbcTemplate;

    public SecurityAuditArchiveDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 按半开时间范围生成 JSON Lines；范围外事件不会进入归档内容。 */
    public ArchiveSnapshot snapshot(String partitionName, Instant rangeStart, Instant rangeEnd) {
        requireRange(partitionName, rangeStart, rangeEnd);
        var output = new ByteArrayOutputStream();
        var rowCount = new long[]{0L};
        jdbcTemplate.query(connection -> statement(connection, rangeStart, rangeEnd), resultSet -> {
            byte[] line = (resultSet.getString("payload") + "\n").getBytes(StandardCharsets.UTF_8);
            if (output.size() + line.length > MAX_ARCHIVE_BYTES) {
                throw new IllegalStateException("安全审计归档内容超过单次处理上限");
            }
            output.writeBytes(line);
            rowCount[0]++;
        });
        return new ArchiveSnapshot(output.toByteArray(), rowCount[0]);
    }

    private static PreparedStatement statement(java.sql.Connection connection,
                                               Instant rangeStart,
                                               Instant rangeEnd)
            throws java.sql.SQLException {
        PreparedStatement statement = connection.prepareStatement(ROW_SQL);
        statement.setTimestamp(1, Timestamp.from(rangeStart));
        statement.setTimestamp(2, Timestamp.from(rangeEnd));
        return statement;
    }

    private static void requireRange(String partitionName, Instant rangeStart, Instant rangeEnd) {
        if (partitionName == null || !partitionName.matches("[A-Za-z0-9_]{1,128}")) {
            throw new IllegalArgumentException("安全审计分区名称不合法");
        }
        if (rangeStart == null || rangeEnd == null || !rangeEnd.isAfter(rangeStart)) {
            throw new IllegalArgumentException("安全审计归档时间范围不合法");
        }
    }

    /** 归档快照，内容为 UTF-8 JSON Lines。 */
    public record ArchiveSnapshot(byte[] content, long rowCount) {

        public ArchiveSnapshot {
            if (content == null || rowCount < 0) {
                throw new IllegalArgumentException("安全审计归档快照不合法");
            }
            content = content.clone();
        }

        @Override
        public byte[] content() {
            return content.clone();
        }
    }
}
