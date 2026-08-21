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

package com.devops00.spectra.security.starter.properties;

import com.devops00.spectra.security.base.properties.SecurityProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 匿名白名单安全回归测试。
 */
class SecurityPropertiesTest {

    @Test
    void shouldExposeOnlyNarrowActuatorAndNoAnonymousFilePreview() {
        var properties = new SecurityProperties();
        var whitelists = properties.getWhitelists();

        assertTrue(whitelists.contains("/actuator/health"));
        assertTrue(whitelists.contains("/actuator/info"));
        assertTrue(whitelists.contains("/security/authentication/mfa/verify"));
        assertTrue(whitelists.contains("/security/authentication/mfa/complete"));
        assertTrue(whitelists.contains("/security/mfa/setup/totp/enroll"));
        assertTrue(whitelists.contains("/security/mfa/setup/totp/confirm"));
        assertFalse(whitelists.contains("/actuator/**"));
        assertFalse(whitelists.contains("/file/preview/**"));
    }
}
