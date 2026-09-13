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

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.devops00.spectra.core.security.authorization.javabean.entity.AssignmentGrantBoundary;
import com.devops00.spectra.core.security.authorization.javabean.entity.AssignmentPermissionBoundary;
import com.devops00.spectra.core.security.authorization.javabean.entity.AuthorizationScope;
import com.devops00.spectra.core.security.authorization.javabean.entity.Permission;
import com.devops00.spectra.core.security.authorization.javabean.entity.RoleAssignment;
import com.devops00.spectra.core.security.authorization.javabean.entity.RoleGrantablePermission;
import com.devops00.spectra.core.security.authorization.javabean.entity.RolePermission;
import com.devops00.spectra.core.security.authorization.javabean.entity.ScopeRule;
import com.devops00.spectra.core.security.authorization.javabean.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentGrantBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentPermissionBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationScopeMapper;
import com.devops00.spectra.core.security.authorization.mapper.PermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleGrantablePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RolePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.ScopeRuleMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.common.security.authorization.ScopeQuery;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证 {@code JdbcAuthorizationSnapshotLoaderTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class JdbcAuthorizationSnapshotLoaderTest {

    private final RoleAssignmentMapper roleAssignmentMapper = mock(RoleAssignmentMapper.class);

    private final SecurityRoleMapper securityRoleMapper = mock(SecurityRoleMapper.class);

    private final RolePermissionMapper rolePermissionMapper = mock(RolePermissionMapper.class);

    private final RoleGrantablePermissionMapper roleGrantablePermissionMapper = mock(RoleGrantablePermissionMapper.class);

    private final PermissionMapper permissionMapper = mock(PermissionMapper.class);

    private final AuthorizationScopeMapper authorizationScopeMapper = mock(AuthorizationScopeMapper.class);

    private final ScopeRuleMapper scopeRuleMapper = mock(ScopeRuleMapper.class);

    private final AssignmentPermissionBoundaryMapper permissionBoundaryMapper = mock(AssignmentPermissionBoundaryMapper.class);

    private final AssignmentGrantBoundaryMapper grantBoundaryMapper = mock(AssignmentGrantBoundaryMapper.class);

    @Test
    void keepsPermissionAndScopeBoundToTheSameAssignment() {
        var userId = UUID.randomUUID();
        var roleId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();
        var readPermissionId = UUID.randomUUID();
        var grantPermissionId = UUID.randomUUID();
        var readScopeId = UUID.randomUUID();
        var grantScopeId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();

        var assignment = assignment(assignmentId, userId, roleId);
        var role = role(roleId, "ROLE_MANAGER");
        var readPermission = permission(readPermissionId, "order:read");
        var grantPermission = permission(grantPermissionId, "user:read");
        var accessBoundary = accessBoundary(assignmentId, readPermissionId, readScopeId);
        var grantBoundary = grantBoundary(assignmentId, grantPermissionId, grantScopeId);
        var readScope = scope(readScopeId, "SELF");
        var grantScope = scope(grantScopeId, "RULES");
        var rule = rule(grantScopeId, departmentId);

        when(roleAssignmentMapper.selectList(any(Wrapper.class))).thenReturn(List.of(assignment));
        when(securityRoleMapper.selectBatchIds(anyCollection())).thenReturn(List.of(role));
        when(rolePermissionMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(rolePermission(roleId, readPermissionId)));
        when(roleGrantablePermissionMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(roleGrantablePermission(roleId, grantPermissionId)));
        when(permissionBoundaryMapper.selectList(any(Wrapper.class))).thenReturn(List.of(accessBoundary));
        when(grantBoundaryMapper.selectList(any(Wrapper.class))).thenReturn(List.of(grantBoundary));
        when(permissionMapper.selectBatchIds(anyCollection())).thenReturn(List.of(readPermission, grantPermission));
        when(authorizationScopeMapper.selectBatchIds(anyCollection())).thenReturn(List.of(readScope, grantScope));
        when(scopeRuleMapper.selectList(any(Wrapper.class))).thenReturn(List.of(rule));

        var snapshot = loader().load(userId);

        assertThat(snapshot.hasPermission("order:read")).isTrue();
        assertThat(snapshot.hasPermission("user:read")).isFalse();
        assertThat(snapshot.grantablePermissions()).containsExactly("user:read");
        assertThat(snapshot.canAccess("order:read", new ScopeQuery(
                userId, userId, null, Set.of()))).isTrue();
        assertThat(snapshot.canGrant("user:read", new ScopeQuery(
                UUID.randomUUID(), null, departmentId, Set.of(departmentId)))).isTrue();
    }

    @Test
    void rejectsBoundaryForPermissionNotDeclaredByRole() {
        var userId = UUID.randomUUID();
        var roleId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();
        var declaredPermissionId = UUID.randomUUID();
        var undeclaredPermissionId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();

        when(roleAssignmentMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(assignment(assignmentId, userId, roleId)));
        when(securityRoleMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(role(roleId, "ROLE_MANAGER")));
        when(rolePermissionMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(rolePermission(roleId, declaredPermissionId)));
        when(roleGrantablePermissionMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(permissionBoundaryMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(accessBoundary(assignmentId, undeclaredPermissionId, scopeId)));
        when(grantBoundaryMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(permissionMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(permission(undeclaredPermissionId, "order:read")));
        when(authorizationScopeMapper.selectBatchIds(anyCollection())).thenReturn(List.of(scope(scopeId, "SELF")));
        when(scopeRuleMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        assertThatThrownBy(() -> loader().load(userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Role 未声明");
    }

    /**
     * 处理授权快照相关数据。
     */
    private JdbcAuthorizationSnapshotLoader loader() {
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

    /**
     * 处理分配相关数据。
     */
    private static RoleAssignment assignment(UUID id, UUID userId, UUID roleId) {
        var value = new RoleAssignment();
        value.setId(id);
        value.setUserId(userId);
        value.setRoleId(roleId);
        value.setState("ACTIVE");
        return value;
    }

    /**
     * 处理角色相关数据。
     */
    private static SecurityRole role(UUID id, String code) {
        var value = new SecurityRole();
        value.setId(id);
        value.setCode(code);
        value.setState("ACTIVE");
        return value;
    }

    /**
     * 处理权限相关数据。
     */
    private static Permission permission(UUID id, String code) {
        var value = new Permission();
        value.setId(id);
        value.setCode(code);
        value.setState("ACTIVE");
        return value;
    }

    /**
     * 处理角色权限相关数据。
     */
    private static RolePermission rolePermission(UUID roleId, UUID permissionId) {
        var value = new RolePermission();
        value.setRoleId(roleId);
        value.setPermissionId(permissionId);
        return value;
    }

    /**
     * 处理角色权限相关数据。
     */
    private static RoleGrantablePermission roleGrantablePermission(UUID roleId, UUID permissionId) {
        var value = new RoleGrantablePermission();
        value.setRoleId(roleId);
        value.setPermissionId(permissionId);
        return value;
    }

    /**
     * 处理访问边界相关数据。
     */
    private static AssignmentPermissionBoundary accessBoundary(UUID assignmentId, UUID permissionId, UUID scopeId) {
        var value = new AssignmentPermissionBoundary();
        value.setAssignmentId(assignmentId);
        value.setPermissionId(permissionId);
        value.setScopeId(scopeId);
        return value;
    }

    /**
     * 处理边界相关数据。
     */
    private static AssignmentGrantBoundary grantBoundary(UUID assignmentId, UUID permissionId, UUID scopeId) {
        var value = new AssignmentGrantBoundary();
        value.setAssignmentId(assignmentId);
        value.setPermissionId(permissionId);
        value.setScopeId(scopeId);
        return value;
    }

    /**
     * 处理范围相关数据。
     */
    private static AuthorizationScope scope(
                                            UUID id, String mode) {
        var value = new AuthorizationScope();
        value.setId(id);
        value.setScopeMode(mode);
        return value;
    }

    /**
     * 处理规则相关数据。
     */
    private static ScopeRule rule(UUID scopeId, UUID departmentId) {
        var value = new ScopeRule();
        value.setId(UUID.randomUUID());
        value.setScopeId(scopeId);
        value.setRuleType("DEPARTMENT");
        value.setDepartmentId(departmentId);
        value.setIncludeDescendants(false);
        return value;
    }
}
