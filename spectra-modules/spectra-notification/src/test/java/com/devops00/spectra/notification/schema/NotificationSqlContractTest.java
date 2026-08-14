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

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.devops00.spectra.notification.mapper.NotificationTaskMapper;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 通知建表脚本与 Mapper XML 的结构契约测试；真实事务和并发行为需在 PostgreSQL 环境执行。
 */
class NotificationSqlContractTest {

    @Test
    void shouldKeepSchemaTablesIndexesAndColumnComments() throws IOException {
        var schema = readSql("建表.sql");

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
        assertFalse(schema.contains("tenant_id"), "通知模块目标 schema 不应恢复已移除的 tenant_id");
        var tableColumns = tableColumns(schema);
        assertEquals(125, tableColumns.size());
        assertEquals(tableColumns, commentedColumns(schema));
    }

    @Test
    void shouldPutPostgresLockClauseAfterOrderByAndLimit() throws IOException {
        var mapper = readResource("mapper/NotificationTaskMapper.xml");
        var orderByIndex = mapper.indexOf("ORDER BY priority DESC, scheduled_at ASC, created_at ASC");
        var limitIndex = mapper.indexOf("LIMIT #{limit}");
        var lockIndex = mapper.indexOf("FOR UPDATE SKIP LOCKED");

        assertTrue(orderByIndex >= 0, "通知任务领取 SQL 缺少 ORDER BY 子句");
        assertTrue(limitIndex > orderByIndex, "LIMIT 子句必须位于 ORDER BY 之后");
        assertTrue(lockIndex > limitIndex, "PostgreSQL 锁定子句必须位于 ORDER BY 和 LIMIT 之后");
    }

    @Test
    void shouldBypassDataPermissionParserForPostgresLockingQuery() throws Exception {
        var method = NotificationTaskMapper.class.getMethod("selectPendingTasks", Instant.class, int.class);
        var interceptorIgnore = method.getAnnotation(InterceptorIgnore.class);
        var mappedStatementId = NotificationTaskMapper.class.getName() + ".selectPendingTasks";
        var classIgnoreStrategy = InterceptorIgnoreHelper.initSqlParserInfoCache(NotificationTaskMapper.class);
        InterceptorIgnoreHelper.initSqlParserInfoCache(classIgnoreStrategy, NotificationTaskMapper.class.getName(), method);
        var ignoreStrategy = InterceptorIgnoreHelper.getIgnoreStrategy(mappedStatementId);

        assertNotNull(interceptorIgnore, "通知任务领取 SQL 必须跳过会重新序列化 SQL 的数据权限拦截器");
        assertEquals("true", interceptorIgnore.dataPermission());
        assertNotNull(ignoreStrategy, "MyBatis-Plus 必须识别通知任务领取方法的拦截器忽略策略");
        assertEquals(Boolean.TRUE, ignoreStrategy.getDataPermission());

        var originalSql = "SELECT id FROM spectra_notification.ntf_task "
                + "ORDER BY priority DESC LIMIT ? FOR UPDATE SKIP LOCKED";
        var configuration = new Configuration();
        var mappedStatement = new MappedStatement.Builder(
                configuration, mappedStatementId, new StaticSqlSource(configuration, originalSql), SqlCommandType.SELECT)
                .build();
        var boundSql = mappedStatement.getBoundSql(null);

        new DataPermissionInterceptor().beforeQuery(null, mappedStatement, null, RowBounds.DEFAULT, null, boundSql);

        assertEquals(originalSql, boundSql.getSql(), "数据权限拦截器不得重新序列化 PostgreSQL 锁定查询");
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

    private String readResource(String name) throws IOException {
        try (var resource = getClass().getClassLoader().getResourceAsStream(name)) {
            if (resource == null) {
                throw new IOException("找不到通知模块资源: " + name);
            }
            return new String(resource.readAllBytes(), StandardCharsets.UTF_8);
        }
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
