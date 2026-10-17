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

package com.devops00.spectra.framework.security;

import com.devops00.spectra.common.port.security.RuntimeSecret;
import com.devops00.spectra.common.port.security.SecurityToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 敏感对象诊断输出不包含明文凭据的验证。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class SensitiveObjectToStringTest {

    @Test
    void runtimeSecretRedactsValue() {
        var secret = new RuntimeSecret("secret.code", 1, "super-secret-value", "fingerprint");
        var text = secret.toString();
        assertFalse(text.contains("super-secret-value"));
        assertTrue(text.contains("code=secret.code"));
    }

    @Test
    void securityTokenRedactsTokens() {
        var token = SecurityToken.builder()
                .accessToken("access-secret")
                .refreshToken("refresh-secret")
                .username("user")
                .build();
        var text = token.toString();
        assertFalse(text.contains("access-secret"));
        assertFalse(text.contains("refresh-secret"));
        assertTrue(text.contains("username=user"));
    }
}
