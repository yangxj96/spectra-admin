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

package com.devops00.spectra.common.security.policy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 验证 {@code PasswordPolicyTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class PasswordPolicyTest {

    private static final PasswordPolicy POLICY = new PasswordPolicy(12, true, true, true, true, null);

    @Test
    void shouldAcceptPasswordMeetingAllConfiguredRequirements() {
        assertDoesNotThrow(() -> POLICY.assertAccepts("StrongPassword1!"));
    }

    @Test
    void shouldRejectPasswordMissingConfiguredRequirements() {
        assertThrows(IllegalArgumentException.class, () -> POLICY.assertAccepts("short1!"));
        assertThrows(IllegalArgumentException.class, () -> POLICY.assertAccepts("strongpassword1!"));
        assertThrows(IllegalArgumentException.class, () -> POLICY.assertAccepts("STRONGPASSWORD1!"));
        assertThrows(IllegalArgumentException.class, () -> POLICY.assertAccepts("StrongPassword!"));
        assertThrows(IllegalArgumentException.class, () -> POLICY.assertAccepts("StrongPassword1"));
    }

    @Test
    void shouldAllowRequirementsToBeDisabledIndividually() {
        PasswordPolicy relaxed = new PasswordPolicy(8, false, false, false, false, null);

        assertDoesNotThrow(() -> relaxed.assertAccepts("password"));
    }

    @Test
    void shouldRejectPolicyMinimumAboveSupportedMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new PasswordPolicy(21, true, true, true, true, null));
    }

    @Test
    void shouldRecognizeSupplementaryUnicodeLettersAndUnlistedSpecialCharacters() {
        PasswordPolicy unicodePolicy = new PasswordPolicy(8, true, true, true, true, null);

        assertDoesNotThrow(() -> unicodePolicy.assertAccepts("𐐀𐐨abc123-"));
    }
}
