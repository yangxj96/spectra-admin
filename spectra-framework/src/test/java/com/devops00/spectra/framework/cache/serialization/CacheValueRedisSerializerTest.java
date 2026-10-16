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

package com.devops00.spectra.framework.cache.serialization;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.SerializationException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 验证损坏/未知缓存值显式拒绝，正常 miss 与结构错误区分。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class CacheValueRedisSerializerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final CacheValueRedisSerializer serializer = new CacheValueRedisSerializer(mapper);

    @Test
    void roundTripsSupportedValues() {
        UUID id = UUID.randomUUID();
        Map<UUID, String> map = new LinkedHashMap<>();
        map.put(id, "name");
        assertEquals(map, serializer.deserialize(serializer.serialize(map)));
        assertEquals(List.of(id), serializer.deserialize(serializer.serialize(List.of(id))));
    }

    @Test
    void nullAndEmptyAreNormalMiss() {
        assertNull(serializer.deserialize(null));
        assertNull(serializer.deserialize(new byte[0]));
    }

    @Test
    void corruptedBytesAreRejected() {
        assertThrows(SerializationException.class,
                () -> serializer.deserialize("{not-json".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void unknownEnvelopeTypeIsRejected() throws Exception {
        byte[] envelope = mapper.writeValueAsBytes(Map.of("type", "legacy", "value", Map.of()));
        assertThrows(SerializationException.class, () -> serializer.deserialize(envelope));
    }

    @Test
    void mismatchedShapeIsRejected() throws Exception {
        byte[] envelope = mapper.writeValueAsBytes(Map.of("type", "uuid-string-map", "value", List.of("x")));
        assertThrows(SerializationException.class, () -> serializer.deserialize(envelope));
    }
}
