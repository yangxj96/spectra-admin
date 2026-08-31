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

package com.devops00.spectra.framework.health;

import com.devops00.spectra.common.health.DependencyHealthContributor;
import com.devops00.spectra.common.health.DependencyHealthResult;
import com.devops00.spectra.common.health.DependencyHealthStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/** framework 提供的 Redis 技术依赖健康检查。 */
@Component("redisHealthContributor")
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisHealthContributor implements DependencyHealthContributor {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final RedisConnectionFactory connectionFactory;

    public RedisHealthContributor(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public String contributorName() {
        return "Redis";
    }

    @Override
    public String moduleName() {
        return "framework";
    }

    @Override
    public String dependencyType() {
        return "REDIS";
    }

    @Override
    public Duration timeout() {
        return TIMEOUT;
    }

    @Override
    public DependencyHealthResult check() {
        var start = System.nanoTime();
        try (var connection = connectionFactory.getConnection()) {
            var available = "PONG".equalsIgnoreCase(connection.ping());
            return result(available ? DependencyHealthStatus.UP : DependencyHealthStatus.DOWN,
                    start, available ? null : "REDIS_UNAVAILABLE",
                    available ? "Redis 连接检查正常" : "Redis 连接不可用");
        } catch (RuntimeException exception) {
            return result(DependencyHealthStatus.DOWN, start, "REDIS_CHECK_FAILED", "Redis 连接检查失败");
        }
    }

    private DependencyHealthResult result(DependencyHealthStatus status, long start, String errorCode,
                                          String safeSummary) {
        return new DependencyHealthResult(contributorName(), moduleName(), dependencyType(), status,
                Duration.ofNanos(System.nanoTime() - start), Instant.now(), errorCode, safeSummary);
    }
}
