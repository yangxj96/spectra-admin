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
import com.devops00.spectra.core.security.authorization.entity.AssignmentGrantBoundary;
import com.devops00.spectra.core.security.authorization.entity.AssignmentPermissionBoundary;
import com.devops00.spectra.core.security.authorization.entity.AuthorizationScope;
import com.devops00.spectra.core.security.authorization.entity.Permission;
import com.devops00.spectra.core.security.authorization.entity.RoleAssignment;
import com.devops00.spectra.core.security.authorization.entity.ScopeRule;
import com.devops00.spectra.core.security.authorization.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentGrantBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentPermissionBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationScopeMapper;
import com.devops00.spectra.core.security.authorization.mapper.PermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.RolePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.ScopeRuleMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthorizationAssignmentQueryServiceImplTest {

    private final RoleAssignmentMapper roleAssignmentMapper = mock(RoleAssignmentMapper.class);

    private final RolePermissionMapper rolePermissionMapper = mock(RolePermissionMapper.class);

    private final SecurityRoleMapper securityRoleMapper = mock(SecurityRoleMapper.class);

    private final AssignmentPermissionBoundaryMapper permissionBoundaryMapper = mock(AssignmentPermissionBoundaryMapper.class);

    private final AssignmentGrantBoundaryMapper grantBoundaryMapper = mock(AssignmentGrantBoundaryMapper.class);

    private final PermissionMapper permissionMapper = mock(PermissionMapper.class);

    private final AuthorizationScopeMapper authorizationScopeMapper = mock(AuthorizationScopeMapper.class);

    private final ScopeRuleMapper scopeRuleMapper = mock(ScopeRuleMapper.class);

    @Test
    void returnsAccessAndGrantBoundariesWithoutCombiningThem() {
        var userId = UUID.randomUUID();
        var roleId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();
        var accessPermissionId = UUID.randomUUID();
        var grantPermissionId = UUID.randomUUID();
        var accessScopeId = UUID.randomUUID();
        var grantScopeId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();

        var assignment = new RoleAssignment();
        assignment.setId(assignmentId);
        assignment.setUserId(userId);
        assignment.setRoleId(roleId);
        assignment.setState("ACTIVE");
        var role = new SecurityRole();
        role.setId(roleId);
        role.setCode("ROLE_MANAGER");
        role.setRoleKind("BUSINESS");
        role.setName("业务管理员");
        role.setSystemManaged(false);
        role.setVersion(3L);
        assignment.setVersion(4L);
        var accessPermission = permission(accessPermissionId, "order:read");
        var grantPermission = permission(grantPermissionId, "user:read");
        var accessBoundary = boundary(assignmentId, accessPermissionId, accessScopeId);
        var grantBoundary = grantBoundary(assignmentId, grantPermissionId, grantScopeId);
        var accessScope = scope(accessScopeId, "SELF", null);
        var grantScope = scope(grantScopeId, "RULES", "user");
        var rule = new ScopeRule();
        rule.setScopeId(grantScopeId);
        rule.setRuleType("DEPARTMENT");
        rule.setDepartmentId(departmentId);
        rule.setIncludeDescendants(true);

        when(roleAssignmentMapper.selectList(any(Wrapper.class))).thenReturn(List.of(assignment));
        when(securityRoleMapper.selectBatchIds(anyCollection())).thenReturn(List.of(role));
        when(permissionBoundaryMapper.selectList(any(Wrapper.class))).thenReturn(List.of(accessBoundary));
        when(grantBoundaryMapper.selectList(any(Wrapper.class))).thenReturn(List.of(grantBoundary));
        when(permissionMapper.selectBatchIds(anyCollection())).thenReturn(List.of(accessPermission, grantPermission));
        when(authorizationScopeMapper.selectBatchIds(anyCollection())).thenReturn(List.of(accessScope, grantScope));
        when(scopeRuleMapper.selectList(any(Wrapper.class))).thenReturn(List.of(rule));

        var result = service().findByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().roleName()).isEqualTo("业务管理员");
        assertThat(result.getFirst().roleSystemManaged()).isFalse();
        assertThat(result.getFirst().roleVersion()).isEqualTo(3L);
        assertThat(result.getFirst().version()).isEqualTo(4L);
        assertThat(result.getFirst().accessBoundaries()).extracting("permissionCode")
                .containsExactly("order:read");
        assertThat(result.getFirst().grantBoundaries()).extracting("permissionCode")
                .containsExactly("user:read");
        assertThat(result.getFirst().grantBoundaries().getFirst().rules().getFirst().departmentId())
                .isEqualTo(departmentId);
    }

    @Test
    void ignoresAssignmentsReferencingDeletedRoles() {
        var userId = UUID.randomUUID();
        var roleId = UUID.randomUUID();
        var assignment = new RoleAssignment();
        assignment.setId(UUID.randomUUID());
        assignment.setUserId(userId);
        assignment.setRoleId(roleId);
        assignment.setState("REVOKED");

        when(roleAssignmentMapper.selectList(any(Wrapper.class))).thenReturn(List.of(assignment));
        when(securityRoleMapper.selectBatchIds(anyCollection())).thenReturn(List.of());

        assertThat(service().findByUserId(userId)).isEmpty();
    }

    private AuthorizationAssignmentQueryServiceImpl service() {
        return new AuthorizationAssignmentQueryServiceImpl(
                roleAssignmentMapper,
                rolePermissionMapper,
                securityRoleMapper,
                permissionBoundaryMapper,
                grantBoundaryMapper,
                permissionMapper,
                authorizationScopeMapper,
                scopeRuleMapper);
    }

    private static Permission permission(UUID id, String code) {
        var value = new Permission();
        value.setId(id);
        value.setCode(code);
        value.setState("ACTIVE");
        return value;
    }

    private static AssignmentPermissionBoundary boundary(UUID assignmentId, UUID permissionId, UUID scopeId) {
        var value = new AssignmentPermissionBoundary();
        value.setAssignmentId(assignmentId);
        value.setPermissionId(permissionId);
        value.setScopeId(scopeId);
        return value;
    }

    private static AssignmentGrantBoundary grantBoundary(UUID assignmentId, UUID permissionId, UUID scopeId) {
        var value = new AssignmentGrantBoundary();
        value.setAssignmentId(assignmentId);
        value.setPermissionId(permissionId);
        value.setScopeId(scopeId);
        return value;
    }

    private static AuthorizationScope scope(
                                            UUID id, String mode, String resourceCode) {
        var value = new AuthorizationScope();
        value.setId(id);
        value.setScopeMode(mode);
        value.setResourceCode(resourceCode);
        return value;
    }
}
