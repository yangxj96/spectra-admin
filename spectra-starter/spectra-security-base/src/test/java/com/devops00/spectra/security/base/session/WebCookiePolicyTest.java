/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.base.session;

import com.devops00.spectra.security.base.properties.SecurityProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WebCookiePolicyTest {

    @Test
    void shouldAcceptDefaultHostOnlyStrictCookie() {
        assertDoesNotThrow(() -> WebCookiePolicy.validate(new SecurityProperties()));
    }

    @Test
    void shouldRejectInsecureOrDomainCookie() {
        SecurityProperties insecure = new SecurityProperties();
        insecure.setRefreshCookieSecure(false);
        assertThrows(IllegalStateException.class, () -> WebCookiePolicy.validate(insecure));

        SecurityProperties domain = new SecurityProperties();
        domain.setRefreshCookieDomain("example.com");
        assertThrows(IllegalStateException.class, () -> WebCookiePolicy.validate(domain));
    }
}
