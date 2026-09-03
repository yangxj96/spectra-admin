/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.framework.configure.security.redis;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenDigestServiceTest {

    @Test
    void shouldGenerateHighEntropyOpaqueTokensAndStableDigests() {
        String first = TokenDigestService.generateToken();
        String second = TokenDigestService.generateToken();

        assertNotEquals(first, second);
        assertEquals(TokenDigestService.digest(first), TokenDigestService.digest(first));
        assertNotEquals(TokenDigestService.digest(first), TokenDigestService.digest(second));
        assertTrue(Base64.getUrlDecoder().decode(first).length >= 32);
        assertTrue(Base64.getUrlDecoder().decode(TokenDigestService.digest(first)).length == 32);
    }
}
