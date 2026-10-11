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

package com.devops00.spectra.core.security.authorization.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 在专用测试 PostgreSQL 的独立 schema 中验证角色收缩、回滚和行锁竞争。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/11
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class JdbcRoleAssignmentBoundaryReconcilerPostgresTest {

    private final DriverManagerDataSource dataSource = testDataSource();

    private final JdbcTemplate jdbc = new JdbcTemplate(dataSource);

    private final TransactionTemplate transactions = new TransactionTemplate(new DataSourceTransactionManager(dataSource));

    private String schema;

    private JdbcRoleAssignmentBoundaryReconciler reconciler;

    @BeforeEach
    void prepare() {
        schema = "core032_" + UUID.randomUUID().toString().replace("-", "");
        jdbc.execute("CREATE SCHEMA " + schema);
        jdbc.execute("CREATE TABLE " + schema + ".sec_role (id uuid PRIMARY KEY, version bigint NOT NULL)");
        jdbc.execute("CREATE TABLE " + schema
                + ".sec_role_assignment (id uuid PRIMARY KEY, role_id uuid NOT NULL, "
                + "state text NOT NULL DEFAULT 'ACTIVE', deleted timestamptz)");
        jdbc.execute("CREATE TABLE " + schema
                + ".sec_role_permission (role_id uuid NOT NULL, permission_id uuid NOT NULL, deleted timestamptz)");
        jdbc.execute("CREATE TABLE " + schema
                + ".sec_role_grantable_permission (role_id uuid NOT NULL, permission_id uuid NOT NULL, deleted timestamptz)");
        jdbc.execute("CREATE TABLE " + schema + ".sec_authorization_scope (id uuid PRIMARY KEY)");
        jdbc.execute("CREATE TABLE " + schema + ".sec_scope_rule (id uuid PRIMARY KEY, scope_id uuid NOT NULL)");
        jdbc.execute("CREATE TABLE " + schema + ".sec_assignment_permission_boundary ("
                + "id uuid PRIMARY KEY, assignment_id uuid NOT NULL, permission_id uuid NOT NULL, "
                + "scope_id uuid NOT NULL, deleted timestamptz)");
        jdbc.execute("CREATE TABLE " + schema + ".sec_assignment_grant_boundary ("
                + "id uuid PRIMARY KEY, assignment_id uuid NOT NULL, permission_id uuid NOT NULL, "
                + "scope_id uuid NOT NULL, deleted timestamptz)");
        reconciler = new JdbcRoleAssignmentBoundaryReconciler(jdbc, schema);
    }

    @AfterEach
    void cleanOwnSchema() {
        if (schema != null) {
            if (!schema.matches("core032_[0-9a-f]{32}")) {
                throw new IllegalStateException("测试 schema 名称越界");
            }
            jdbc.execute("DROP SCHEMA " + schema + " CASCADE");
        }
    }

    @Test
    void shrunkRoleRemovesObsoleteAccessAndGrantButKeepsValidAndOtherRoleRows() {
        UUID role = UUID.randomUUID();
        UUID otherRole = UUID.randomUUID();
        UUID assignment = assignment(role);
        UUID revokedAssignment = assignment(role);
        UUID otherAssignment = assignment(otherRole);
        UUID retained = UUID.randomUUID();
        UUID removed = UUID.randomUUID();
        declare("sec_role_permission", role, retained);
        jdbc.update("UPDATE " + schema + ".sec_role_assignment SET state = 'REVOKED' WHERE id = ?",
                revokedAssignment);
        declare("sec_role_grantable_permission", role, retained);
        declare("sec_role_permission", otherRole, removed);
        UUID keptScope = boundary("sec_assignment_permission_boundary", assignment, retained);
        UUID removedAccessScope = boundary("sec_assignment_permission_boundary", assignment, removed);
        UUID removedGrantScope = boundary("sec_assignment_grant_boundary", assignment, removed);
        UUID revokedScope = boundary("sec_assignment_permission_boundary", revokedAssignment, removed);
        UUID otherScope = boundary("sec_assignment_permission_boundary", otherAssignment, removed);
        jdbc.update("INSERT INTO " + schema + ".sec_scope_rule (id, scope_id) VALUES (?, ?)",
                UUID.randomUUID(), removedAccessScope);

        transactions.executeWithoutResult(status -> reconciler.reconcile(role));

        assertEquals(1, count("sec_assignment_permission_boundary", "scope_id", keptScope));
        assertEquals(1, count("sec_assignment_permission_boundary", "scope_id", otherScope));
        assertEquals(0, count("sec_assignment_permission_boundary", "scope_id", removedAccessScope));
        assertEquals(0, count("sec_assignment_permission_boundary", "scope_id", revokedScope));
        assertEquals(0, count("sec_assignment_grant_boundary", "scope_id", removedGrantScope));
        assertEquals(0, count("sec_authorization_scope", "id", removedAccessScope));
        assertEquals(0, count("sec_authorization_scope", "id", removedGrantScope));
        assertEquals(0, count("sec_scope_rule", "scope_id", removedAccessScope));
        assertEquals(1, count("sec_authorization_scope", "id", keptScope));
        assertEquals(1, count("sec_authorization_scope", "id", otherScope));

        declare("sec_role_permission", role, removed);
        declare("sec_role_grantable_permission", role, removed);
        transactions.executeWithoutResult(status -> reconciler.reconcile(role));
        assertEquals(0, count("sec_assignment_permission_boundary", "scope_id", removedAccessScope),
                "角色重新授予能力不能恢复已删除的 Assignment Boundary");
    }

    @Test
    void rejectsCleanupOutsideRoleChangeTransaction() {
        UUID role = UUID.randomUUID();
        UUID scope = boundary("sec_assignment_permission_boundary", assignment(role), UUID.randomUUID());

        assertThrows(IllegalStateException.class, () -> reconciler.reconcile(role));

        assertEquals(1, count("sec_assignment_permission_boundary", "scope_id", scope));
    }

    @Test
    void cleanupFailureRollsBackDeletedBoundary() {
        UUID role = UUID.randomUUID();
        UUID scope = boundary("sec_assignment_permission_boundary", assignment(role), UUID.randomUUID());
        jdbc.execute("DROP TABLE " + schema + ".sec_scope_rule");

        assertThrows(RuntimeException.class, () -> transactions.execute(status -> {
            reconciler.reconcile(role);
            return null;
        }));

        assertEquals(1, count("sec_assignment_permission_boundary", "scope_id", scope));
        assertEquals(1, count("sec_authorization_scope", "id", scope));
    }

    @Test
    void assignmentSharedRoleLockSerializesRoleUpdate() throws Exception {
        UUID role = UUID.randomUUID();
        jdbc.update("INSERT INTO " + schema + ".sec_role (id, version) VALUES (?, 0)", role);
        var locked = new CountDownLatch(1);
        var attempting = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var assignment = executor.submit(() -> transactions.execute(status -> {
                jdbc.queryForObject("SELECT version FROM " + schema + ".sec_role WHERE id = ? FOR SHARE",
                        Long.class, role);
                locked.countDown();
                await(release);
                return null;
            }));
            assertTrue(locked.await(5, TimeUnit.SECONDS));
            var roleChange = executor.submit(() -> transactions.execute(status -> {
                attempting.countDown();
                return jdbc.update("UPDATE " + schema + ".sec_role SET version = version + 1 WHERE id = ?", role);
            }));
            assertTrue(attempting.await(5, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> roleChange.get(250, TimeUnit.MILLISECONDS));
            release.countDown();
            assignment.get(5, TimeUnit.SECONDS);
            assertEquals(1, roleChange.get(5, TimeUnit.SECONDS));
        } finally {
            release.countDown();
        }
        assertEquals(1L, jdbc.queryForObject("SELECT version FROM " + schema + ".sec_role WHERE id = ?",
                Long.class, role));
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("并发角色锁测试未收到释放信号");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("并发角色锁测试被中断", exception);
        }
    }

    private UUID assignment(UUID roleId) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO " + schema + ".sec_role_assignment (id, role_id) VALUES (?, ?)", id, roleId);
        return id;
    }

    private void declare(String table, UUID roleId, UUID permissionId) {
        jdbc.update("INSERT INTO " + schema + "." + table + " (role_id, permission_id) VALUES (?, ?)",
                roleId, permissionId);
    }

    private UUID boundary(String table, UUID assignmentId, UUID permissionId) {
        UUID scopeId = UUID.randomUUID();
        jdbc.update("INSERT INTO " + schema + ".sec_authorization_scope (id) VALUES (?)", scopeId);
        jdbc.update("INSERT INTO " + schema + "." + table
                + " (id, assignment_id, permission_id, scope_id) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), assignmentId, permissionId, scopeId);
        return scopeId;
    }

    private int count(String table, String column, UUID id) {
        return jdbc.queryForObject("SELECT count(*) FROM " + schema + "." + table + " WHERE " + column + " = ?",
                Integer.class, id);
    }

    private static DriverManagerDataSource testDataSource() {
        String url = System.getenv("DB_URL");
        if (url == null || !url.matches("^jdbc:postgresql://[^/]+/devops00_spectra_db_test(?:\\?.*)?$")) {
            throw new IllegalStateException("真实依赖测试只允许 devops00_spectra_db_test");
        }
        String username = System.getenv("DB_USERNAME");
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("真实依赖测试缺少 DB_USERNAME");
        }
        return new DriverManagerDataSource(url, username, System.getenv("DB_PASSWORD"));
    }
}
