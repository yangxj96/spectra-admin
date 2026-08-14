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

package com.devops00.spectra.security.base.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

/**
 * 验证码 Redis 一次性消费工具。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public final class VerificationCodeRedisStore {

    private static final RedisScript<Long> COMPARE_AND_DELETE_SCRIPT = new DefaultRedisScript<>("""
            local current = redis.call('GET', KEYS[1])
            if current ~= false and current == ARGV[1] then
                redis.call('DEL', KEYS[1])
                return 1
            end
            return 0
            """, Long.class);

    private VerificationCodeRedisStore() {
    }

    /**
     * 只有 Redis 中的值与期望值完全相同才删除，比较和删除在同一脚本中完成。
     *
     * @param redisTemplate Redis 模板
     * @param key           验证码 key
     * @param expectedValue 用户提交的验证码或摘要
     * @return 是否成功消费
     */
    public static boolean compareAndDelete(RedisTemplate<String, Object> redisTemplate, String key, String expectedValue) {
        if (expectedValue == null || expectedValue.isBlank()) {
            return false;
        }
        Long result = redisTemplate.execute(COMPARE_AND_DELETE_SCRIPT, List.of(key), expectedValue);
        return Long.valueOf(1L).equals(result);
    }
}
