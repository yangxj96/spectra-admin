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

package com.devops00.spectra.framework.security.session;

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 安全会话活动时间脚本在 Redis 结果不明时拒绝认证。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/10
 */
class SecuritySessionActivityStoreTest {

    @Test
    void missingScriptResultFailsClosed() {
        RedisTemplate<String, Object> redis = mock();
        when(redis.execute(any(RedisScript.class), anyList(), any(), any(), any(), any(), any()))
                .thenReturn(null);

        var activity = new SecuritySessionActivityStore.Activity("access-digest", "family-id", "refresh-digest",
                1000L, Duration.ofSeconds(30), Duration.ofSeconds(300));
        assertThrows(SecurityRedisUnavailableException.class, () -> SecuritySessionActivityStore.touch(redis, activity));
    }
}
