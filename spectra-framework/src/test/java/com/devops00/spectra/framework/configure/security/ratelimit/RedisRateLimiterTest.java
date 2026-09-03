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

package com.devops00.spectra.framework.configure.security.ratelimit;

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Redis-backed API 限流策略与计数语义测试。 */
class RedisRateLimiterTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-01T00:00:30Z"), ZoneOffset.UTC);

    @Test
    void shouldResolveOnlyProtectedApiPolicies() {
        assertThat(RateLimitPolicy.resolve("POST", "/security/authentication/login"))
                .hasValueSatisfying(policy -> {
                    assertThat(policy.name()).isEqualTo("authentication-login");
                    assertThat(policy.maxRequests()).isEqualTo(10);
                    assertThat(policy.subjectDimension()).isEqualTo(RateLimitPolicy.SubjectDimension.IP);
                });
        assertThat(RateLimitPolicy.resolve("POST", "/file/uploads/123/complete")).isPresent();
        assertThat(RateLimitPolicy.resolve("GET", "/actuator/health")).isEmpty();
        assertThat(RateLimitPolicy.resolve("GET", "/system/bootstrap")).isEmpty();
    }

    @Test
    void shouldAllowWithinWindowAndRejectAfterLimit() {
        var policy = new RateLimitPolicy("test", java.time.Duration.ofSeconds(60), 2,
                RateLimitPolicy.SubjectDimension.IP,
                List.of(new RateLimitPolicy.Endpoint("POST", "/test")));
        var counters = new ConcurrentHashMap<String, AtomicLong>();
        var key = new AtomicReference<String>();
        var limiter = new RedisRateLimiter((redisKey, ttlMillis) -> {
            key.set(redisKey);
            long count = counters.computeIfAbsent(redisKey, ignored -> new AtomicLong()).incrementAndGet();
            return new RedisRateLimiter.IncrementResult(count, ttlMillis);
        }, CLOCK);
        var subject = new RateLimitPolicy.Subject("192.0.2.10", null);

        var first = limiter.tryAcquire(policy, subject);
        var second = limiter.tryAcquire(policy, subject);
        var third = limiter.tryAcquire(policy, subject);

        assertThat(first.allowed()).isTrue();
        assertThat(first.remaining()).isEqualTo(1);
        assertThat(second.allowed()).isTrue();
        assertThat(second.remaining()).isZero();
        assertThat(third.allowed()).isFalse();
        assertThat(third.retryAfterSeconds()).isEqualTo(60);
        assertThat(key).hasValueSatisfying(value -> {
            assertThat(value).startsWith("sec:ratelimit:v1:test:");
            assertThat(value).doesNotContain("192.0.2.10");
        });
    }

    @Test
    void shouldKeepSubjectsAndIpHeadersSeparatedByResolvedSubject() {
        var policy = new RateLimitPolicy("test", java.time.Duration.ofMinutes(1), 1,
                RateLimitPolicy.SubjectDimension.IP_AND_USER,
                List.of(new RateLimitPolicy.Endpoint("POST", "/test")));
        var keys = ConcurrentHashMap.<String>newKeySet();
        var limiter = new RedisRateLimiter((key, ttlMillis) -> {
            keys.add(key);
            return new RedisRateLimiter.IncrementResult(1, ttlMillis);
        }, CLOCK);

        limiter.tryAcquire(policy, new RateLimitPolicy.Subject("192.0.2.10", "user-a"));
        limiter.tryAcquire(policy, new RateLimitPolicy.Subject("192.0.2.10", "user-b"));

        assertThat(keys).hasSize(2);
        assertThat(keys).allSatisfy(key -> assertThat(key).doesNotContain("192.0.2.10", "user-a", "user-b"));
    }

    @Test
    void shouldNotOverIssueAllowedRequestsWhenStoreCountsAtomically() {
        var policy = new RateLimitPolicy("concurrent", java.time.Duration.ofSeconds(60), 10,
                RateLimitPolicy.SubjectDimension.IP,
                List.of(new RateLimitPolicy.Endpoint("POST", "/test")));
        var counters = new ConcurrentHashMap<String, AtomicLong>();
        var limiter = new RedisRateLimiter((key, ttlMillis) -> {
            long count = counters.computeIfAbsent(key, ignored -> new AtomicLong()).incrementAndGet();
            return new RedisRateLimiter.IncrementResult(count, ttlMillis);
        }, CLOCK);

        long allowed = IntStream.range(0, 100)
                .parallel()
                .mapToObj(ignored -> limiter.tryAcquire(policy, new RateLimitPolicy.Subject("192.0.2.10", null)))
                .filter(RedisRateLimiter.Decision::allowed)
                .count();

        assertThat(allowed).isEqualTo(10);
    }

    @Test
    void shouldFailClosedWhenRedisStoreIsUnavailable() {
        var policy = new RateLimitPolicy("test", java.time.Duration.ofMinutes(1), 1,
                RateLimitPolicy.SubjectDimension.IP,
                List.of(new RateLimitPolicy.Endpoint("POST", "/test")));
        var limiter = new RedisRateLimiter((key, ttlMillis) -> {
            throw new RedisConnectionFailureException("redis down");
        }, CLOCK);

        assertThatThrownBy(() -> limiter.tryAcquire(policy, new RateLimitPolicy.Subject("192.0.2.10", null)))
                .isInstanceOf(SecurityRedisUnavailableException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldPassWindowTtlAsNumericScriptArgument() {
        var policy = new RateLimitPolicy("test", java.time.Duration.ofMinutes(1), 1,
                RateLimitPolicy.SubjectDimension.IP,
                List.of(new RateLimitPolicy.Endpoint("POST", "/test")));
        var arguments = new AtomicReference<Object[]>();
        RedisTemplate<String, Object> redis = new RedisTemplate<>() {
            @Override
            public <T> T execute(RedisScript<T> script, List<String> keys, Object... args) {
                arguments.set(args);
                return (T) List.of(1L, 60_000L);
            }
        };

        var decision = new RedisRateLimiter(redis).tryAcquire(policy,
                new RateLimitPolicy.Subject("192.0.2.10", null));

        assertThat(decision.allowed()).isTrue();
        assertThat(arguments.get()).containsExactly(60_000L);
    }

    @Test
    void shouldRecoverNaturallyWhenTheFixedWindowChanges() {
        var policy = new RateLimitPolicy("test", java.time.Duration.ofMinutes(1), 1,
                RateLimitPolicy.SubjectDimension.IP,
                List.of(new RateLimitPolicy.Endpoint("POST", "/test")));
        var counters = new ConcurrentHashMap<String, AtomicLong>();
        var store = (RedisRateLimiter.RateLimitStore) (key, ttlMillis) -> {
            long count = counters.computeIfAbsent(key, ignored -> new AtomicLong()).incrementAndGet();
            return new RedisRateLimiter.IncrementResult(count, ttlMillis);
        };
        var subject = new RateLimitPolicy.Subject("192.0.2.10", null);

        var firstWindow = new RedisRateLimiter(store, CLOCK).tryAcquire(policy, subject);
        var nextWindow = new RedisRateLimiter(store,
                Clock.fixed(Instant.parse("2026-09-01T00:01:30Z"), ZoneOffset.UTC))
                .tryAcquire(policy, subject);

        assertThat(firstWindow.allowed()).isTrue();
        assertThat(nextWindow.allowed()).isTrue();
        assertThat(counters).hasSize(2);
    }
}
