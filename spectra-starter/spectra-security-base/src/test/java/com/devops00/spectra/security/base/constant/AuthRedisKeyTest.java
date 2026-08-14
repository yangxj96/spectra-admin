/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.base.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthRedisKeyTest {

    @Test
    void shouldUseV2DigestNamespaceForSessionAndRefreshIndexes() {
        assertTrue(AuthRedisKey.SESSION.format("digest").startsWith("sec:v2:"));
        assertTrue(AuthRedisKey.REFRESH_TOKEN.format("digest").startsWith("sec:v2:"));
        assertTrue(AuthRedisKey.SESSION_FAMILY.format("family").startsWith("sec:v2:"));
        assertFalse(AuthRedisKey.SESSION.format("plaintext-token").contains("auth:"));
    }
}
