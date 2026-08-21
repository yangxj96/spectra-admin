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

package com.devops00.spectra.security.starter.filter;

import com.devops00.spectra.security.base.change.SecurityUserLookupPort;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

/**
 * Redis 会话依赖故障时的 fail-closed 回归测试。
 */
class TokenAuthenticationFilterTest {

    @Test
    void shouldStopRequestWhenRedisSessionIsUnavailable() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/user/profile");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);
        var contextAccessor = mock(SecurityContextAccessor.class);
        var userLookupPort = mock(SecurityUserLookupPort.class);
        when(contextAccessor.currentToken()).thenReturn("opaque-token");
        when(userLookupPort.findByToken("opaque-token"))
                .thenThrow(new RedisConnectionFailureException("redis unavailable"));
        var filter = new TokenAuthenticationFilter(contextAccessor, userLookupPort);

        filter.doFilter(request, response, chain);

        assertEquals(503, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void shouldAllowClientPrivateKeyForTemporaryPasswordSession() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/system/crypto/keypair/client-private");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);
        var contextAccessor = mock(SecurityContextAccessor.class);
        var userLookupPort = mock(SecurityUserLookupPort.class);
        var user = new SecurityUser();
        user.setPasswordChangeRequired(true);
        when(contextAccessor.currentToken()).thenReturn("temporary-token");
        when(userLookupPort.findByToken("temporary-token")).thenReturn(user);
        var filter = new TokenAuthenticationFilter(contextAccessor, userLookupPort);

        try {
            filter.doFilter(request, response, chain);

            assertEquals(200, response.getStatus());
            verify(chain).doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldBlockBusinessRequestForTemporaryPasswordSession() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/department/tree");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);
        var contextAccessor = mock(SecurityContextAccessor.class);
        var userLookupPort = mock(SecurityUserLookupPort.class);
        var user = new SecurityUser();
        user.setPasswordChangeRequired(true);
        when(contextAccessor.currentToken()).thenReturn("temporary-token");
        when(userLookupPort.findByToken("temporary-token")).thenReturn(user);
        var filter = new TokenAuthenticationFilter(contextAccessor, userLookupPort);

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }
}
