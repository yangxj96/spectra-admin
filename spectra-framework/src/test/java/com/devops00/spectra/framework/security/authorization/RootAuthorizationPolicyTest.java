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

package com.devops00.spectra.framework.security.authorization;

import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Root 判定契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class RootAuthorizationPolicyTest {

    @Test
    void shouldRecognizeOnlyConfiguredDevOpsRoleAsRoot() {
        var policy = new DefaultRootAuthorizationPolicy();
        var root = mockPrincipal(List.of("ROLE_DEV_OPS"));
        var administrator = mockPrincipal(List.of("ROLE_ADMIN_SYSTEM"));

        assertTrue(policy.isRoot(root));
        assertFalse(policy.isRoot(administrator));
        assertFalse(policy.isRoot(null));
    }

    /**
     * 处理主体相关数据。
     */
    private static SecurityPrincipal mockPrincipal(List<String> authorityNames) {
        var principal = org.mockito.Mockito.mock(SecurityPrincipal.class);
        org.mockito.Mockito.when(principal.isEnabled()).thenReturn(true);
        org.mockito.Mockito.when(principal.isAccountNonExpired()).thenReturn(true);
        org.mockito.Mockito.when(principal.isAccountNonLocked()).thenReturn(true);
        org.mockito.Mockito.when(principal.isCredentialsNonExpired()).thenReturn(true);
        org.mockito.Mockito.when(principal.getAuthorityNames()).thenReturn(authorityNames);
        return principal;
    }

    @Test
    void shouldDefaultToThreeDevOpsUsersAndProtectTheLastOne() {
        var properties = new SecurityProperties();

        assertTrue(properties.getMinEffectiveDevOpsUsers() == 1);
        assertTrue(properties.getMaxDevOpsUsers() == 3);
    }
}
