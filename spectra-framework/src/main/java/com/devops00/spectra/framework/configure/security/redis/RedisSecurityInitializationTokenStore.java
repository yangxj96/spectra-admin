/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.framework.configure.security.redis;

import com.devops00.spectra.common.port.security.SecurityInitializationTokenStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 系统初始化令牌的 Redis 适配器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
@Component
public class RedisSecurityInitializationTokenStore implements SecurityInitializationTokenStore {

    private final RedisTemplate<String, Object> redis;

    public RedisSecurityInitializationTokenStore(
                                                 @Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redis) {
        this.redis = redis;
    }

    @Override
    public boolean putIfAbsent(String digest) {
        Boolean created = SecurityRedisExecutor.require("写入系统初始化令牌",
                () -> redis.opsForValue().setIfAbsent(key(), digest));
        return Boolean.TRUE.equals(created);
    }

    @Override
    public Optional<String> getDigest() {
        Object value = SecurityRedisExecutor.execute("读取系统初始化令牌",
                () -> redis.opsForValue().get(key()));
        return value instanceof String digest ? Optional.of(digest) : Optional.empty();
    }

    @Override
    public void clear() {
        SecurityRedisExecutor.run("删除系统初始化令牌", () -> redis.delete(key()));
    }

    private String key() {
        return SecurityRedisKey.INITIALIZATION_TOKEN.getPattern();
    }
}
