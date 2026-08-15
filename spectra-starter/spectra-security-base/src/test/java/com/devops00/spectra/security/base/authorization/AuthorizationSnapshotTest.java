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

package com.devops00.spectra.security.base.authorization;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationSnapshotTest {

    private static final UUID JAVA = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private static final UUID FRONTEND = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private static final UUID OTHER_USER = UUID.fromString("00000000-0000-0000-0000-000000000003");

    private static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-000000000004");

    @Test
    void rootAssignmentHasImplicitPermissionsAndGlobalAccess() {
        var snapshot = AuthorizationSnapshot.of(List.of(
                new AuthorizationAssignment(UUID.randomUUID(), "ROLE_DEV_OPS", Map.of(), Map.of())));

        assertTrue(snapshot.isRoot());
        assertEquals(Set.of("*"), snapshot.permissions());
        assertEquals(Set.of("*"), snapshot.grantablePermissions());
        assertTrue(snapshot.hasPermission("system:secret:delete"));
        assertTrue(snapshot.canAccess("system:secret:read", query(OTHER_USER, OWNER, OTHER_USER)));
        assertTrue(snapshot.canGrant("system:secret:read", query(OTHER_USER, OWNER, OTHER_USER)));
    }

    @Test
    void shouldUnionScopesOnlyForTheSamePermission() {
        var snapshot = AuthorizationSnapshot.of(List.of(
                new AuthorizationAssignment(
                        UUID.randomUUID(),
                        "ROLE_A",
                        Map.of(
                                "order:read", boundary("order:read", rules(JAVA, false)),
                                "salary:read", boundary("salary:read", self())),
                        Map.of()),
                new AuthorizationAssignment(
                        UUID.randomUUID(),
                        "ROLE_B",
                        Map.of(
                                "order:read", boundary("order:read", rules(FRONTEND, false)),
                                "salary:read", boundary("salary:read", rules(JAVA, false))),
                        Map.of())));

        assertTrue(snapshot.canAccess("order:read", query(OTHER_USER, OWNER, JAVA)));
        assertTrue(snapshot.canAccess("order:read", query(OTHER_USER, OWNER, FRONTEND)));
        assertFalse(snapshot.canAccess("order:read", query(OTHER_USER, OWNER, OTHER_USER)));

        // salary:read 只能使用 A 的 SELF 或 B 的 Java boundary，不能借用 order:read 的 Frontend scope。
        assertTrue(snapshot.canAccess("salary:read", query(OWNER, OWNER, OTHER_USER)));
        assertTrue(snapshot.canAccess("salary:read", query(OTHER_USER, OWNER, JAVA)));
        assertFalse(snapshot.canAccess("salary:read", query(OTHER_USER, OWNER, FRONTEND)));
    }

    @Test
    void shouldKeepGrantBoundarySeparateFromAccessBoundary() {
        var snapshot = AuthorizationSnapshot.of(List.of(
                new AuthorizationAssignment(
                        UUID.randomUUID(),
                        "ROLE_MANAGER",
                        Map.of("user:read", boundary("user:read", AuthorizationScope.of(ScopeMode.ALL))),
                        Map.of("user:read", boundary("user:read", rules(JAVA, false))))));

        assertTrue(snapshot.canAccess("user:read", query(OTHER_USER, OWNER, FRONTEND)));
        assertTrue(snapshot.canGrant("user:read", query(OTHER_USER, OWNER, JAVA)));
        assertFalse(snapshot.canGrant("user:read", query(OTHER_USER, OWNER, FRONTEND)));
    }

    @Test
    void shouldSupportDescendantRulesWithoutExpandingExplicitDepartmentSets() {
        var snapshot = AuthorizationSnapshot.of(List.of(
                new AuthorizationAssignment(UUID.randomUUID(), "ROLE_A",
                        Map.of("order:read", boundary("order:read", rules(JAVA, true))), Map.of()),
                new AuthorizationAssignment(UUID.randomUUID(), "ROLE_B",
                        Map.of("salary:read", boundary("salary:read", rules(JAVA, false))), Map.of())));

        assertTrue(snapshot.canAccess("order:read", new ScopeQuery(OTHER_USER, OWNER, FRONTEND, Set.of(FRONTEND, JAVA))));
        assertFalse(snapshot.canAccess("salary:read", new ScopeQuery(OTHER_USER, OWNER, FRONTEND, Set.of(FRONTEND, JAVA))));
    }

    private static PermissionBoundary boundary(String permission, AuthorizationScope scope) {
        return new PermissionBoundary(permission, scope);
    }

    private static AuthorizationScope rules(UUID departmentId, boolean includeDescendants) {
        return new AuthorizationScope(ScopeMode.RULES, Set.of(departmentId), includeDescendants);
    }

    private static AuthorizationScope self() {
        return new AuthorizationScope(ScopeMode.SELF, Set.of(), false);
    }

    private static ScopeQuery query(UUID subjectId, UUID ownerId, UUID departmentId) {
        return new ScopeQuery(subjectId, ownerId, departmentId, Set.of(departmentId));
    }
}
