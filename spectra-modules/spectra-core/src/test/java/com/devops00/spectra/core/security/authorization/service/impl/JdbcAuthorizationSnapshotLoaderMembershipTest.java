package com.devops00.spectra.core.security.authorization.service.impl;

import com.devops00.spectra.core.security.authorization.mapper.AssignmentGrantBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentPermissionBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.PermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleGrantablePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RolePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.ScopeRuleMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationScopeMapper;
import com.devops00.spectra.core.user.mapper.UserDepartmentMembershipMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JdbcAuthorizationSnapshotLoaderMembershipTest {

    @Mock
    private RoleAssignmentMapper roleAssignmentMapper;

    @Mock
    private SecurityRoleMapper securityRoleMapper;

    @Mock
    private RolePermissionMapper rolePermissionMapper;

    @Mock
    private RoleGrantablePermissionMapper roleGrantablePermissionMapper;

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private AuthorizationScopeMapper authorizationScopeMapper;

    @Mock
    private ScopeRuleMapper scopeRuleMapper;

    @Mock
    private AssignmentPermissionBoundaryMapper permissionBoundaryMapper;

    @Mock
    private AssignmentGrantBoundaryMapper grantBoundaryMapper;

    @Mock
    private UserDepartmentMembershipMapper userDepartmentMembershipMapper;

    @InjectMocks
    private JdbcAuthorizationSnapshotLoader loader;

    @Test
    void loadsPrimaryAndAssociatedDepartmentsIntoSnapshot() {
        var userId = UUID.randomUUID();
        var primary = UUID.randomUUID();
        var associated = UUID.randomUUID();
        when(userDepartmentMembershipMapper.selectDepartmentIdsForAuthorization(userId))
                .thenReturn(List.of(primary, associated, primary));
        when(roleAssignmentMapper.selectList(any())).thenReturn(List.of());

        var snapshot = loader.load(userId);

        assertEquals(java.util.Set.of(primary, associated), snapshot.departmentMembershipIds());
    }

    @Test
    void membershipLoadFailureRejectsSnapshot() {
        var userId = UUID.randomUUID();
        when(userDepartmentMembershipMapper.selectDepartmentIdsForAuthorization(userId))
                .thenThrow(new IllegalStateException("membership query failed"));

        assertThrows(IllegalStateException.class, () -> loader.load(userId));
    }
}
