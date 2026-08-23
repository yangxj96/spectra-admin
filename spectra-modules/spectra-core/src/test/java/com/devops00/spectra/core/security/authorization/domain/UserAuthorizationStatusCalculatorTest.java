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

package com.devops00.spectra.core.security.authorization.domain;

import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationAssignmentView;
import com.devops00.spectra.security.base.root.RootAuthorizationPolicy;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserAuthorizationStatusCalculatorTest {

    private static final Instant NOW = Instant.parse("2026-08-21T00:00:00Z");

    @Test
    void returnsUnconfiguredWhenUserHasNoAssignments() {
        assertThat(UserAuthorizationStatusCalculator.calculate(List.of(), NOW))
                .isEqualTo(UserAuthorizationStatus.UNCONFIGURED);
    }

    @Test
    void returnsIncompleteWhenEffectiveAssignmentMissesPermissionBoundary() {
        var assignment = assignment("ACTIVE", "ACTIVE", 2L, 1, null, null);

        assertThat(UserAuthorizationStatusCalculator.calculate(List.of(assignment), NOW))
                .isEqualTo(UserAuthorizationStatus.INCOMPLETE);
    }

    @Test
    void returnsActiveWhenEffectiveAssignmentHasAllPermissionBoundaries() {
        var assignment = assignment("ACTIVE", "ACTIVE", 2L, 2, null, null);

        assertThat(UserAuthorizationStatusCalculator.calculate(List.of(assignment), NOW))
                .isEqualTo(UserAuthorizationStatus.ACTIVE);
    }

    @Test
    void returnsActiveWhenEffectiveRootAssignmentHasNoPermissionBoundaries() {
        var assignment = assignment(RootAuthorizationPolicy.ROOT_ROLE, "ACTIVE", "ACTIVE", 7L, 0, null, null);

        assertThat(UserAuthorizationStatusCalculator.calculate(List.of(assignment), NOW))
                .isEqualTo(UserAuthorizationStatus.ACTIVE);
    }

    @Test
    void returnsPartialWhenOneOfMultipleAssignmentsIsInvalid() {
        var active = assignment("ACTIVE", "ACTIVE", 1L, 1, null, null);
        var expired = assignment("EXPIRED", "ACTIVE", 1L, 1, null, NOW.minusSeconds(1));

        assertThat(UserAuthorizationStatusCalculator.calculate(List.of(active, expired), NOW))
                .isEqualTo(UserAuthorizationStatus.PARTIAL);
    }

    @Test
    void returnsPartialWhenRoleIsDisabled() {
        var assignment = assignment("ACTIVE", "DISABLED", 1L, 1, null, null);

        assertThat(UserAuthorizationStatusCalculator.calculate(List.of(assignment), NOW))
                .isEqualTo(UserAuthorizationStatus.PARTIAL);
    }

    @Test
    void ignoresRevokedHistoricalAssignmentWhenCurrentAssignmentIsComplete() {
        var active = assignment("ACTIVE", "ACTIVE", 1L, 1, null, null);
        var revoked = assignment("REVOKED", "ACTIVE", 1L, 1, null, NOW.minusSeconds(1));

        assertThat(UserAuthorizationStatusCalculator.calculate(List.of(active, revoked), NOW))
                .isEqualTo(UserAuthorizationStatus.ACTIVE);
    }

    @Test
    void returnsUnconfiguredWhenAllAssignmentsAreRevoked() {
        var revoked = assignment("REVOKED", "ACTIVE", 1L, 1, null, NOW.minusSeconds(1));

        assertThat(UserAuthorizationStatusCalculator.calculate(List.of(revoked), NOW))
                .isEqualTo(UserAuthorizationStatus.UNCONFIGURED);
    }

    private static AuthorizationAssignmentView assignment(
                                                          String state,
                                                          String roleState,
                                                          long rolePermissionCount,
                                                          int boundaryCount,
                                                          Instant validFrom,
                                                          Instant validUntil) {
        return assignment("ROLE_TEST", state, roleState, rolePermissionCount, boundaryCount, validFrom, validUntil);
    }

    private static AuthorizationAssignmentView assignment(
                                                          String roleCode,
                                                          String state,
                                                          String roleState,
                                                          long rolePermissionCount,
                                                          int boundaryCount,
                                                          Instant validFrom,
                                                          Instant validUntil) {
        var boundaries = java.util.stream.IntStream.range(0, boundaryCount)
                .mapToObj(index -> new com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationBoundaryView(
                        "permission:" + index, "NONE", null, List.of()))
                .toList();
        return new AuthorizationAssignmentView(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                roleCode,
                "BUSINESS",
                "测试角色",
                false,
                roleState,
                1L,
                rolePermissionCount,
                1L,
                state,
                validFrom,
                validUntil,
                boundaries,
                List.of());
    }
}
