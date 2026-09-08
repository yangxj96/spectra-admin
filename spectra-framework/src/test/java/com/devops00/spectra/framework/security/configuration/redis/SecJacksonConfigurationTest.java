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

package com.devops00.spectra.framework.security.configuration.redis;

import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** 安全 Redis mapper 的基础值格式和多态边界测试。 */
class SecJacksonConfigurationTest {

    @Test
    void shouldSerializeSecurityValuesWithoutTypeMetadata() throws Exception {
        ObjectMapper mapper = securityMapper();
        Map<String, Object> value = Map.of("userId", "user-1", "loginTime", 1_757_311_872_000L);
        String json = mapper.writeValueAsString(value);

        assertThat(json).doesNotContain("@class");
        assertThat(mapper.readValue(json, new TypeReference<Map<String, Object>>() {
        })).containsEntry("userId", "user-1");
    }

    @Test
    void shouldNotInstantiateApplicationTypesFromSecurityPayload() throws Exception {
        ObjectMapper mapper = securityMapper();
        String json = "{\"@class\":\"com.devops00.spectra.framework.security.configuration.redis.SecJacksonConfigurationTest$ApplicationPayload\","
                + "\"value\":\"secret\"}";

        Object decoded = mapper.readValue(json, Object.class);

        assertThat(decoded).isInstanceOf(Map.class);
    }

    @Test
    void shouldKeepUnknownPropertyFailureEnabledForSecurityData() {
        assertThat(securityMapper().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isTrue();
    }

    private static ObjectMapper securityMapper() {
        return new SecJacksonConfiguration().redisObjectMapper();
    }

    static class ApplicationPayload {

        public String value;
    }
}
