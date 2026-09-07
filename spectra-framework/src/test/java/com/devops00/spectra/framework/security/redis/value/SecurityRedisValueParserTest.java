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

package com.devops00.spectra.framework.security.redis.value;

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 安全 Redis 类型解析边界测试。 */
class SecurityRedisValueParserTest {

    @Test
    void shouldRejectUnknownClientTypeWithoutWebFallback() {
        assertThrows(SecurityRedisUnavailableException.class,
                () -> SecurityRedisValueParser.requiredClientType("unknown", "clientType"));
    }

    @Test
    void shouldRejectEmptyMapAsMissingSecurityData() {
        var exception = assertThrows(SecurityRedisUnavailableException.class,
                () -> SecurityRedisValueParser.requiredMap(Map.of(), "session"));

        assertTrue(exception.getMessage().contains("字段 session 无效"));
    }
}
