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
import com.devops00.spectra.framework.configure.mvc.advice.exception.SqlExceptionAdvice;
import com.devops00.spectra.security.base.exception.SecurityRedisUnavailableException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 安全 Redis 故障必须映射为 503 的测试。 */
class SqlExceptionAdviceTest {

    @Test
    void securityRedisUnavailableShouldReturnServiceUnavailable() throws Exception {
        var method = SqlExceptionAdvice.class.getMethod("handleSecurityRedisUnavailable",
                SecurityRedisUnavailableException.class, HttpServletResponse.class);
        var response = new MockHttpServletResponse();

        var result = (R<?>) method.invoke(new SqlExceptionAdvice(),
                new SecurityRedisUnavailableException("redis unavailable", null), response);

        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, response.getStatus());
        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, result.getCode());
        assertEquals("安全会话服务暂不可用", result.getMsg());
    }
}
