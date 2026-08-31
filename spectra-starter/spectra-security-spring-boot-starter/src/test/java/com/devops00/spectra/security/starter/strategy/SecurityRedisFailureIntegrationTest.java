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

package com.devops00.spectra.security.starter.strategy;

import com.devops00.spectra.security.base.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.security.base.constant.SecurityRedisNamespace;
import com.devops00.spectra.security.base.util.SecurityRedisExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 真实 Redis 上验证安全 Redis 正常读写与故障拒绝。 */
@EnabledIfEnvironmentVariable(named = "SPECTRA_SECURITY_REDIS_LIVE_TEST", matches = "true")
class SecurityRedisFailureIntegrationTest {

    @Test
    void shouldReadAndWriteThroughLiveRedis() {
        RedisProbe probe = RedisProbe.connect();
        String key = SecurityRedisNamespace.PREFIX + "test:redis-live:" + UUID.randomUUID();
        try {
            SecurityRedisExecutor.run("写入实时 Redis 测试值",
                    () -> probe.redis().opsForValue().set(key, "ok", Duration.ofMinutes(1)));

            String value = SecurityRedisExecutor.require("读取实时 Redis 测试值",
                    () -> probe.redis().opsForValue().get(key));

            assertThat(value).isEqualTo("ok");
        } finally {
            probe.redis().delete(key);
            probe.close();
        }
    }

    @Test
    void shouldFailClosedWhenRedisIsUnavailable() {
        RedisProbe probe = RedisProbe.connect();
        try {
            assertThatThrownBy(() -> SecurityRedisExecutor.require("读取故障 Redis 测试值",
                    () -> probe.redis().opsForValue().get(SecurityRedisNamespace.PREFIX + "test:redis-failure")))
                    .isInstanceOf(SecurityRedisUnavailableException.class);
        } finally {
            probe.close();
        }
    }

    private record RedisProbe(RedisTemplate<String, String> redis, LettuceConnectionFactory connectionFactory) {

        private static RedisProbe connect() {
            RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(
                    environment("REDIS_HOST", "127.0.0.1"), integerEnvironment("REDIS_PORT", 6379));
            configuration.setDatabase(integerEnvironment("REDIS_DB", 0));
            String password = environment("REDIS_PASSWORD", "");
            if (!password.isBlank()) {
                configuration.setPassword(RedisPassword.of(password));
            }

            LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(configuration);
            connectionFactory.afterPropertiesSet();
            RedisTemplate<String, String> redis = new RedisTemplate<>();
            redis.setConnectionFactory(connectionFactory);
            StringRedisSerializer serializer = new StringRedisSerializer();
            redis.setKeySerializer(serializer);
            redis.setValueSerializer(serializer);
            redis.setHashKeySerializer(serializer);
            redis.setHashValueSerializer(serializer);
            redis.afterPropertiesSet();
            return new RedisProbe(redis, connectionFactory);
        }

        private void close() {
            connectionFactory.destroy();
        }
    }

    private static String environment(String name, String defaultValue) {
        return System.getenv().getOrDefault(name, defaultValue).trim();
    }

    private static int integerEnvironment(String name, int defaultValue) {
        return Integer.parseInt(environment(name, Integer.toString(defaultValue)));
    }
}
