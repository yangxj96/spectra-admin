package com.devops00.spectra.framework.security.redis.store;

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 安全计数脚本返回值的拒绝边界。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/09
 */
class SecurityRedisCounterTest {

    @Test
    void unknownAndInvalidIncrementRepliesCannotBecomeSuccessfulAttempts() {
        assertThrows(SecurityRedisUnavailableException.class,
                () -> SecurityRedisCounter.increment(resultTemplate(null), "synthetic", Duration.ofMinutes(1)));
        assertThrows(SecurityRedisUnavailableException.class,
                () -> SecurityRedisCounter.increment(resultTemplate(0L), "synthetic", Duration.ofMinutes(1)));
        assertThrows(SecurityRedisUnavailableException.class,
                () -> SecurityRedisCounter.increment(resultTemplate(-1L), "synthetic", Duration.ofMinutes(1)));
    }

    @Test
    void missingCounterIsZeroButNegativeReadReplyIsUnknown() {
        assertEquals(0L, SecurityRedisCounter.current(resultTemplate(0L), "synthetic"));
        assertThrows(SecurityRedisUnavailableException.class,
                () -> SecurityRedisCounter.current(resultTemplate(-1L), "synthetic"));
    }

    private static RedisTemplate<String, Object> resultTemplate(Long result) {
        return new RedisTemplate<>() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T execute(RedisScript<T> script, List<String> keys, Object... args) {
                return (T) result;
            }
        };
    }
}
