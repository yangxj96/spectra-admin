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

import com.devops00.spectra.framework.security.configuration.redis.SecJacksonConfiguration;
import com.devops00.spectra.framework.security.configuration.redis.SecRedisConfiguration;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * 真实依赖测试只使用 test 环境的 Redis DB 5，并为每个用例隔离、清理自己的键。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/10
 */
final class RealRedisTestEnvironment {
    private RealRedisTestEnvironment() {
    }

    static RedisStandaloneConfiguration configuration() {
        return configuration(System.getenv());
    }

    static RedisStandaloneConfiguration configuration(Map<String, String> environment) {
        String database = environment.get("REDIS_DB");
        if (!"5".equals(database)) {
            throw new IllegalStateException("真实依赖测试只允许 REDIS_DB=5");
        }
        String host = environment.get("REDIS_HOST");
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("真实依赖测试缺少 REDIS_HOST");
        }
        int port;
        try {
            port = Integer.parseInt(environment.get("REDIS_PORT"));
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("真实依赖测试缺少有效 REDIS_PORT", exception);
        }
        if (port < 1 || port > 65535) {
            throw new IllegalStateException("真实依赖测试的 REDIS_PORT 超出范围");
        }
        var configuration = new RedisStandaloneConfiguration(host, port);
        configuration.setDatabase(5);
        String password = environment.get("REDIS_PASSWORD");
        if (password != null && !password.isBlank()) {
            configuration.setPassword(RedisPassword.of(password));
        }
        return configuration;
    }

    static String newKeyPrefix() {
        return "spectra:test:" + UUID.randomUUID() + ":";
    }

    static RedisTemplate<String, Object> template(LettuceConnectionFactory factory, String prefix) {
        var template = new SecRedisConfiguration().redisTemplate(factory,
                new SecJacksonConfiguration().redisObjectMapper(), new SecurityProperties());
        var delegate = new StringRedisSerializer();
        template.setKeySerializer(new RedisSerializer<String>() {
            @Override
            public byte[] serialize(String value) throws SerializationException {
                return delegate.serialize(prefix + value);
            }

            @Override
            public String deserialize(byte[] bytes) throws SerializationException {
                String value = delegate.deserialize(bytes);
                if (value == null || !value.startsWith(prefix)) {
                    throw new SerializationException("Redis 测试键不属于当前用例");
                }
                return value.substring(prefix.length());
            }
        });
        return template;
    }

    static void clearOwnKeys(LettuceConnectionFactory factory, String prefix) {
        var ownedKeys = new ArrayList<byte[]>();
        try (var connection = factory.getConnection();
                var keys = connection.scan(ScanOptions.scanOptions().match(prefix + "*").count(100).build())) {
            while (keys.hasNext()) {
                byte[] key = keys.next();
                if (!new String(key, StandardCharsets.UTF_8).startsWith(prefix)) {
                    throw new IllegalStateException("Redis 扫描结果不属于当前测试用例");
                }
                ownedKeys.add(key);
            }
            for (byte[] key : ownedKeys) {
                connection.keyCommands().del(key);
            }
        }
    }
}
