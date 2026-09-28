package com.devops00.spectra.core.security.authorization;

import com.devops00.spectra.common.security.authorization.AuthorizationAssignment;
import com.devops00.spectra.common.security.authorization.AuthorizationScope;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.common.security.authorization.PermissionBoundary;
import com.devops00.spectra.common.security.authorization.ScopeMode;
import com.devops00.spectra.core.system.javabean.entity.Department;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationDepartmentScopeTest {

    private static final UUID VIEWER = id(90);
    private static final UUID D09 = id(9);
    private static final UUID D10 = id(10);
    private static final UUID D11 = id(11);
    private static final UUID D12 = id(12);

    @Test
    void rulesScopeMustIntersectConfiguredDepartmentsWithViewerMembership() {
        var snapshot = snapshot("user:read", ScopeMode.RULES, Set.of(D09, D10, D11), false,
                Set.of(D09, D10));

        var visible = AuthorizationDepartmentScope.visibleDepartmentIds(snapshot, "user:read", departments());

        assertEquals(Set.of(D09, D10), visible);
    }

    @Test
    void userRowsMatchPrimaryOrAssociatedDepartmentsButNeverAnOutOfScopeDepartment() {
        var snapshot = snapshot("user:read", ScopeMode.RULES, Set.of(D09, D10), false,
                Set.of(D09, D10));
        var departments = departments();

        assertTrue(AuthorizationDepartmentScope.canAccessUser(snapshot, "user:read", VIEWER,
                id(101), Set.of(D10), departments));
        assertFalse(AuthorizationDepartmentScope.canAccessUser(snapshot, "user:read", VIEWER,
                id(102), Set.of(D11), departments));
        assertFalse(AuthorizationDepartmentScope.canAccessUser(snapshot, "user:read", VIEWER,
                id(103), Set.of(), departments));
    }

    @Test
    void descendantRulesExposeOnlyDescendantsWhosePathIntersectsViewerMembership() {
        var snapshot = snapshot("department:read", ScopeMode.RULES, Set.of(D09), true, Set.of(D09));
        var departments = List.of(department(D09, null), department(D10, D09), department(D11, null));

        var visible = AuthorizationDepartmentScope.visibleDepartmentIds(snapshot, "department:read", departments);

        assertEquals(Set.of(D09, D10), visible);
    }

    @Test
    void treeIncludesAncestorsNeededToRenderAuthorizedDepartment() {
        var snapshot = snapshot("department:read", ScopeMode.RULES, Set.of(D10), false, Set.of(D10));
        var departments = List.of(department(D09, null), department(D10, D09), department(D11, D09));

        var visible = AuthorizationDepartmentScope.treeDepartmentIds(snapshot, "department:read", departments);

        assertEquals(Set.of(D09, D10), visible);
        assertFalse(visible.contains(D11));
    }

    @Test
    void allAndSelfModesAreResolvedWithoutInventingDepartmentMembership() {
        var all = snapshot("user:read", ScopeMode.ALL, Set.of(), false, Set.of());
        var self = snapshot("user:read", ScopeMode.SELF, Set.of(), false, Set.of());

        assertTrue(AuthorizationDepartmentScope.isUnrestricted(all, "user:read"));
        assertFalse(AuthorizationDepartmentScope.isUnrestricted(self, "user:read"));
        assertTrue(AuthorizationDepartmentScope.allowsOwnUser(self, "user:read", VIEWER, VIEWER));
        assertFalse(AuthorizationDepartmentScope.allowsOwnUser(self, "user:read", VIEWER, id(91)));
        assertFalse(AuthorizationDepartmentScope.canAccessUser(self, "user:read", VIEWER,
                id(91), Set.of(D09), departments()));
        assertEquals(Set.of(), AuthorizationDepartmentScope.visibleDepartmentIds(self, "user:read", departments()));
    }

    private static AuthorizationSnapshot snapshot(String permission, ScopeMode mode, Set<UUID> departments,
                                                  boolean includeDescendants, Set<UUID> memberships) {
        var scope = mode == ScopeMode.RULES
                ? new AuthorizationScope(mode, departments, includeDescendants)
                : AuthorizationScope.of(mode);
        var boundary = new PermissionBoundary(permission, scope);
        var assignment = new AuthorizationAssignment(id(100), "ROLE_ADMIN_SYSTEM", 2,
                Map.of(permission, boundary), Map.of());
        return AuthorizationSnapshot.of(List.of(assignment), memberships);
    }

    private static List<Department> departments() {
        return List.of(department(D09, null), department(D10, null), department(D11, null), department(D12, null));
    }

    private static Department department(UUID id, UUID parentId) {
        var department = new Department();
        department.setId(id);
        department.setPid(parentId);
        return department;
    }

    private static UUID id(long value) {
        return new UUID(0, value);
    }
}
