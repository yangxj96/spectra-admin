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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Redis Key过期监听（日志记录，实际清理由业务层懒处理）
 *
 * <p>
 * 需要在 Redis 中开启 {@code notify-keyspace-events Ex}
 * </p>
 *
 * @author yangxj96
 * @version 2.0
 * @since 2026/2/8 20:22
 */
@Slf4j
@Component
public class SecurityRedisKeyExpirationListener implements MessageListener {

    private static final String SESSION_PREFIX = "auth:sess:";

    @Override
    public void onMessage(Message message, byte[] pattern) {
        var expiredKey = new String(message.getBody(), StandardCharsets.UTF_8);

        if (!expiredKey.startsWith(SESSION_PREFIX)) {
            return;
        }

        log.debug("{}SecuritySession 已过期", LogPrefix.SECURITY.p());
    }
}
