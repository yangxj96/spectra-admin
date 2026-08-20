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
        assertTrue(schema.contains("CREATE TABLE spectra_security.sec_assignment_permission_boundary"));
        assertTrue(schema.contains("CREATE TABLE spectra_security.sec_assignment_grant_boundary"));
        assertTrue(schema.contains("scope_mode IN ('NONE', 'ALL', 'SELF', 'RULES')"));
        assertTrue(schema.contains("CREATE TABLE spectra_security.sec_root_policy"));
        assertTrue(schema.contains("VALUES ('SYSTEM', 1, 3)"));
        assertFalse(schema.contains("ManagementScope"));
        assertFalse(schema.contains("scope_access_union"));
    }

    @Test
    void shouldMakeSecurityAuditAppendOnlyAndRetainOutbox() throws IOException {
        String schema = readSql();

        assertTrue(schema.contains("PARTITION BY RANGE (occurred_at)"));
        assertTrue(schema.contains("CREATE TABLE spectra_security.sec_security_audit_event_default"));
        assertTrue(schema.contains("CREATE TRIGGER trg_sec_security_audit_event_immutable"));
        assertTrue(schema.contains("REVOKE UPDATE, DELETE ON spectra_security.sec_security_audit_event FROM PUBLIC"));
        assertTrue(schema.contains("CREATE TABLE spectra_security.sec_security_audit_retention_policy"));
        assertTrue(schema.contains("CREATE TABLE spectra_security.sec_security_audit_archive_manifest"));
        assertTrue(schema.contains("hot_retention_months >= 12"));
        assertTrue(schema.contains("total_retention_years >= 5"));
        assertTrue(schema.contains("CREATE TABLE spectra_security.sec_security_change_outbox"));
    }

    @Test
    void phase8ArchiveMetadataMustBeVersionedAndReadOnlyForRuntime() throws IOException {
        String migration = readMigration("V9__security_audit_retention_and_archive_manifest.sql");

        assertTrue(migration.contains("CREATE TABLE IF NOT EXISTS spectra_security.security_audit_retention_policy"));
        assertTrue(migration.contains("CREATE TABLE IF NOT EXISTS spectra_security.security_audit_archive_manifest"));
        assertTrue(migration.contains("GRANT SELECT ON spectra_security.security_audit_retention_policy TO spectra_runtime"));
        assertTrue(migration.contains("REVOKE INSERT, UPDATE, DELETE, TRUNCATE"));
        assertTrue(migration.contains("content_sha256"));
        assertTrue(migration.contains("SystemSecurityAudit"));
    }

    @Test
    void phase8SecurityPolicyDefaultsMustBeSeededIdempotently() throws IOException {
        String migration = readMigration("V13__seed_security_policy_defaults.sql");

        assertTrue(migration.contains("INSERT INTO spectra_security.security_client"));
        assertTrue(migration.contains("'web', 'Web 浏览器', 'ACTIVE'"));
        assertTrue(migration.contains("'app', '移动 App', 'ACTIVE'"));
        assertTrue(migration.contains("'mini', '微信小程序', 'ACTIVE'"));
        assertTrue(migration.contains("FROM spectra_security.security_client"));
        assertTrue(migration.contains("ON CONFLICT (client_id) DO NOTHING"));
        assertTrue(migration.contains("INSERT INTO spectra_security.password_policy"));
        assertTrue(migration.contains("VALUES ('SYSTEM', 12, TRUE, TRUE, TRUE, TRUE, NULL)"));
        assertTrue(migration.contains("ON CONFLICT (policy_key) DO NOTHING"));
    }

    @Test
    void phase9LegacyDataScopeMigrationMustDropOnlyKnownLegacyObjects() throws IOException {
        String migration = readMigration("V10__remove_legacy_data_scope.sql");

        assertTrue(migration.contains("DROP TABLE IF EXISTS spectra_core.sys_user_data_scope_target"));
        assertTrue(migration.contains("DROP TABLE IF EXISTS spectra_core.sys_user_data_scope"));
        assertTrue(migration.contains("DROP TABLE IF EXISTS spectra_core.sys_role_data_scope_target"));
        assertTrue(migration.contains("DROP TABLE IF EXISTS spectra_core.sys_role_data_scope"));
        assertTrue(migration.contains("ALTER TABLE IF EXISTS spectra_core.sys_role"));
        assertTrue(migration.contains("DROP COLUMN IF EXISTS scope"));
        assertFalse(migration.contains("CASCADE"));
        assertTrue(migration.contains("Historical user/role scope is not converted automatically"));
    }

    @Test
    void phase9LegacyAuthorizationMigrationMustDropOnlyRetiredObjects() throws IOException {
        String migration = readMigration("V11__remove_legacy_authorization_runtime.sql");

        assertTrue(migration.contains("DROP TABLE IF EXISTS spectra_core.sys_rel_role_authority"));
        assertTrue(migration.contains("DROP TABLE IF EXISTS spectra_core.sys_rel_role_menu"));
        assertTrue(migration.contains("DROP TABLE IF EXISTS spectra_core.sys_rel_user_role"));
        assertTrue(migration.contains("DROP TABLE IF EXISTS spectra_core.sys_authority"));
        assertTrue(migration.contains("DROP TABLE IF EXISTS spectra_core.sys_role"));
        assertFalse(migration.contains("CASCADE"));
        assertFalse(migration.contains("sys_account"));
    }

    @Test
    void phase9LegacyAccountMigrationMustDropOnlyTheRetiredAccountTable() throws IOException {
        String migration = readMigration("V12__remove_legacy_account.sql");

        assertTrue(migration.contains("DROP TABLE IF EXISTS spectra_core.sys_account"));
        assertFalse(migration.contains("CASCADE"));
        assertFalse(migration.contains("INSERT"));
        assertFalse(migration.contains("UPDATE"));
    }

    @Test
    void phase4ConcurrencyAndDelegationColumnsMustRemainInTheDatabaseContract() throws IOException {
        String schema = readSql();
        String migration = readV1();

        assertTrue(schema.contains("authority_level SMALLINT NOT NULL"));
        assertTrue(schema.contains("CREATE TABLE spectra_security.sec_role_assignment"));
        assertTrue(schema.contains("version       BIGINT NOT NULL DEFAULT 0"));
        assertTrue(schema.contains("CREATE TABLE spectra_security.sec_assignment_permission_boundary"));
        assertTrue(schema.contains("CREATE TABLE spectra_security.sec_assignment_grant_boundary"));
        assertTrue(schema.contains("PRIMARY KEY (assignment_id, permission_id)"));
        assertTrue(migration.contains("authority_level SMALLINT NOT NULL"));
        assertTrue(migration.contains("CREATE TABLE spectra_security.role_assignment"));
        assertTrue(migration.contains("CREATE TABLE spectra_security.assignment_permission_boundary"));
        assertTrue(migration.contains("CREATE TABLE spectra_security.assignment_grant_boundary"));
        assertTrue(migration.contains("security_version      BIGINT NOT NULL DEFAULT 0"));
        assertTrue(migration.contains("CREATE TABLE spectra_core.sys_department_closure"));
        assertTrue(migration.contains("CREATE TABLE spectra_core.sys_organization_version"));
        assertTrue(migration.contains("CONSTRAINT ck_sys_user_security_version CHECK (security_version >= 0)"));
    }

    @Test
    void phase5MustKeepClientPolicyAndMfaTablesInBothSchemaSources() throws IOException {
        String schema = readSql();
        String migration = readV1();
        for (String table : List.of("sec_security_client", "sec_authentication_method", "sec_session_policy",
                "sec_mfa_enrollment", "sec_totp_credential", "sec_recovery_code")) {
            assertTrue(schema.contains("CREATE TABLE spectra_security." + table));
        }
        for (String table : List.of("security_client", "authentication_method", "session_policy",
                "mfa_enrollment", "totp_credential", "recovery_code")) {
            assertTrue(migration.contains("CREATE TABLE spectra_security." + table));
        }
        assertTrue(schema.contains("encrypted_secret BYTEA NOT NULL"));
        assertTrue(schema.contains("code_hash     VARCHAR(255) NOT NULL"));
        assertTrue(migration.contains("encrypted_secret BYTEA NOT NULL"));
        assertTrue(migration.contains("code_hash     VARCHAR(255) NOT NULL"));
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
        assertTrue(migration.contains("resource_code"));
        assertTrue(migration.contains("action_code"));
        assertTrue(migration.contains("allowed_scope_modes"));
        assertTrue(migration.contains("system_managed"));
        assertTrue(migration.contains("CREATE TABLE spectra_security.role_permission"));
        assertTrue(migration.contains("CREATE TABLE spectra_security.role_grantable_permission"));
        assertTrue(migration.contains("CREATE TABLE spectra_core.sys_user_department_membership"));
        assertTrue(migration.contains("primary_department_id UUID"));
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

    @Test
    void phase10MustRenameDeployedSecurityTablesAndDatabaseObjectsToSecPrefix() throws IOException {
        String schema = readSql();
        String migration = readMigration("V18__normalize_security_object_names.sql");

        assertTrue(schema.contains("CREATE TABLE spectra_security.sec_permission"));
        assertTrue(schema.contains("CONSTRAINT pk_sec_permission PRIMARY KEY"));
        assertTrue(schema.contains("CONSTRAINT fk_sec_role_permission_role_id"));
        assertTrue(schema.contains("CREATE INDEX idx_sec_role_assignment_user_state"));
        assertFalse(schema.contains("CREATE TABLE spectra_security.permission"));
        assertFalse(schema.contains("+-- spectra_security: table and column comments"));

        assertTrue(migration.contains("ALTER TABLE spectra_security.permission RENAME TO sec_permission"));
        assertTrue(migration.contains("ALTER TABLE spectra_security.role RENAME TO sec_role"));
        assertTrue(migration.contains("ALTER TABLE spectra_security.sec_permission RENAME CONSTRAINT permission_pkey TO pk_sec_permission"));
        assertTrue(migration.contains("ALTER FUNCTION spectra_security.reject_audit_mutation() RENAME TO sec_reject_audit_mutation"));
    }

    @Test
    void phase11MustRemoveLegacyNotificationRuntimeTablesAndFreezeDepartmentDeletePermission() throws IOException {
        String migration = readMigration("V19__remove_legacy_notification_runtime.sql");

        assertTrue(migration.contains("DROP TABLE IF EXISTS spectra_core.sys_notification_setting"));
        assertTrue(migration.contains("DROP TABLE IF EXISTS spectra_core.sys_notification"));
        assertTrue(migration.contains("UPDATE spectra_security.sec_permission"));
        assertTrue(migration.contains("WHERE code = 'department:disable'"));
        assertTrue(migration.contains("SET state = 'DEPRECATED'"));
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
