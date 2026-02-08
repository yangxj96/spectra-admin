package io.github.yangxj96.spectra.core.configure.security.renew;

import io.github.yangxj96.spectra.core.configure.security.javabean.AuthRedisKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisSessionRenewService implements SessionRenewService {

    private static final double RENEW_THRESHOLD = 0.3;

    private final StringRedisTemplate redis;

    public RedisSessionRenewService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean tryRenew(String token) {

        String sessionKey = AuthRedisKey.SESSION_TOKEN_DETAIL.format(token);

        Long remain = redis.getExpire(sessionKey, TimeUnit.SECONDS);
        if (remain == null || remain <= 0) {
            return false;
        }

        Object ttlObj = redis.opsForHash().get(sessionKey, "ttlSeconds");
        if (ttlObj == null) {
            return false;
        }

        long originalTtl = Long.parseLong(ttlObj.toString());

        // 剩余时间足够，不续
        if (remain > originalTtl * RENEW_THRESHOLD) {
            return false;
        }

        // 是否允许 sliding
        Object slidingObj = redis.opsForHash().get(sessionKey, "sliding");
        if (slidingObj != null && !"true".equals(slidingObj.toString())) {
            return false;
        }

        // 续期
        redis.expire(sessionKey, originalTtl, TimeUnit.SECONDS);

        // 更新最后活跃时间（非必须）
        redis.opsForHash().put(
                sessionKey,
                "lastActiveAt",
                System.currentTimeMillis()
        );

        return true;
    }
}
