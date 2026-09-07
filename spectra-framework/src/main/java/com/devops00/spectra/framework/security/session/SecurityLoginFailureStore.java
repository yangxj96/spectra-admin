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
import com.devops00.spectra.framework.security.redis.key.SecurityRedisExecutor;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.value.SecurityRedisValueParser;
import com.devops00.spectra.framework.security.session.lifecycle.SecurityLoginFailureTracker;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 登录失败计数用例，所有计数解析失败均保持安全 Redis 不可用语义。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/07
 */
@Component
@NullMarked
public class SecurityLoginFailureStore implements SecurityLoginFailureTracker {

    private final SecuritySessionStore store;

    public SecurityLoginFailureStore(SecuritySessionStore store) {
        this.store = store;
    }

    @Override
    public void recordLoginFail(String username) {
        SecurityRedisExecutor.run("记录登录失败次数", () -> {
            String key = SecurityRedisKey.LOGIN_FAIL.format(username);
            Long count = SecurityRedisExecutor.require("记录登录失败次数",
                    () -> store.redis().opsForValue().increment(key));
            if (count == 1L && store.properties().getLockoutSeconds() > 0) {
                store.redis().expire(key, Duration.ofSeconds(store.properties().getLockoutSeconds()));
            }
        });
    }

    @Override
    public boolean isLockedOut(String username) {
        return SecurityRedisExecutor.execute("读取登录失败锁定状态", () -> {
            if (store.properties().getLockoutSeconds() <= 0) {
                return false;
            }
            Object value = store.value("读取登录失败次数", SecurityRedisKey.LOGIN_FAIL.format(username));
            if (value == null) {
                return false;
            }
            long count = SecurityRedisValueParser.requiredLong(value, "LoginFailure.count");
            if (count < 0) {
                throw new SecurityRedisUnavailableException("安全 Redis 登录失败次数无效", null);
            }
            return count >= store.properties().getLockoutMaxAttempts();
        });
    }

    @Override
    public void clearLoginFail(String username) {
        SecurityRedisExecutor.run("清理登录失败次数",
                () -> store.redis().delete(SecurityRedisKey.LOGIN_FAIL.format(username)));
    }
}
