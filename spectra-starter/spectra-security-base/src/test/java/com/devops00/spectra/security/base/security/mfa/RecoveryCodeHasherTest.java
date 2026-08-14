/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.base.security.mfa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecoveryCodeHasherTest {

    @Test
    void shouldHashAndVerifyWithoutStoringPlainCode() {
        String code = "ABCD-EFGH-1234";
        String hash = RecoveryCodeHasher.hash(code);

        assertNotEquals(code, hash);
        assertTrue(RecoveryCodeHasher.matches(code, hash));
        assertFalse(RecoveryCodeHasher.matches("ABCD-EFGH-1235", hash));
    }
}
