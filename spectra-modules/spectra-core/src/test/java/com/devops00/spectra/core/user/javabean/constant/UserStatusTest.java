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

package com.devops00.spectra.core.user.javabean.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserStatusTest {

    @Test
    void shouldExposeTargetLifecycleValuesAndLoginRule() {
        assertEquals("ACTIVE", UserStatus.ACTIVE.getCode());
        assertEquals("LOCKED", UserStatus.LOCKED.getCode());
        assertEquals("DISABLED", UserStatus.DISABLED.getCode());
        assertEquals("DEPARTED", UserStatus.DEPARTED.getCode());
        assertTrue(UserStatus.ACTIVE.loginAllowed());
        assertFalse(UserStatus.LOCKED.loginAllowed());
        assertFalse(UserStatus.DISABLED.loginAllowed());
        assertFalse(UserStatus.DEPARTED.loginAllowed());
    }

    @Test
    void shouldAllowExplicitReentryWithoutImplyingAuthorizationRestore() {
        assertDoesNotThrow(() -> UserStatus.DEPARTED.assertTransitionTo(UserStatus.ACTIVE));
        assertTrue(UserStatus.DEPARTED.requiresSessionRevocation(UserStatus.ACTIVE));
    }

    @Test
    void shouldRejectImplicitOrUnsafeLifecycleTransitions() {
        assertThrows(IllegalArgumentException.class,
                () -> UserStatus.DEPARTED.assertTransitionTo(UserStatus.DISABLED));
        assertThrows(IllegalArgumentException.class,
                () -> UserStatus.DISABLED.assertTransitionTo(UserStatus.LOCKED));
    }
}
