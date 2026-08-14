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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void phase4ConcurrencyAndDelegationColumnsMustRemainInTheDatabaseContract() throws IOException {
        String schema = readSql();
        String migration = readV1();

        for (String source : List.of(schema, migration)) {
            assertTrue(source.contains("authority_level SMALLINT NOT NULL"));
            assertTrue(source.contains("CREATE TABLE spectra_security.role_assignment"));
            assertTrue(source.contains("version       BIGINT NOT NULL DEFAULT 0"));
            assertTrue(source.contains("CREATE TABLE spectra_security.assignment_permission_boundary"));
            assertTrue(source.contains("CREATE TABLE spectra_security.assignment_grant_boundary"));
            assertTrue(source.contains("PRIMARY KEY (assignment_id, permission_id)"));
        }
        assertTrue(migration.contains("security_version      BIGINT NOT NULL DEFAULT 0"));
        assertTrue(migration.contains("CREATE TABLE spectra_core.sys_department_closure"));
        assertTrue(migration.contains("CREATE TABLE spectra_core.sys_organization_version"));
        assertTrue(migration.contains("CONSTRAINT ck_sys_user_security_version CHECK (security_version >= 0)"));
    }

    @Test
    void phase5MustKeepClientPolicyAndMfaTablesInBothSchemaSources() throws IOException {
        String schema = readSql();
        String migration = readV1();
        for (String source : List.of(schema, migration)) {
            assertTrue(source.contains("CREATE TABLE spectra_security.security_client"));
            assertTrue(source.contains("CREATE TABLE spectra_security.authentication_method"));
            assertTrue(source.contains("CREATE TABLE spectra_security.session_policy"));
            assertTrue(source.contains("CREATE TABLE spectra_security.mfa_enrollment"));
            assertTrue(source.contains("CREATE TABLE spectra_security.totp_credential"));
            assertTrue(source.contains("CREATE TABLE spectra_security.recovery_code"));
            assertTrue(source.contains("encrypted_secret BYTEA NOT NULL"));
            assertTrue(source.contains("code_hash     VARCHAR(255) NOT NULL"));
        }
    }

    @Test
    void phase6MustKeepResourceScopeIndexesInFlywayAndSchemaDocumentation() throws IOException {
        String migration = readMigration("V7__permission_aware_datascope_indexes.sql");
        String documented = readOaSql();
        List<String> indexes = List.of(
                "idx_oa_asset_scope_department",
                "idx_oa_calendar_scope_owner_department",
                "idx_oa_contract_scope_department_owner",
                "idx_oa_document_scope_department_owner",
                "idx_oa_document_folder_scope_department",
                "idx_oa_meeting_scope_department",
                "idx_oa_meeting_participant_scope",
                "idx_oa_meeting_record_scope_department",
                "idx_oa_notice_scope_department",
                "idx_oa_application_scope_department",
                "idx_oa_leave_application_scope_department",
                "idx_oa_leave_balance_scope",
                "idx_oa_attendance_record_scope",
                "idx_oa_supply_item_scope_department",
                "idx_oa_purchase_scope_department",
                "idx_oa_purchase_item_scope_department",
                "idx_oa_purchase_receipt_scope_purchase",
                "idx_oa_purchase_receipt_item_scope_purchase_item",
                "idx_oa_reimbursement_scope_department",
                "idx_oa_reimbursement_item_scope_department");
        for (String index : indexes) {
            assertTrue(migration.contains(index), index);
            assertTrue(documented.contains(index), index);
        }
    }

    @Test
    void targetFlywayV1MustBeCompleteAndMustNotReintroduceLegacySecurityOrTenantTables() throws IOException {
        String migration = readV1();

        assertTrue(migration.contains("CREATE SCHEMA IF NOT EXISTS spectra_core"));
        assertTrue(migration.contains("CREATE SCHEMA IF NOT EXISTS spectra_security"));
        assertTrue(migration.contains("CREATE SCHEMA IF NOT EXISTS spectra_oa"));
        assertTrue(migration.contains("CREATE SCHEMA IF NOT EXISTS spectra_ai"));
        assertTrue(migration.contains("CREATE SCHEMA IF NOT EXISTS spectra_workflow"));
        assertTrue(migration.contains("CREATE SCHEMA IF NOT EXISTS spectra_notification"));
        assertTrue(migration.contains("CREATE TABLE spectra_security.role_menu"));
        assertTrue(migration.contains("CREATE TABLE spectra_core.sys_user_department_membership"));
        assertTrue(migration.contains("CREATE TABLE spectra_core.sys_department_closure"));
        assertTrue(migration.contains("CREATE TABLE spectra_core.sys_organization_version"));
        assertTrue(migration.contains("CREATE EXTENSION IF NOT EXISTS vector WITH SCHEMA spectra_ai"));

        assertFalse(migration.contains("tenant_id"));
        assertFalse(migration.contains("CREATE TABLE spectra_core.sys_account"));
        assertFalse(migration.contains("CREATE TABLE spectra_core.sys_role"));
        assertFalse(migration.contains("CREATE TABLE spectra_core.sys_authority"));
        assertFalse(migration.contains("CREATE TABLE spectra_core.sys_rel_user_role"));
        assertFalse(migration.contains("CREATE TABLE spectra_core.sys_rel_role_authority"));
        assertFalse(migration.contains("CREATE TABLE spectra_core.sys_role_data_scope"));
        assertFalse(migration.contains("CREATE TABLE spectra_core.sys_user_data_scope"));
    }

    @Test
    void runtimePrivilegesMustProtectAppendOnlyAudit() throws IOException {
        String migration = readMigration("V2__security_runtime_privileges.sql");

        assertTrue(migration.contains("CREATE ROLE spectra_runtime"));
        assertTrue(migration.contains("GRANT SELECT, INSERT ON spectra_security.security_audit_event TO spectra_runtime"));
        assertTrue(migration.contains("REVOKE UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER"));
        assertTrue(migration.contains("CREATE ROLE spectra_migrator"));
        assertFalse(migration.contains("ALTER DEFAULT PRIVILEGES IN SCHEMA spectra_security\n    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES"));
    }

    @Test
    void permissionCatalogMustBeFullySeededIntoTargetPermissionTable() throws IOException {
        String catalog = readCatalog();
        String seed = readMigration("V3__security_permission_catalog_seed.sql");
        String phase7Seed = readMigration("V8__security_permission_catalog_phase7.sql");
        var codes = catalog.lines()
                .filter(line -> line.matches("  - code: [a-z][a-z0-9_-]*(:[a-z][a-z0-9_-]*){1,2}"))
                .map(line -> line.substring("  - code: ".length()))
                .toList();

        assertEquals(115, codes.size());
        assertEquals(codes.size(), Set.copyOf(codes).size());
        assertTrue(seed.contains("INSERT INTO spectra_security.permission"));
        assertTrue(seed.contains("ON CONFLICT (code) DO NOTHING"));
        for (String code : codes) {
            assertTrue(seed.contains("md5('" + code + "')::uuid, '" + code + "'")
                    || phase7Seed.contains("md5('" + code + "')::uuid, '" + code + "'"), code);
        }
        assertFalse(seed.contains("ROLE_DEV_OPS"));
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

    private String readV1() throws IOException {
        return readMigration("V1__init_target_schema.sql");
    }

    private String readCatalog() throws IOException {
        var candidates = List.of(
                Path.of("..", "..", "docs", "security", "permission-catalog.yaml"),
                Path.of("..", "..", "..", "docs", "security", "permission-catalog.yaml"),
                Path.of("..", "..", "..", "..", "docs", "security", "permission-catalog.yaml"));
        for (var candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return Files.readString(candidate);
            }
        }
        throw new IOException("找不到 Permission Catalog");
    }

    private String readOaSql() throws IOException {
        var candidates = List.of(
                Path.of("docs", "sql", "spectra_oa", "建表.sql"),
                Path.of("..", "docs", "sql", "spectra_oa", "建表.sql"),
                Path.of("..", "..", "docs", "sql", "spectra_oa", "建表.sql"),
                Path.of("..", "..", "..", "docs", "sql", "spectra_oa", "建表.sql"));
        for (var candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return Files.readString(candidate);
            }
        }
        throw new IOException("找不到 OA schema SQL 文件");
    }

    private String readMigration(String fileName) throws IOException {
        var candidates = List.of(
                Path.of("spectra-config", "src", "main", "resources", "db", "migration", fileName),
                Path.of("..", "..", "spectra-config", "src", "main", "resources", "db", "migration", fileName),
                Path.of("..", "..", "..", "spectra-config", "src", "main", "resources", "db", "migration", fileName),
                Path.of("..", "..", "..", "..", "spectra-config", "src", "main", "resources", "db", "migration", fileName));
        for (var candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return Files.readString(candidate);
            }
        }
        throw new IOException("找不到目标 Flyway migration: " + fileName);
    }
}
