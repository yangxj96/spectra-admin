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

package com.devops00.spectra.framework.web.advice.exception;

import com.devops00.spectra.common.exception.DataScopeViolationException;
import com.devops00.spectra.common.exception.DataExistException;
import com.devops00.spectra.framework.web.response.R;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 通用异常处理测试
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/30
 */
class CommonExceptionAdviceTest {

    @Test
    void accessDeniedShouldReturnForbidden() throws Exception {
        var method = CommonExceptionAdvice.class.getMethod("accessDeniedException", AccessDeniedException.class, HttpServletResponse.class);
        var response = new MockHttpServletResponse();

        var result = (R<?>) method.invoke(new CommonExceptionAdvice(), new AccessDeniedException("Access Denied"), response);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertEquals(HttpServletResponse.SC_FORBIDDEN, result.getCode());
        assertEquals("权限不足", result.getMsg());
    }

    @Test
    void dataScopeViolationShouldReturnForbidden() throws Exception {
        var method = CommonExceptionAdvice.class.getMethod("dataScopeViolationException", DataScopeViolationException.class,
                HttpServletResponse.class);
        var response = new MockHttpServletResponse();

        var result = (R<?>) method.invoke(new CommonExceptionAdvice(), new DataScopeViolationException("missing scope"), response);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertEquals(HttpServletResponse.SC_FORBIDDEN, result.getCode());
        assertEquals("数据范围不足", result.getMsg());
    }

    @Test
    void dataExistShouldPreserveActionableMessage() throws Exception {
        var method = CommonExceptionAdvice.class.getMethod("dataExistException", Exception.class, HttpServletResponse.class);
        var response = new MockHttpServletResponse();

        var result = (R<?>) method.invoke(new CommonExceptionAdvice(),
                new DataExistException("任务已归档，请重新注册"), response);

        assertEquals(HttpServletResponse.SC_CONFLICT, response.getStatus());
        assertEquals(HttpServletResponse.SC_CONFLICT, result.getCode());
        assertEquals("任务已归档，请重新注册", result.getMsg());
    }

    @Test
    void unreadableRequestShouldReturnBadRequest() throws Exception {
        var method = CommonExceptionAdvice.class.getMethod("httpMessageNotReadableException",
                HttpMessageNotReadableException.class, HttpServletResponse.class);
        var response = new MockHttpServletResponse();

        var result = (R<?>) method.invoke(new CommonExceptionAdvice(),
                new HttpMessageNotReadableException("malformed request", (HttpInputMessage) null), response);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, result.getCode());
        assertEquals("请求数据格式错误，请检查请求体", result.getMsg());
    }

    @Test
    void unexpectedRuntimeMessageShouldNotBeReturnedToClient() throws Exception {
        var method = CommonExceptionAdvice.class.getMethod("runtimeException", RuntimeException.class,
                HttpServletResponse.class);
        var response = new MockHttpServletResponse();

        var result = (R<?>) method.invoke(new CommonExceptionAdvice(),
                new RuntimeException("internal secret"), response);

        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("系统内部错误,请联系管理员", result.getMsg());
    }
}
