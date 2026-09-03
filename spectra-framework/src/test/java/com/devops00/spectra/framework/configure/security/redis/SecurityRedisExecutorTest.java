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

package com.devops00.spectra.framework.configure.security.redis;

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 安全 Redis 故障统一转换测试。 */
class SecurityRedisExecutorTest {

    @Test
    void shouldConvertRedisDataAccessFailureToSecurityUnavailable() {
        var cause = new DataAccessResourceFailureException("redis unavailable");

        var exception = assertThrows(SecurityRedisUnavailableException.class,
                () -> SecurityRedisExecutor.execute("读取安全会话", () -> {
                    throw cause;
                }));

        assertEquals("安全 Redis 不可用，拒绝执行读取安全会话", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void shouldPreserveBusinessRuntimeException() {
        var exception = new IllegalArgumentException("invalid token");

        assertSame(exception, assertThrows(IllegalArgumentException.class,
                () -> SecurityRedisExecutor.run("校验安全会话", () -> {
                    throw exception;
                })));
    }

    @Test
    void shouldRejectMissingRequiredRedisResult() {
        assertThrows(SecurityRedisUnavailableException.class,
                () -> SecurityRedisExecutor.require("检查安全 Redis Key", () -> null));
    }
}
