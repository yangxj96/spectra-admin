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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 当前 V1 安全 schema 契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/20
 */
class SecuritySchemaContractTest {

    @Test
    void shouldDeclareCurrentTargetSchemaAndRejectLegacySecurityObjects() throws IOException {
        String migration = readV1();

        for (String schemaName : List.of("spectra_core", "spectra_security", "spectra_oa",
                "spectra_workflow", "spectra_notification")) {
            assertTrue(migration.contains("CREATE SCHEMA IF NOT EXISTS " + schemaName)
                    || migration.contains("CREATE SCHEMA " + schemaName + ";"), schemaName);
        }
        for (String table : List.of(
                "sec_permission",
                "sec_role",
                "sec_role_permission",
                "sec_role_grantable_permission",
                "sec_role_assignment",
                "sec_authorization_scope",
                "sec_assignment_permission_boundary",
                "sec_assignment_grant_boundary",
                "sec_scope_rule",
                "sec_security_client",
                "sec_authentication_method",
                "sec_session_policy",
                "sec_password_policy",
                "sec_mfa_enrollment",
                "sec_totp_credential",
                "sec_recovery_code",
                "sec_root_policy",
                "sec_role_menu")) {
            assertTrue(migration.contains("CREATE TABLE spectra_security." + table), table);
        }
        assertTrue(migration.contains("CREATE TABLE spectra_security.sec_security_audit_event"));
        assertTrue(migration.contains("CREATE TABLE spectra_security.sec_security_audit_archive_manifest"));
        assertTrue(migration.contains("CREATE TABLE spectra_security.sec_security_change_outbox"));
        assertTrue(migration.contains("CREATE TRIGGER trg_sec_security_audit_event_immutable"));

        for (String legacyObject : List.of(
                "CREATE TABLE spectra_core.sys_account",
                "CREATE TABLE spectra_core.sys_role",
                "CREATE TABLE spectra_core.sys_authority",
                "CREATE TABLE spectra_core.sys_rel_user_role",
                "CREATE TABLE spectra_core.sys_rel_role_authority",
                "CREATE TABLE spectra_core.sys_role_data_scope",
                "CREATE TABLE spectra_core.sys_user_data_scope",
                "CREATE TABLE spectra_security.security_",
                "CREATE TABLE spectra_security.permission")) {
            assertFalse(migration.contains(legacyObject), legacyObject);
        }
    }

    @Test
    void shouldKeepAuditBoundaryAndSecuritySeedsInTheCurrentBaseline() throws IOException {
        String migration = readV1();

        assertTrue(migration.contains("PARTITION BY RANGE (occurred_at)"));
        assertTrue(migration.contains("CREATE TRIGGER trg_sec_security_audit_event_immutable"));
        assertTrue(migration.contains("REVOKE UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER"));
        assertTrue(migration.contains("ON spectra_security.sec_security_audit_event FROM spectra_runtime"));
        assertTrue(migration.contains("hot_retention_months >= 12"));
        assertTrue(migration.contains("total_retention_years >= 5"));
        assertTrue(migration.contains("CREATE TABLE spectra_security.sec_security_change_outbox"));
        assertTrue(migration.contains("CREATE ROLE spectra_runtime"));
        assertTrue(migration.contains("GRANT SELECT, INSERT ON spectra_security.sec_security_audit_event TO spectra_runtime"));
        assertTrue(migration.contains("GRANT SELECT, INSERT ON spectra_security.sec_security_audit_event_default TO spectra_runtime"));
        assertTrue(migration.contains("INSERT INTO spectra_security.sec_permission"));
        assertTrue(migration.contains("INSERT INTO spectra_security.sec_password_policy"));
        assertTrue(migration.contains("INSERT INTO spectra_security.sec_security_client"));
        assertTrue(migration.contains("INSERT INTO spectra_security.sec_session_policy"));
    }

    @Test
    void shouldKeepCurrentBaselineColumnsAndTechnicalConstraints() throws IOException {
        String migration = readV1();

        assertTrue(migration.contains("authority_level smallint"));
        assertTrue(migration.contains("CREATE UNIQUE INDEX uk_sec_assignment_permission_boundary_assignment_permission"));
        assertTrue(migration.contains("CREATE UNIQUE INDEX uk_sec_assignment_grant_boundary_assignment_permission"));
        assertTrue(migration.contains("WHERE (deleted IS NULL)"));
        assertTrue(migration.contains("CONSTRAINT pk_sec_permission PRIMARY KEY"));
        assertTrue(migration.contains("primary_department_id uuid"));
        assertTrue(migration.contains("security_version bigint"));
        assertTrue(migration.contains("CREATE TABLE spectra_core.sys_department_closure"));
        assertTrue(migration.contains("CREATE TABLE spectra_core.sys_organization_version"));
        assertTrue(migration.contains("CREATE TABLE spectra_core.sys_system_state"));
        assertTrue(migration.contains("CREATE TABLE spectra_security.sec_assignment_permission_boundary"));
        assertTrue(migration.contains("CREATE TABLE spectra_security.sec_assignment_grant_boundary"));
        assertTrue(migration.contains("encrypted_secret bytea"));
        assertTrue(migration.contains("code_hash character varying(255)"));
    }

    @Test
    void permissionCatalogMustBeSeededIntoTheCurrentPermissionTable() throws IOException {
        String catalog = readCatalog();
        String migration = readV1();
        var codes = catalog.lines()
                .filter(line -> line.matches("  - code: [a-z][a-z0-9_-]*(:[a-z][a-z0-9_-]*){1,2}"))
                .map(line -> line.substring("  - code: ".length()))
                .toList();

        assertEquals(111, codes.size());
        for (String code : codes) {
            assertTrue(migration.contains("'" + code + "'"), code);
        }
    }

    @Test
    void resourceScopeIndexesMustRemainInTheFlywayBaseline() throws IOException {
        String migration = readV1();
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
        }
    }

    @Test
    void assignmentBoundaryUniquenessMustIgnoreSoftDeletedHistory() throws IOException {
        String migration = readV1();

        assertTrue(migration.contains("CREATE UNIQUE INDEX uk_sec_assignment_permission_boundary_assignment_permission"));
        assertTrue(migration.contains("CREATE UNIQUE INDEX uk_sec_assignment_grant_boundary_assignment_permission"));
        assertTrue(migration.contains(
                "ON spectra_security.sec_assignment_permission_boundary USING btree (assignment_id, permission_id) WHERE (deleted IS NULL);"));
        assertTrue(migration
                .contains("ON spectra_security.sec_assignment_grant_boundary USING btree (assignment_id, permission_id) WHERE (deleted IS NULL);"));
    }

    private String readV1() throws IOException {
        return readMigration("V1__init_db.sql");
    }

    private String readCatalog() throws IOException {
        var candidates = List.of(
                Path.of("..", "..", "docs", "10-后端", "permission-catalog.yaml"),
                Path.of("..", "..", "..", "docs", "10-后端", "permission-catalog.yaml"),
                Path.of("..", "..", "..", "..", "docs", "10-后端", "permission-catalog.yaml"));
        for (var candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return Files.readString(candidate);
            }
        }
        throw new IOException("找不到 Permission Catalog");
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
