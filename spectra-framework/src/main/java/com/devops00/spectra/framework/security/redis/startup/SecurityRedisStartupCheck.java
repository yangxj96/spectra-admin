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

package com.devops00.spectra.framework.security.redis.startup;

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 安全 Redis 启动连通门。
 *
 * <p>安全 Redis 是 Token、Session、防重放和登录失败锁定的事实源。应用必须在启动阶段确认其可用，
 * 否则拒绝启动（非零退出），不能带病启动后在运行期逐请求失败。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@Component
public final class SecurityRedisStartupCheck {

    public SecurityRedisStartupCheck(
                                     @Qualifier("securityRedisTemplate") RedisTemplate<String, Object> securityRedisTemplate) {
        verify(securityRedisTemplate);
    }

    private static void verify(RedisTemplate<String, Object> securityRedisTemplate) {
        try {
            var factory = securityRedisTemplate.getConnectionFactory();
            if (factory == null) {
                throw new SecurityRedisUnavailableException("安全 Redis 连接工厂未装配，拒绝启动", null);
            }
            try (var connection = factory.getConnection()) {
                if (connection == null) {
                    throw new SecurityRedisUnavailableException("安全 Redis 连接不可用，拒绝启动", null);
                }
                connection.ping();
            }
        } catch (SecurityRedisUnavailableException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new SecurityRedisUnavailableException("安全 Redis 启动校验失败，拒绝启动", exception);
        }
    }
}
