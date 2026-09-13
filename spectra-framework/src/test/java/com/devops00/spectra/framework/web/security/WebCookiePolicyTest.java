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

package com.devops00.spectra.framework.web.security;

import com.devops00.spectra.framework.security.properties.SecurityProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 验证 {@code WebCookiePolicyTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
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

        SecurityProperties development = new SecurityProperties();
        development.setRefreshCookieName("spectra-refresh");
        development.setRefreshCookieSecure(false);
        development.setAllowInsecureRefreshCookie(true);
        assertDoesNotThrow(() -> WebCookiePolicy.validate(development));

        SecurityProperties domain = new SecurityProperties();
        domain.setRefreshCookieDomain("example.com");
        assertThrows(IllegalStateException.class, () -> WebCookiePolicy.validate(domain));
    }

    @Test
    void shouldRequireExplicitApprovalForCrossSiteCookie() {
        SecurityProperties crossSite = new SecurityProperties();
        crossSite.setRefreshCookieSameSite("None");
        assertThrows(IllegalStateException.class, () -> WebCookiePolicy.validate(crossSite));

        crossSite.setRefreshCookieSameSiteNoneAllowed(true);
        assertDoesNotThrow(() -> WebCookiePolicy.validate(crossSite));
    }

    @Test
    void shouldRejectInvalidCsrfNames() {
        SecurityProperties duplicate = new SecurityProperties();
        duplicate.setCsrfHeaderName(duplicate.getCsrfCookieName());
        assertThrows(IllegalStateException.class, () -> WebCookiePolicy.validate(duplicate));

        SecurityProperties blank = new SecurityProperties();
        blank.setCsrfCookieName(" ");
        assertThrows(IllegalStateException.class, () -> WebCookiePolicy.validate(blank));
    }
}
