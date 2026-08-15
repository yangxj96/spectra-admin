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

package com.devops00.spectra.security.starter.strategy;

import com.devops00.spectra.security.base.constant.ClientType;
import com.devops00.spectra.security.base.constant.SecurityRedisKey;
import com.devops00.spectra.security.base.mfa.SecurityMfaChallengePort;
import com.devops00.spectra.security.base.mfa.SecurityMfaChallengePort.MfaLoginChallenge;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Redis MFA 预认证挑战存储。 */
@NullMarked
public final class RedisMfaLoginChallengeRepository implements SecurityMfaChallengePort {

    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_CLIENT_TYPE = "clientType";
    private static final String FIELD_STATE = "state";
    private static final String FIELD_FAILURES = "failures";
    private static final String FIELD_EXPIRES_AT = "expiresAt";

    private static final String STATE_ENROLLMENT_REQUIRED = "ENROLLMENT_REQUIRED";
    private static final String STATE_VERIFICATION_REQUIRED = "VERIFICATION_REQUIRED";
    private static final String STATE_ENROLLMENT_COMPLETED = "ENROLLMENT_COMPLETED";

    private static final RedisScript<Long> RECORD_FAILURE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 0 then
                return -1
            end
            local failures = redis.call('HINCRBY', KEYS[1], 'failures', 1)
            if failures >= tonumber(ARGV[1]) then
                redis.call('DEL', KEYS[1])
                return 0
            end
            return failures
            """, Long.class);

    private static final RedisScript<Long> CONSUME_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 1 then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """, Long.class);

    private final RedisTemplate<String, Object> redis;
    private final SecurityProperties properties;

    public RedisMfaLoginChallengeRepository(RedisTemplate<String, Object> redis, SecurityProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    @Override
    public MfaLoginChallenge create(UUID userId, String username, ClientType clientType, boolean enrollmentRequired) {
        String id = UUID.randomUUID().toString();
        long expiresAt = Instant.now().plusSeconds(Math.max(1L, properties.getMfaChallengeExpire())).toEpochMilli();
        Map<String, Object> challenge = new LinkedHashMap<>();
        challenge.put(FIELD_USER_ID, userId.toString());
        challenge.put(FIELD_USERNAME, username);
        challenge.put(FIELD_CLIENT_TYPE, clientType.getName());
        challenge.put(FIELD_STATE, enrollmentRequired ? STATE_ENROLLMENT_REQUIRED : STATE_VERIFICATION_REQUIRED);
        challenge.put(FIELD_FAILURES, 0L);
        challenge.put(FIELD_EXPIRES_AT, expiresAt);

        String key = key(id);
        redis.opsForHash().putAll(key, challenge);
        redis.expire(key, Duration.ofSeconds(Math.max(1L, properties.getMfaChallengeExpire())));
        return new MfaLoginChallenge(id, userId, username, clientType, enrollmentRequired, false, expiresAt);
    }

    @Override
    public MfaLoginChallenge find(String challengeId) {
        if (challengeId == null || challengeId.isBlank()) {
            return null;
        }
        Map<Object, Object> values = redis.opsForHash().entries(key(challengeId));
        if (values.isEmpty()) {
            return null;
        }
        try {
            UUID userId = UUID.fromString(value(values, FIELD_USER_ID));
            String username = value(values, FIELD_USERNAME);
            ClientType clientType = ClientType.fromName(value(values, FIELD_CLIENT_TYPE));
            String state = value(values, FIELD_STATE);
            long expiresAt = Long.parseLong(value(values, FIELD_EXPIRES_AT));
            if (expiresAt <= Instant.now().toEpochMilli()) {
                redis.delete(key(challengeId));
                return null;
            }
            return new MfaLoginChallenge(challengeId, userId, username, clientType,
                    STATE_ENROLLMENT_REQUIRED.equals(state), STATE_ENROLLMENT_COMPLETED.equals(state), expiresAt);
        } catch (IllegalArgumentException | NullPointerException exception) {
            redis.delete(key(challengeId));
            return null;
        }
    }

    @Override
    public boolean recordFailure(String challengeId) {
        Long result = redis.execute(RECORD_FAILURE_SCRIPT, Collections.singletonList(key(challengeId)),
                Integer.toString(Math.max(1, properties.getMfaChallengeMaxAttempts())));
        return result != null && result > 0;
    }

    @Override
    public boolean markEnrollmentCompleted(String challengeId) {
        MfaLoginChallenge challenge = find(challengeId);
        if (challenge == null || !challenge.enrollmentRequired() || challenge.enrollmentCompleted()) {
            return false;
        }
        redis.opsForHash().put(key(challengeId), FIELD_STATE, STATE_ENROLLMENT_COMPLETED);
        return true;
    }

    @Override
    public boolean consume(String challengeId) {
        Long result = redis.execute(CONSUME_SCRIPT, Collections.singletonList(key(challengeId)));
        return result != null && result > 0;
    }

    private String key(String challengeId) {
        return SecurityRedisKey.MFA_CHALLENGE.format(challengeId);
    }

    @Nullable
    private String value(Map<Object, Object> values, String field) {
        Object value = values.get(field);
        return value == null ? null : value.toString();
    }
}
