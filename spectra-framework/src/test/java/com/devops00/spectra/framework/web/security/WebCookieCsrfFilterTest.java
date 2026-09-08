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
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** Cookie CSRF 过滤器回归测试。 */
class WebCookieCsrfFilterTest {

    private static final String FILTER_CLASS = "com.devops00.spectra.framework.web.security.WebCookieCsrfFilter";

    @Test
    void shouldRejectUnsafeCookieRequestWithoutCsrfHeader() throws Exception {
        var filter = filter(new SecurityProperties());
        var request = request("POST");
        request.setCookies(new jakarta.servlet.http.Cookie("__Host-spectra-refresh", "refresh"),
                new jakarta.servlet.http.Cookie("XSRF-TOKEN", "csrf"));
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void shouldAllowCookieRequestWithMatchingDoubleSubmitToken() throws Exception {
        var filter = filter(new SecurityProperties());
        var request = request("PUT");
        request.setCookies(new jakarta.servlet.http.Cookie("__Host-spectra-refresh", "refresh"),
                new jakarta.servlet.http.Cookie("XSRF-TOKEN", "csrf"));
        request.addHeader("X-XSRF-TOKEN", "csrf");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldLeaveHeaderTokenClientWithoutCookiesUsable() throws Exception {
        var filter = filter(new SecurityProperties());
        var request = request("POST");
        request.addHeader("X-Client-Type", "app");
        request.addHeader("Authorization", "Bearer opaque-token");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldKeepFilterAsServletFilterWithSecurityPropertiesConstructor() throws Exception {
        Class<?> filterType = Class.forName(FILTER_CLASS);
        assertNotNull(filterType.getConstructor(SecurityProperties.class, AccessDeniedHandler.class));
        assertEquals(true, Filter.class.isAssignableFrom(filterType));
    }

    private static Filter filter(SecurityProperties properties) throws Exception {
        Class<?> filterType = Class.forName(FILTER_CLASS);
        Constructor<?> constructor = filterType.getConstructor(SecurityProperties.class, AccessDeniedHandler.class);
        AccessDeniedHandler deniedHandler = (request, response, exception) -> response.setStatus(403);
        return (Filter) constructor.newInstance(properties, deniedHandler);
    }

    private static MockHttpServletRequest request(String method) {
        return new MockHttpServletRequest(method, "/api/security/authentication/refresh");
    }
}
