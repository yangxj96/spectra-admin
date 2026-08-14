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

package com.devops00.spectra.security.starter.root;

import com.devops00.spectra.security.base.properties.SecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Root 判定契约测试。
 */
class RootAuthorizationPolicyTest {

    @Test
    void shouldRecognizeOnlyConfiguredDevOpsRoleAsRoot() {
        var policy = new DefaultRootAuthorizationPolicy();
        var root = UsernamePasswordAuthenticationToken.authenticated("root", "N/A",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_DEV_OPS")));
        var administrator = UsernamePasswordAuthenticationToken.authenticated("admin", "N/A",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN_SYSTEM")));

        assertTrue(policy.isRoot(root));
        assertFalse(policy.isRoot(administrator));
        assertFalse(policy.isRoot(null));
    }

    @Test
    void shouldDefaultToThreeDevOpsUsersAndProtectTheLastOne() {
        var properties = new SecurityProperties();

        assertTrue(properties.getMinEffectiveDevOpsUsers() == 1);
        assertTrue(properties.getMaxDevOpsUsers() == 3);
    }
}
