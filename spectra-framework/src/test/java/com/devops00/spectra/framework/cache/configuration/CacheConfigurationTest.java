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

package com.devops00.spectra.framework.cache.configuration;

import com.devops00.spectra.framework.cache.serialization.CacheValueRedisSerializer;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 缓存 mapper 的类型边界和 Java 容器兼容性测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class CacheConfigurationTest {

    @Test
    void shouldRoundTripJavaContainerValuesWithoutApplicationTyping() throws Exception {
        ObjectMapper mapper = cacheMapper();
        UUID id = UUID.fromString("d7c9f7d2-1a0b-4e4e-8ec1-38ad8f46e0b6");
        CacheValueRedisSerializer serializer = new CacheValueRedisSerializer(mapper);
        byte[] encoded = serializer.serialize(Map.of(id, "研发部"));
        String json = new String(encoded, StandardCharsets.UTF_8);

        assertThat(json).contains("uuid-string-map").doesNotContain("@class");
        assertThat(serializer.deserialize(encoded)).isEqualTo(Map.of(id, "研发部"));
    }

    @Test
    void shouldRoundTripUuidCollectionsAndTreatEmptyValuesAsMiss() {
        CacheValueRedisSerializer serializer = new CacheValueRedisSerializer(cacheMapper());
        UUID first = UUID.fromString("d7c9f7d2-1a0b-4e4e-8ec1-38ad8f46e0b6");
        UUID second = UUID.fromString("2b7b1a4e-4e24-4e9d-a1a4-7dbd3cb5af30");

        byte[] encoded = serializer.serialize(List.of(first, second));

        assertThat(new String(encoded, StandardCharsets.UTF_8)).contains("uuid-collection");
        assertThat(serializer.deserialize(encoded)).isEqualTo(List.of(first, second));
        assertThat(serializer.deserialize(serializer.serialize(null))).isNull();
    }

    @Test
    void shouldTreatOldOrUntrustedCachePayloadAsMiss() {
        CacheValueRedisSerializer serializer = new CacheValueRedisSerializer(cacheMapper());
        String json = "{\"@class\":\"com.devops00.spectra.framework.cache.configuration.CacheConfigurationTest$ApplicationPayload\","
                + "\"value\":\"secret\"}";

        assertThat(serializer.deserialize(json.getBytes(StandardCharsets.UTF_8))).isNull();
    }

    @Test
    void shouldKeepUnknownPropertyFailureEnabledForInternalCacheData() {
        assertThat(cacheMapper().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isTrue();
    }

    /**
     * 处理缓存相关数据。
     */
    private static ObjectMapper cacheMapper() {
        return new CacheConfiguration().redisObjectMapper();
    }

}
