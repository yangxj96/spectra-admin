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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Grant Boundary 的授权等级、Permission 隔离和自授权约束测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
class GrantBoundaryPolicyTest {

    private static final UUID OPERATOR = UUID.fromString("00000000-0000-0000-0000-000000000011");

    private static final UUID TARGET = UUID.fromString("00000000-0000-0000-0000-000000000012");

    private static final UUID DEPARTMENT = UUID.fromString("00000000-0000-0000-0000-000000000013");

    @Test
    void higherAuthorityCanGrantWithinItsPermissionBoundary() {
        var boundary = rulesBoundary("user:read");
        var snapshot = AuthorizationSnapshot.of(List.of(assignment(5, Map.of("user:read", boundary))));

        assertDoesNotThrow(() -> GrantBoundaryPolicy.assertAllowed(snapshot, OPERATOR, TARGET,
                List.of(request("user:read", 3)), false));
    }

    @Test
    void sameAuthorityAndSelfAuthorizationAreRejected() {
        var snapshot = AuthorizationSnapshot.of(List.of(assignment(5, Map.of("user:read", rulesBoundary("user:read")))));

        assertThrows(GrantBoundaryViolationException.class, () -> GrantBoundaryPolicy.assertAllowed(snapshot, OPERATOR,
                TARGET, List.of(request("user:read", 5)), false));
        assertThrows(GrantBoundaryViolationException.class, () -> GrantBoundaryPolicy.assertAllowed(snapshot, OPERATOR,
                OPERATOR, List.of(request("user:read", 3)), false));
    }

    @Test
    void permissionBoundariesCannotBeBorrowedAcrossPermissions() {
        var snapshot = AuthorizationSnapshot.of(List.of(assignment(5,
                Map.of("user:read", rulesBoundary("user:read")))));

        assertThrows(GrantBoundaryViolationException.class, () -> GrantBoundaryPolicy.assertAllowed(snapshot, OPERATOR,
                TARGET, List.of(request("role:assign", 3)), false));
    }

    @Test
    void rootStillUsesTheUnifiedEntryPoint() {
        var snapshot = AuthorizationSnapshot.of(List.of(assignment(1, Map.of())));

        assertDoesNotThrow(() -> GrantBoundaryPolicy.assertAllowed(snapshot, OPERATOR, OPERATOR,
                List.of(request("role:assign", 99)), true));
    }

    private static AuthorizationAssignment assignment(int authorityLevel,
                                                      Map<String, PermissionBoundary> grantBoundaries) {
        return new AuthorizationAssignment(UUID.randomUUID(), "ROLE_MANAGER", authorityLevel, Map.of(), grantBoundaries);
    }

    private static PermissionBoundary rulesBoundary(String permission) {
        return new PermissionBoundary(permission, new AuthorizationScope(ScopeMode.RULES, Set.of(DEPARTMENT), false));
    }

    private static AuthorizationGrantRequest request(String permission, int targetAuthorityLevel) {
        return new AuthorizationGrantRequest(permission, AuthorizationScope.of(ScopeMode.NONE), null,
                targetAuthorityLevel);
    }
}
