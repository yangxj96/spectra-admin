/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.base.security.mfa;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotpCodeServiceTest {

    @Test
    void shouldMatchRfc6238Sha1VectorWithinOneTimeWindow() {
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        assertTrue(TotpCodeService.matches(secret, "287082", Instant.ofEpochSecond(59), 0));
        assertTrue(TotpCodeService.matches(secret, "287082", Instant.ofEpochSecond(60), 1));
        assertFalse(TotpCodeService.matches(secret, "287083", Instant.ofEpochSecond(59), 1));
    }
}
