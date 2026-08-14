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

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Grant Boundary 的 Scope 包含关系测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
class ScopeContainsTest {

    private static final UUID ROOT = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private static final UUID CHILD = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Test
    void allContainsEveryRequestedScopeAndNoneIsOnlyContainedByNoneOrAll() {
        assertTrue(ScopeContains.contains(AuthorizationScope.of(ScopeMode.ALL), rules(CHILD, true)));
        assertTrue(ScopeContains.contains(AuthorizationScope.of(ScopeMode.ALL), AuthorizationScope.of(ScopeMode.NONE)));
        assertTrue(ScopeContains.contains(AuthorizationScope.of(ScopeMode.NONE), AuthorizationScope.of(ScopeMode.NONE)));
        assertFalse(ScopeContains.contains(AuthorizationScope.of(ScopeMode.NONE), AuthorizationScope.of(ScopeMode.SELF)));
    }

    @Test
    void descendantBoundaryMustContainRequestedDescendantBoundary() {
        var hierarchy = (DepartmentHierarchy) (ancestor, descendant) -> ROOT.equals(ancestor) && CHILD.equals(descendant);

        assertTrue(ScopeContains.contains(rules(ROOT, true), rules(CHILD, false), hierarchy));
        assertFalse(ScopeContains.contains(rules(ROOT, false), rules(CHILD, false), hierarchy));
        assertFalse(ScopeContains.contains(rules(ROOT, false), rules(ROOT, true), hierarchy));
        assertFalse(ScopeContains.contains(rules(ROOT, true), rules(OTHER, false), hierarchy));
    }

    @Test
    void selfBoundaryDoesNotExpandToRules() {
        assertTrue(ScopeContains.contains(AuthorizationScope.of(ScopeMode.SELF),
                AuthorizationScope.of(ScopeMode.SELF)));
        assertFalse(ScopeContains.contains(AuthorizationScope.of(ScopeMode.SELF), rules(ROOT, false)));
    }

    private static AuthorizationScope rules(UUID departmentId, boolean includeDescendants) {
        return new AuthorizationScope(ScopeMode.RULES, Set.of(departmentId), includeDescendants);
    }
}
