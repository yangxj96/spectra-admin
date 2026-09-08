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

    /**
     * 记录登录失败次数并在达到阈值时锁定登录。
     *
     * @param username 发生登录失败的用户名；用于生成失败计数键，不写入日志。
     */
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

    /**
     * 获取或判断 Framework 的 isLockedOut 结果。
     *
     * @param username 要查询登录失败锁定状态的用户名；用于读取失败计数键，不写入日志。
     * @return 返回用户名当前是否达到登录失败锁定阈值；未锁定或锁定已过期时返回 false，Redis 无法确认状态时抛出异常。
     */
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

    /**
     * 清除已成功认证用户的登录失败计数。
     *
     * @param username 已成功认证并需要清除失败计数的用户名。
     */
    @Override
    public void clearLoginFail(String username) {
        SecurityRedisExecutor.run("清理登录失败次数",
                () -> store.redis().delete(SecurityRedisKey.LOGIN_FAIL.format(username)));
    }
}
