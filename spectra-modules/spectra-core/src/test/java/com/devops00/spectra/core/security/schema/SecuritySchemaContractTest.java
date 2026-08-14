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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 1 安全 schema 契约测试。
 * <p>
 * 该测试确保目标 DDL 不会退回旧的全局 DataScope/兼容关系，并保留审计与 Root 的硬约束。
 */
class SecuritySchemaContractTest {

    @Test
    void shouldDeclarePermissionSpecificBoundariesAndRootPolicy() throws IOException {
        String schema = readSql();

        assertTrue(schema.contains("CREATE SCHEMA IF NOT EXISTS spectra_security"));
        assertTrue(schema.contains("CREATE TABLE spectra_security.assignment_permission_boundary"));
        assertTrue(schema.contains("CREATE TABLE spectra_security.assignment_grant_boundary"));
        assertTrue(schema.contains("scope_mode IN ('NONE', 'ALL', 'SELF', 'RULES')"));
        assertTrue(schema.contains("CREATE TABLE spectra_security.root_policy"));
        assertTrue(schema.contains("VALUES ('SYSTEM', 1, 3)"));
        assertFalse(schema.contains("ManagementScope"));
        assertFalse(schema.contains("scope_access_union"));
    }

    @Test
    void shouldMakeSecurityAuditAppendOnlyAndRetainOutbox() throws IOException {
        String schema = readSql();

        assertTrue(schema.contains("PARTITION BY RANGE (occurred_at)"));
        assertTrue(schema.contains("CREATE TABLE spectra_security.security_audit_event_default"));
        assertTrue(schema.contains("CREATE TRIGGER trg_security_audit_event_immutable"));
        assertTrue(schema.contains("REVOKE UPDATE, DELETE ON spectra_security.security_audit_event FROM PUBLIC"));
        assertTrue(schema.contains("CREATE TABLE spectra_security.security_change_outbox"));
    }

    private String readSql() throws IOException {
        var candidates = List.of(
                Path.of("docs", "sql", "spectra_security", "建表.sql"),
                Path.of("..", "docs", "sql", "spectra_security", "建表.sql"),
                Path.of("..", "..", "docs", "sql", "spectra_security", "建表.sql"),
                Path.of("..", "..", "..", "docs", "sql", "spectra_security", "建表.sql"));
        for (var candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return Files.readString(candidate);
            }
        }
        throw new IOException("找不到安全 schema SQL 文件");
    }
}
