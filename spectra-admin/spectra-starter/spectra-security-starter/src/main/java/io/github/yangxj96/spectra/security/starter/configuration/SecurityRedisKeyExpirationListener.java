package io.github.yangxj96.spectra.security.starter.configuration;

import io.github.yangxj96.spectra.security.base.constant.AuthRedisKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/// Redis Key过期监听
///
/// 需要再redis中开启`notify-keyspace-events Ex`和`maxmemory-policy noeviction`
@Slf4j
@Component
public class SecurityRedisKeyExpirationListener implements MessageListener {

    private static final String SESSION_PREFIX = "auth:session:token:";

    private final RedisTemplate<String, Object> redis;

    public SecurityRedisKeyExpirationListener(
            @Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redis
    ) {
        this.redis = redis;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.debug("[Security] 监听到redis key过期");
        var expiredKey = new String(message.getBody(), StandardCharsets.UTF_8);

        if (!expiredKey.startsWith(SESSION_PREFIX)) {
            return;
        }

        var token = expiredKey.substring(SESSION_PREFIX.length());

        log.debug("[Security] Session expired, token={}", token);

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
