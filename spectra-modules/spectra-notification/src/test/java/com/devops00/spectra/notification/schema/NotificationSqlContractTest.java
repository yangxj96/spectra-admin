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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 通知建表和迁移脚本的结构契约测试；真实事务/并发行为需在 PostgreSQL 环境执行。 */
class NotificationSqlContractTest {

    @Test
    void shouldKeepSchemaTablesIndexesAndIdempotentMigrations() throws IOException {
        var schema = readSql("建表.sql");
        var messages = readSql("迁移/20260811-旧消息迁移.sql");
        var preferences = readSql("迁移/20260811-旧偏好迁移.sql");
        var tasks = readSql("迁移/20260811-任务内容.sql");

        assertTrue(schema.contains("CREATE SCHEMA IF NOT EXISTS spectra_notification"));
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS spectra_notification.ntf_request"));
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS spectra_notification.ntf_task"));
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS spectra_notification.ntf_delivery"));
        assertTrue(schema.contains("UK_NTF_REQUEST_IDEMPOTENCY"));
        assertTrue(schema.contains("UK_NTF_TASK_RECIPIENT_CHANNEL"));
        assertTrue(schema.contains("created_by"));
        assertTrue(schema.contains("notification_request_id"));
        assertTrue(schema.contains("receiver_user_id"));
        assertTrue(schema.contains("recipient_key_hash"));
        assertTrue(schema.contains("attempt_no"));
        assertTrue(schema.contains("is_read"));
        assertTrue(schema.contains("tenant_id"));
        var tableColumns = tableColumns(schema);
        assertEquals(131, tableColumns.size());
        assertEquals(tableColumns, commentedColumns(schema));
        assertTrue(messages.contains("ON CONFLICT DO NOTHING"));
        assertTrue(preferences.contains("ON CONFLICT (tenant_id, user_id, purpose, channel) WHERE deleted IS NULL DO UPDATE"));
        assertTrue(tasks.contains("ALTER TABLE spectra_notification.ntf_task ADD COLUMN IF NOT EXISTS title"));
        assertTrue(tasks.contains("ALTER TABLE spectra_notification.ntf_task ADD COLUMN IF NOT EXISTS content"));
    }

    private String readSql(String name) throws IOException {
        var candidates = List.of(
                Path.of("docs", "sql", "spectra_notification", name),
                Path.of("..", "docs", "sql", "spectra_notification", name),
                Path.of("..", "..", "docs", "sql", "spectra_notification", name),
                Path.of("..", "..", "..", "docs", "sql", "spectra_notification", name));
        for (var candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return Files.readString(candidate);
            }
        }
        throw new IOException("找不到通知 SQL 文件: " + name);
    }

    private Set<String> tableColumns(String schema) {
        var tablePattern = Pattern.compile(
                "CREATE TABLE IF NOT EXISTS spectra_notification\\.(\\w+) \\((.*?)\\R\\);", Pattern.DOTALL);
        var columnPattern = Pattern.compile("^ {4}([a-z][a-z0-9_]*)\\s+[A-Z]", Pattern.MULTILINE);
        var columns = new HashSet<String>();
        var tables = tablePattern.matcher(schema);
        while (tables.find()) {
            var tableName = tables.group(1);
            var tableColumns = columnPattern.matcher(tables.group(2));
            while (tableColumns.find()) {
                columns.add(tableName + "." + tableColumns.group(1));
            }
        }
        return columns;
    }

    private Set<String> commentedColumns(String schema) {
        var commentPattern = Pattern.compile(
                "^COMMENT ON COLUMN spectra_notification\\.(\\w+)\\.(\\w+) IS '[^']+';$", Pattern.MULTILINE);
        var columns = new HashSet<String>();
        var comments = commentPattern.matcher(schema);
        while (comments.find()) {
            columns.add(comments.group(1) + "." + comments.group(2));
        }
        return columns;
    }
}
