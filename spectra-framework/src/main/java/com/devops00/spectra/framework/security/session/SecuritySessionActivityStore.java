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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;

/**
 * 原子维护 Access 与当前 Refresh 的活动时间；撤销期间不会重建已删除的安全键。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/10
 */
final class SecuritySessionActivityStore {

    private static final RedisScript<Long> TOUCH_SCRIPT = RedisScript.of("""
            if redis.call('EXISTS', KEYS[3]) == 1 then return 0 end
            if redis.call('EXISTS', KEYS[1]) == 0 then return 0 end
            if redis.call('EXISTS', KEYS[5]) == 0 then return -1 end
            if tonumber(ARGV[4]) == 1 then
              if redis.call('GET', KEYS[4]) ~= ARGV[5] then return 0 end
              if redis.call('EXISTS', KEYS[2]) == 0 then return 0 end
            end
            local accessTime = tonumber(redis.call('HGET', KEYS[1], 'lastActiveTime'))
            if not accessTime then return -1 end
            local now = tonumber(ARGV[1])
            if accessTime > now then return 0 end
            if tonumber(ARGV[4]) == 1 then
              local refreshTime = tonumber(redis.call('HGET', KEYS[2], 'lastActiveTime'))
              if not refreshTime then return -1 end
              if refreshTime > now then return 0 end
              redis.call('HSET', KEYS[2], 'lastActiveTime', ARGV[1])
              redis.call('PEXPIRE', KEYS[2], ARGV[3])
            end
            redis.call('HSET', KEYS[1], 'lastActiveTime', ARGV[1])
            redis.call('PEXPIRE', KEYS[1], ARGV[2])
            redis.call('PEXPIRE', KEYS[5], ARGV[2])
            return 1
            """, Long.class);

    private SecuritySessionActivityStore() {
    }

    record Activity(String tokenDigest, String familyId, String refreshDigest, long now,
                    Duration accessTtl, Duration refreshTtl) {
    }

    static boolean touch(RedisTemplate<String, Object> redis, Activity activity) {
        boolean hasRefresh = activity.refreshDigest() != null;
        String sessionKey = SecurityRedisKey.SESSION.format(activity.tokenDigest());
        String refreshKey = hasRefresh ? SecurityRedisKey.REFRESH_TOKEN.format(activity.refreshDigest()) : sessionKey;
        Long result = SecurityRedisExecutor.require("原子更新安全会话活动时间", () -> redis.execute(TOUCH_SCRIPT,
                List.of(sessionKey, refreshKey, SecurityRedisKey.REFRESH_REPLAY_FENCE.format(activity.familyId()),
                        SecurityRedisKey.REFRESH_TOKEN.format(activity.tokenDigest()),
                        SecurityRedisKey.SESSION_SUMMARY.format(activity.tokenDigest())),
                activity.now(), activity.accessTtl().toMillis(), activity.refreshTtl().toMillis(), hasRefresh ? 1 : 0,
                hasRefresh ? activity.refreshDigest() : ""));
        if (result == 1L) {
            return true;
        }
        if (result == 0L) {
            return false;
        }
        throw new SecurityRedisUnavailableException("安全 Redis 会话活动时间状态无效", null);
    }
}
