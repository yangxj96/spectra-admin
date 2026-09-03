/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.common.security.policy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
