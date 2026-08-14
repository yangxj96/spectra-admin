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

package com.devops00.spectra.core.authorization;

import com.devops00.spectra.core.authorization.domain.RoleAuthorizationState;
import com.devops00.spectra.core.authorization.service.RoleChangeImpactAnalyzer;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Role 变更影响分析测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
class RoleChangeImpactAnalyzerTest {

    @Test
    void reportsPermissionGrantableAndAuthorityExpansion() {
        var before = new RoleAuthorizationState(3, Set.of("user:read"), Set.of());
        var after = new RoleAuthorizationState(5, Set.of("user:read", "role:assign"), Set.of("role:assign"));

        var impact = new RoleChangeImpactAnalyzer().analyze(before, after, 4, 3);

        assertEquals(Set.of("role:assign"), impact.addedPermissions());
        assertEquals(Set.of("role:assign"), impact.addedGrantablePermissions());
        assertTrue(impact.authorityLevelChanged());
        assertTrue(impact.expandsEffectiveAuthority());
        assertEquals(4, impact.affectedAssignmentCount());
        assertEquals(3, impact.affectedUserCount());
    }
}
