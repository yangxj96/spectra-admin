/*
 * Copyright 2018-2026 yangxj96
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.devops00.spectra.framework.security.session;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 测试键序列化保持每个用例独立；不连接真实 Redis。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/10
 */
class RealRedisTestEnvironmentTest {
    @Test
    @SuppressWarnings("unchecked")
    void eachTestPrefixKeepsPhysicalKeysSeparate() {
        var factory = new LettuceConnectionFactory(new RedisStandaloneConfiguration("127.0.0.1", 6379));
        String firstPrefix = RealRedisTestEnvironment.newKeyPrefix();
        String secondPrefix = RealRedisTestEnvironment.newKeyPrefix();
        var first = (RedisSerializer<String>) RealRedisTestEnvironment.template(factory, firstPrefix)
                .getKeySerializer();
        var second = (RedisSerializer<String>) RealRedisTestEnvironment.template(factory, secondPrefix)
                .getKeySerializer();

        byte[] firstKey = first.serialize("sec:session:synthetic");
        byte[] secondKey = second.serialize("sec:session:synthetic");
        assertFalse(Arrays.equals(firstKey, secondKey));
        assertEquals("sec:session:synthetic", first.deserialize(firstKey));
        assertThrows(SerializationException.class, () -> first.deserialize(secondKey));
    }

    @Test
    void acceptsOnlyTheDedicatedRedisDatabaseAndValidConnectionShape() {
        var testEnvironment = Map.of("REDIS_DB", "5", "REDIS_HOST", "127.0.0.1", "REDIS_PORT", "6379");
        var configuration = RealRedisTestEnvironment.configuration(testEnvironment);
        assertEquals(5, configuration.getDatabase());
        assertEquals(6379, configuration.getPort());

        assertThrows(IllegalStateException.class, () -> RealRedisTestEnvironment.configuration(
                Map.of("REDIS_DB", "4", "REDIS_HOST", "127.0.0.1", "REDIS_PORT", "6379")));
        assertThrows(IllegalStateException.class, () -> RealRedisTestEnvironment.configuration(
                Map.of("REDIS_DB", "5", "REDIS_HOST", "127.0.0.1", "REDIS_PORT", "0")));
        assertThrows(IllegalStateException.class, () -> RealRedisTestEnvironment.configuration(
                Map.of("REDIS_DB", "5", "REDIS_PORT", "6379")));
    }
}
