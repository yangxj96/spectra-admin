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
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.net.ServerSocket;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 真实 Redis 上验证安全 Redis 正常读写与故障拒绝。 */
@Tag("integration")
@Testcontainers
class SecurityRedisFailureIntegrationTest {

    @Container
    static final RedisContainer REDIS = new RedisContainer("redis:7.4.7");

    @Test
    void shouldReadAndWriteThroughLiveRedis() {
        RedisProbe probe = RedisProbe.connect(REDIS);
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
    void shouldFailClosedWhenRedisEndpointIsUnavailable() throws Exception {
        int unusedPort;
        try (var socket = new ServerSocket(0)) {
            unusedPort = socket.getLocalPort();
        }
        RedisProbe probe = RedisProbe.connect(REDIS.getHost(), unusedPort);
        try {
            assertThatThrownBy(() -> SecurityRedisExecutor.require("读取故障 Redis 测试值",
                    () -> probe.redis().opsForValue().get(SecurityRedisNamespace.PREFIX + "test:redis-failure")))
                    .isInstanceOf(SecurityRedisUnavailableException.class);
        } finally {
            probe.close();
        }
    }

    private record RedisProbe(RedisTemplate<String, String> redis, LettuceConnectionFactory connectionFactory) {

        private static RedisProbe connect(RedisContainer container) {
            return connect(container.getHost(), container.getFirstMappedPort());
        }

        private static RedisProbe connect(String host, int port) {
            RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(host, port);
            configuration.setDatabase(0);
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
}
