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

package com.devops00.spectra.framework.security.advice;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 401/403 安全边界响应携带稳定 error_code 的验证。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class RestSecurityErrorResponseTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void unauthenticatedCarriesUnauthenticatedCode() throws Exception {
        var response = new MockHttpServletResponse();
        new RestAuthenticationEntryPoint(mapper).commence(new MockHttpServletRequest(), response,
                new InsufficientAuthenticationException("未登录"));
        assertEquals(401, response.getStatus());
        assertEquals("UNAUTHENTICATED", code(response));
    }

    @Test
    void badCredentialsCarriesAuthenticationFailedCode() throws Exception {
        var response = new MockHttpServletResponse();
        new RestAuthenticationEntryPoint(mapper).commence(new MockHttpServletRequest(), response,
                new BadCredentialsException("密码错误"));
        assertEquals(401, response.getStatus());
        assertEquals("AUTHENTICATION_FAILED", code(response));
    }

    @Test
    void accessDeniedCarriesAccessDeniedCode() throws Exception {
        var response = new MockHttpServletResponse();
        new RestAccessDeniedHandler(mapper).handle(new MockHttpServletRequest(), response,
                new AccessDeniedException("denied"));
        assertEquals(403, response.getStatus());
        assertEquals("ACCESS_DENIED", code(response));
    }

    private String code(MockHttpServletResponse response) throws Exception {
        return mapper.readTree(response.getContentAsString()).path("errorCode").asString();
    }
}
