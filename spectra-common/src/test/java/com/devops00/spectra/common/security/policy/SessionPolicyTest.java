/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.common.security.policy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SessionPolicyTest {

    @Test
    void shouldRejectInvalidSessionLimits() {
        assertThrows(IllegalArgumentException.class,
                () -> new SessionPolicy(SessionConcurrencyMode.ALLOW, 0, 300, 604800, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new SessionPolicy(SessionConcurrencyMode.ALLOW, 1, 0, 604800, null, null));
    }
}
