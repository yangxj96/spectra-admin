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

package com.devops00.spectra.core.security.policy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 验证当前密码策略可由普通已登录用户读取，修改权限仍受限。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class SecurityPolicyControllerPermissionTest {

    @Test
    void passwordPolicyReadRequiresAuthenticationWhileUpdateRequiresPermission() throws NoSuchMethodException {
        var read = SecurityPolicyController.class.getMethod("passwordPolicy");
        var update = SecurityPolicyController.class.getMethod("modifyPasswordPolicy",
                com.devops00.spectra.core.security.policy.javabean.from.SecurityPasswordPolicyFrom.class);

        assertEquals("isAuthenticated()", read.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasPermission(null, 'security:password-policy:update')",
                update.getAnnotation(PreAuthorize.class).value());
    }
}
