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

package com.devops00.spectra.security.starter.listener;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.security.base.constant.AuthRedisKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/// Redis Key过期监听
///
/// > 需要再redis中开启`notify-keyspace-events Ex`和`maxmemory-policy noeviction`
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/2/8 20:22
@Slf4j
@Component
public class SecurityRedisKeyExpirationListener implements MessageListener {

    private static final String SESSION_PREFIX = "auth:session:token:";

    private final RedisTemplate<String, Object> redis;

    public SecurityRedisKeyExpirationListener(RedisTemplate<String, Object> redis) {
        this.redis = redis;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.debug("{}监听到redis key过期", LogPrefix.SECURITY.p());
        var expiredKey = new String(message.getBody(), StandardCharsets.UTF_8);

        if (!expiredKey.startsWith(SESSION_PREFIX)) {
            return;
        }

        var token = expiredKey.substring(SESSION_PREFIX.length());

        log.debug("{}Session过期, token={}", LogPrefix.SECURITY.p(), token);

        // 触发“轻量清理”
        cleanupTokenIndexes(token);
    }

    private void cleanupTokenIndexes(String token) {

        // 全局在线 token
        redis.opsForSet().remove(AuthRedisKey.SESSION_ONLINE.getPattern(), token);

        // token -> userId（如果你保留了这个 key）
        var userIdObj = redis.opsForValue()
                .get(AuthRedisKey.TOKEN_USER.format(token));

        if (userIdObj == null) {
            return;
        }

        var userId = userIdObj.toString();

        // user -> tokens
        redis.opsForSet()
                .remove(AuthRedisKey.USER_TOKENS.format(userId), token);

        // 如果用户已无任何 session，可顺手移除在线用户
        var remain = redis.opsForSet()
                .size(AuthRedisKey.USER_TOKENS.format(userId));

        if (remain != null && remain == 0) {
            redis.opsForSet()
                    .remove(AuthRedisKey.ONLINE_USER_IDS.getPattern(), userId);
        }

        // 清理用户的 client token
        redis.opsForSet().remove(AuthRedisKey.USER_CLIENT_TOKENS.format(userId, token), token);
    }
}
