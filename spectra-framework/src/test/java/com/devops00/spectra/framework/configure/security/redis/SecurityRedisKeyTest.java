/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.framework.configure.security.redis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityRedisKeyTest {

    @Test
    void shouldUseSecurityDigestNamespaceForSessionAndRefreshIndexes() {
        assertEquals(SecurityRedisNamespace.PREFIX, "sec:");
        assertTrue(SecurityRedisKey.SESSION.format("digest").startsWith(SecurityRedisNamespace.PREFIX));
        assertTrue(SecurityRedisKey.REFRESH_TOKEN.format("digest").startsWith(SecurityRedisNamespace.PREFIX));
        assertTrue(SecurityRedisKey.SESSION_FAMILY.format("family").startsWith(SecurityRedisNamespace.PREFIX));
        assertTrue(SecurityRedisKey.REFRESH_FAMILY.format("family").startsWith(SecurityRedisNamespace.PREFIX));
        assertFalse(SecurityRedisKey.SESSION.format("plaintext-token").contains("auth:"));
    }
}
