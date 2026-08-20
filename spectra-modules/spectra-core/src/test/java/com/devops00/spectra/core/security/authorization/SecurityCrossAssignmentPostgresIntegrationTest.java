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

package com.devops00.spectra.core.security.authorization;

import com.devops00.spectra.core.security.authorization.service.impl.JdbcAuthorizationSnapshotLoader;
import com.devops00.spectra.security.base.change.SecuritySessionRevocationPort;
import com.devops00.spectra.security.base.authorization.ScopeQuery;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentGrantBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentPermissionBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationScopeMapper;
import com.devops00.spectra.core.security.authorization.mapper.PermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleGrantablePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RolePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.ScopeRuleMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.annotation.Resource;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 真实 PostgreSQL 上验证同一用户的多个 RoleAssignment 不会交叉组合 Permission 与 Boundary。
 * <p>
 * 该测试默认禁用，只接受专用、可丢弃的 Flyway 测试数据库连接；启用开关与
 * {@link SecurityFlywayPostgresIntegrationTest} 共用。
 */
@EnabledIfEnvironmentVariable(named = "SPECTRA_SECURITY_FLYWAY_POSTGRES_TEST", matches = "true")
@SpringBootTest(classes = SecurityCrossAssignmentPostgresIntegrationTest.TestApplication.class, properties = {
        "spring.autoconfigure.exclude="
                + "com.devops00.spectra.core.CoreModule,"
                + "com.devops00.spectra.framework.FrameworkModule"
})
class SecurityCrossAssignmentPostgresIntegrationTest {

    private static final String ACCESS_PERMISSION = "cross:access";

    private static final String GRANT_PERMISSION = "cross:grant";

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private JdbcAuthorizationSnapshotLoader authorizationSnapshotLoader;

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @MapperScan("com.devops00.spectra.core.security.authorization.mapper")
    static class TestApplication {

        @Bean
        JdbcAuthorizationSnapshotLoader authorizationSnapshotLoader(
                                                                    RoleAssignmentMapper roleAssignmentMapper,
                                                                    SecurityRoleMapper securityRoleMapper,
                                                                    RolePermissionMapper rolePermissionMapper,
                                                                    RoleGrantablePermissionMapper roleGrantablePermissionMapper,
                                                                    PermissionMapper permissionMapper,
                                                                    AuthorizationScopeMapper authorizationScopeMapper,
                                                                    ScopeRuleMapper scopeRuleMapper,
                                                                    AssignmentPermissionBoundaryMapper permissionBoundaryMapper,
                                                                    AssignmentGrantBoundaryMapper grantBoundaryMapper) {
            return new JdbcAuthorizationSnapshotLoader(
                    roleAssignmentMapper,
                    securityRoleMapper,
                    rolePermissionMapper,
                    roleGrantablePermissionMapper,
                    permissionMapper,
                    authorizationScopeMapper,
                    scopeRuleMapper,
                    permissionBoundaryMapper,
                    grantBoundaryMapper);
        }

        @Bean
        SecuritySessionRevocationPort securitySessionRevocationPort() {
            return ignored -> {
                // 集成测试不创建安全 Session，因此无需撤销外部 Session。
            };
        }
    }

    @Test
    void keepsAccessAndGrantBoundariesWithinTheirAssignments() {
        UUID userId = UUID.randomUUID();
        UUID otherOwnerId = UUID.randomUUID();
        UUID departmentA = UUID.randomUUID();
        UUID departmentB = UUID.randomUUID();
        UUID roleA = UUID.randomUUID();
        UUID roleB = UUID.randomUUID();
        UUID accessPermissionId = UUID.randomUUID();
        UUID grantPermissionId = UUID.randomUUID();
        UUID assignmentA = UUID.randomUUID();
        UUID assignmentB = UUID.randomUUID();
        UUID accessSelfScope = UUID.randomUUID();
        UUID accessDepartmentScope = UUID.randomUUID();
        UUID grantDepartmentScope = UUID.randomUUID();
        UUID grantSelfScope = UUID.randomUUID();
        UUID accessDepartmentRule = UUID.randomUUID();
        UUID grantDepartmentRule = UUID.randomUUID();

        try {
            insertDepartment(departmentA, "cross-a");
            insertDepartment(departmentB, "cross-b");
            insertUser(userId, "cross-assignment-user");
            insertUser(otherOwnerId, "cross-assignment-owner");

            insertRole(roleA, "ROLE_CROSS_ASSIGNMENT_A");
            insertRole(roleB, "ROLE_CROSS_ASSIGNMENT_B");
            insertPermission(accessPermissionId, ACCESS_PERMISSION, "access");
            insertPermission(grantPermissionId, GRANT_PERMISSION, "grant");

            insertRolePermission(roleA, accessPermissionId);
            insertRolePermission(roleB, grantPermissionId);
            insertRoleGrantablePermission(roleA, grantPermissionId);
            insertRoleGrantablePermission(roleB, accessPermissionId);
            insertAssignment(assignmentA, userId, roleA);
            insertAssignment(assignmentB, userId, roleB);

            insertScope(accessSelfScope, "SELF", null);
            insertScope(accessDepartmentScope, "RULES", "cross");
            insertScope(grantDepartmentScope, "RULES", "cross");
            insertScope(grantSelfScope, "SELF", null);
            insertDepartmentRule(accessDepartmentRule, accessDepartmentScope, departmentB);
            insertDepartmentRule(grantDepartmentRule, grantDepartmentScope, departmentA);

            insertAccessBoundary(assignmentA, accessPermissionId, accessSelfScope);
            insertAccessBoundary(assignmentB, grantPermissionId, accessDepartmentScope);
            insertGrantBoundary(assignmentA, grantPermissionId, grantDepartmentScope);
            insertGrantBoundary(assignmentB, accessPermissionId, grantSelfScope);

            var snapshot = authorizationSnapshotLoader.load(userId);

            assertThat(snapshot.assignments()).hasSize(2);
            assertThat(snapshot.hasPermission(ACCESS_PERMISSION)).isTrue();
            assertThat(snapshot.hasPermission(GRANT_PERMISSION)).isTrue();

            assertThat(snapshot.canAccess(ACCESS_PERMISSION, selfQuery(userId, userId))).isTrue();
            assertThat(snapshot.canAccess(ACCESS_PERMISSION, departmentQuery(userId, otherOwnerId, departmentB)))
                    .isFalse();
            assertThat(snapshot.canAccess(GRANT_PERMISSION, departmentQuery(userId, otherOwnerId, departmentB)))
                    .isTrue();
            assertThat(snapshot.canAccess(GRANT_PERMISSION, selfQuery(userId, userId, departmentA))).isFalse();

            assertThat(snapshot.canGrant(GRANT_PERMISSION, departmentQuery(userId, otherOwnerId, departmentA))).isTrue();
            assertThat(snapshot.canGrant(GRANT_PERMISSION, selfQuery(userId, userId, departmentB))).isFalse();
            assertThat(snapshot.canGrant(ACCESS_PERMISSION, selfQuery(userId, userId))).isTrue();
            assertThat(snapshot.canGrant(ACCESS_PERMISSION, departmentQuery(userId, otherOwnerId, departmentA)))
                    .isFalse();
        } finally {
            deleteFixture(assignmentA, assignmentB, roleA, roleB, accessPermissionId, grantPermissionId,
                    accessSelfScope, accessDepartmentScope, grantDepartmentScope, grantSelfScope,
                    accessDepartmentRule, grantDepartmentRule, userId, otherOwnerId, departmentA, departmentB);
        }
    }

    private void insertDepartment(UUID id, String code) {
        jdbcTemplate.update("""
                INSERT INTO spectra_core.sys_department (id, name, code, created_at, updated_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, id, code, code);
    }

    private void insertUser(UUID id, String username) {
        jdbcTemplate.update("INSERT INTO spectra_core.sys_user (id, username, status) VALUES (?, ?, 'ACTIVE')", id,
                username);
    }

    private void insertRole(UUID id, String code) {
        jdbcTemplate.update("""
                INSERT INTO spectra_security.sec_role (id, code, name, authority_level, state, role_kind)
                VALUES (?, ?, ?, 10, 'ACTIVE', 'BUSINESS')
                """, id, code, code);
    }

    private void insertPermission(UUID id, String code, String action) {
        jdbcTemplate.update("""
                INSERT INTO spectra_security.sec_permission
                    (id, code, name, resource_code, action_code, allowed_scope_modes, state)
                VALUES (?, ?, ?, 'cross', ?, 'SELF,RULES', 'ACTIVE')
                """, id, code, code, action);
    }

    private void insertRolePermission(UUID roleId, UUID permissionId) {
        jdbcTemplate.update("""
                INSERT INTO spectra_security.sec_role_permission (role_id, permission_id)
                VALUES (?, ?)
                """, roleId, permissionId);
    }

    private void insertRoleGrantablePermission(UUID roleId, UUID permissionId) {
        jdbcTemplate.update("""
                INSERT INTO spectra_security.sec_role_grantable_permission (role_id, permission_id)
                VALUES (?, ?)
                """, roleId, permissionId);
    }

    private void insertAssignment(UUID id, UUID userId, UUID roleId) {
        jdbcTemplate.update("""
                INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id, state)
                VALUES (?, ?, ?, 'ACTIVE')
                """, id, userId, roleId);
    }

    private void insertScope(UUID id, String mode, String resourceCode) {
        jdbcTemplate.update("""
                INSERT INTO spectra_security.sec_authorization_scope (id, scope_mode, resource_code)
                VALUES (?, ?, ?)
                """, id, mode, resourceCode);
    }

    private void insertDepartmentRule(UUID id, UUID scopeId, UUID departmentId) {
        jdbcTemplate.update("""
                INSERT INTO spectra_security.sec_scope_rule (id, scope_id, rule_type, department_id)
                VALUES (?, ?, 'DEPARTMENT', ?)
                """, id, scopeId, departmentId);
    }

    private void insertAccessBoundary(UUID assignmentId, UUID permissionId, UUID scopeId) {
        jdbcTemplate.update("""
                INSERT INTO spectra_security.sec_assignment_permission_boundary (assignment_id, permission_id, scope_id)
                VALUES (?, ?, ?)
                """, assignmentId, permissionId, scopeId);
    }

    private void insertGrantBoundary(UUID assignmentId, UUID permissionId, UUID scopeId) {
        jdbcTemplate.update("""
                INSERT INTO spectra_security.sec_assignment_grant_boundary (assignment_id, permission_id, scope_id)
                VALUES (?, ?, ?)
                """, assignmentId, permissionId, scopeId);
    }

    private ScopeQuery selfQuery(UUID subjectId, UUID ownerId) {
        return new ScopeQuery(subjectId, ownerId, null, Set.of());
    }

    private ScopeQuery selfQuery(UUID subjectId, UUID ownerId, UUID departmentId) {
        return new ScopeQuery(subjectId, ownerId, departmentId, Set.of(departmentId));
    }

    private ScopeQuery departmentQuery(UUID subjectId, UUID ownerId, UUID departmentId) {
        return new ScopeQuery(subjectId, ownerId, departmentId, Set.of(departmentId));
    }

    private void deleteFixture(UUID assignmentA, UUID assignmentB, UUID roleA, UUID roleB,
                               UUID accessPermissionId, UUID grantPermissionId, UUID accessSelfScope,
                               UUID accessDepartmentScope, UUID grantDepartmentScope, UUID grantSelfScope,
                               UUID accessDepartmentRule, UUID grantDepartmentRule, UUID userId,
                               UUID otherOwnerId, UUID departmentA, UUID departmentB) {
        jdbcTemplate.update("DELETE FROM spectra_security.sec_assignment_permission_boundary WHERE assignment_id IN (?, ?)",
                assignmentA, assignmentB);
        jdbcTemplate.update("DELETE FROM spectra_security.sec_assignment_grant_boundary WHERE assignment_id IN (?, ?)",
                assignmentA, assignmentB);
        jdbcTemplate.update("DELETE FROM spectra_security.sec_role_assignment WHERE id IN (?, ?)", assignmentA,
                assignmentB);
        jdbcTemplate.update("DELETE FROM spectra_security.sec_role_permission WHERE role_id IN (?, ?)", roleA, roleB);
        jdbcTemplate.update("DELETE FROM spectra_security.sec_role_grantable_permission WHERE role_id IN (?, ?)", roleA,
                roleB);
        jdbcTemplate.update("DELETE FROM spectra_security.sec_scope_rule WHERE id IN (?, ?)", accessDepartmentRule,
                grantDepartmentRule);
        jdbcTemplate.update("DELETE FROM spectra_security.sec_authorization_scope WHERE id IN (?, ?, ?, ?)",
                accessSelfScope, accessDepartmentScope, grantDepartmentScope, grantSelfScope);
        jdbcTemplate.update("DELETE FROM spectra_security.sec_role WHERE id IN (?, ?)", roleA, roleB);
        jdbcTemplate.update("DELETE FROM spectra_security.sec_permission WHERE id IN (?, ?)", accessPermissionId,
                grantPermissionId);
        jdbcTemplate.update("DELETE FROM spectra_core.sys_user WHERE id IN (?, ?)", userId, otherOwnerId);
        jdbcTemplate.update("DELETE FROM spectra_core.sys_department WHERE id IN (?, ?)", departmentA, departmentB);
    }
}
