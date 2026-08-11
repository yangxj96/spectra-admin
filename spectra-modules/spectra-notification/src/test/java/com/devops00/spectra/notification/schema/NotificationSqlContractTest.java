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
import java.util.List;

import org.junit.jupiter.api.Test;

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
        assertTrue(schema.contains("uk_ntf_request_idempotency"));
        assertTrue(schema.contains("uk_ntf_task_recipient_channel"));
        assertTrue(schema.contains("tenant_id UUID NOT NULL"));
        assertTrue(messages.contains("ON CONFLICT (tenant_id, idempotency_key) DO NOTHING"));
        assertTrue(preferences.contains("ON CONFLICT (tenant_id, user_id, purpose, channel) DO UPDATE"));
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
}
