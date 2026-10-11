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

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LoginExceptionAdviceCredentialAgeTest {

    @Test
    void expiredCredentialMessageIsReturnedAsUnauthorized() {
        var response = mock(HttpServletResponse.class);
        var body = new LoginExceptionAdvice().handleLoginException(
                new BadCredentialsException("密码已过期，请联系管理员重置密码"), response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertEquals(401, body.getCode());
        assertEquals("密码已过期，请联系管理员重置密码", body.getMsg());
    }
}
