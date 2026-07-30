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

package com.devops00.spectra.framework;

import com.devops00.spectra.common.response.R;
import com.devops00.spectra.framework.configure.mvc.advice.exception.CommonExceptionAdvice;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// 通用异常处理测试
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/30
class CommonExceptionAdviceTest {

    @Test
    void accessDeniedShouldReturnForbidden() throws Exception {
        var method = CommonExceptionAdvice.class.getMethod(
                "accessDeniedException", AccessDeniedException.class, HttpServletResponse.class);
        var response = new MockHttpServletResponse();

        var result = (R<?>) method.invoke(new CommonExceptionAdvice(), new AccessDeniedException("Access Denied"), response);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertEquals(HttpServletResponse.SC_FORBIDDEN, result.getCode());
        assertEquals("权限不足", result.getMsg());
    }
}
