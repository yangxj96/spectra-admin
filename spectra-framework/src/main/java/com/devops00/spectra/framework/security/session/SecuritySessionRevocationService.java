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

import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.store.RefreshTokenRotationStore;
import com.devops00.spectra.framework.security.redis.token.TokenDigestService;
import com.devops00.spectra.framework.security.redis.value.SecurityRedisValueParser;
import com.devops00.spectra.framework.security.session.lifecycle.SecuritySessionRevoker;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.devops00.spectra.framework.security.redis.key.SecurityRedisExecutor.run;

/**
 * 安全 Session 撤销用例，集中处理 Token、用户、设备和 Family 清理。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/07
 */
@Component
@NullMarked
public class SecuritySessionRevocationService implements SecuritySessionRevoker {

    private final SecuritySessionStore store;

    public SecuritySessionRevocationService(SecuritySessionStore store) {
        this.store = store;
    }

    @Override
    public void deleteToken(String token) {
        run("撤销 Access Token", () -> deleteAccessDigest(TokenDigestService.digest(token)));
    }

    /** 按 Access Token digest 撤销会话，供签发和刷新用例复用。 */
    void deleteAccessDigest(String tokenDigest) {
        String sessionKey = SecurityRedisKey.SESSION.format(tokenDigest);
        Map<Object, Object> session = store.hash("读取待撤销安全会话", sessionKey);
        if (session.isEmpty()) {
            return;
        }

        String userId = SecurityRedisValueParser.requiredText(session.get("userId"), "Session.userId");
        String clientType = SecurityRedisValueParser.requiredText(session.get("clientType"), "Session.clientType");
        String userTokensKey = SecurityRedisKey.USER_TOKENS.format(userId);
        String familyId = SecurityRedisValueParser.requiredText(session.get("familyId"), "Session.familyId");

        Object refreshDigestValue = store.value("读取 Access Refresh 映射", SecurityRedisKey.REFRESH_TOKEN.format(tokenDigest));
        if (refreshDigestValue != null) {
            String refreshDigest = SecurityRedisValueParser.requiredText(refreshDigestValue, "Access.refreshDigest");
            RefreshTokenRotationStore.compareAndDelete(
                    store.redis(), SecurityRedisKey.REFRESH_TOKEN.format(tokenDigest), refreshDigest);
            store.redis().delete(SecurityRedisKey.REFRESH_TOKEN.format(refreshDigest));
            store.redis().delete(SecurityRedisKey.REFRESH_CLAIM.format(refreshDigest));
        }
        store.redis().delete(SecurityRedisKey.REFRESH_TOKEN.format(tokenDigest));

        store.redis().delete(sessionKey);
        store.redis().delete(SecurityRedisKey.SESSION_SUMMARY.format(tokenDigest));
        store.redis().opsForSet().remove(userTokensKey, tokenDigest);
        store.redis().opsForSet().remove(SecurityRedisKey.ONLINE_SESSIONS.getPattern(), tokenDigest);
        deleteRefreshFamily(familyId);
        store.redis().opsForSet().remove(SecurityRedisKey.SESSION_FAMILY.format(familyId), tokenDigest);

        Long remaining = store.redis().opsForSet().size(userTokensKey);
        if (remaining == null) {
            throw new SecurityRedisUnavailableException("安全 Redis 未返回剩余会话数量", null);
        }
        if (remaining == 0) {
            store.redis().opsForSet().remove(SecurityRedisKey.ONLINE_USERS.getPattern(), userId);
            store.redis().delete(userTokensKey);
        }
        Object currentAccessDigest = store.value("读取用户客户端会话映射",
                SecurityRedisKey.USER_CLIENT.format(userId, clientType));
        if (currentAccessDigest != null) {
            SecurityRedisValueParser.requiredText(currentAccessDigest, "UserClient.accessDigest");
            RefreshTokenRotationStore.compareAndDelete(
                    store.redis(), SecurityRedisKey.USER_CLIENT.format(userId, clientType), tokenDigest);
        }
    }

    @Override
    public void deleteByRefreshToken(String refreshToken) {
        run("按 Refresh Token 撤销会话", () -> deleteByRefreshTokenInternal(refreshToken));
    }

    private void deleteByRefreshTokenInternal(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        String refreshDigest = TokenDigestService.digest(refreshToken);
        String refreshKey = SecurityRedisKey.REFRESH_TOKEN.format(refreshDigest);
        Map<Object, Object> refreshData = store.hash("读取待撤销 Refresh Token", refreshKey);
        if (refreshData.isEmpty()) {
            store.redis().delete(SecurityRedisKey.REFRESH_CLAIM.format(refreshDigest));
            return;
        }

        String accessDigest = SecurityRedisValueParser.requiredText(refreshData.get("accessToken"), "Refresh.accessToken");
        String userId = SecurityRedisValueParser.requiredText(refreshData.get("userId"), "Refresh.userId");
        String familyId = SecurityRedisValueParser.requiredText(refreshData.get("familyId"), "Refresh.familyId");
        deleteAccessDigest(accessDigest);
        deleteRefreshFamily(familyId);
        store.redis().delete(refreshKey);
        store.redis().delete(SecurityRedisKey.REFRESH_CLAIM.format(refreshDigest));
        store.redis().opsForSet().remove(SecurityRedisKey.ONLINE_USERS.getPattern(), userId);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        run("按用户撤销会话", () -> deleteByUserIdInternal(userId, null));
    }

    @Override
    public void deleteByUserIdExceptToken(UUID userId, String accessToken) {
        run("按用户撤销除当前会话外的其他会话", () -> deleteByUserIdInternal(userId, accessToken));
    }

    private void deleteByUserIdInternal(UUID userId, String accessToken) {
        Objects.requireNonNull(userId, "userId");
        String userTokensKey = SecurityRedisKey.USER_TOKENS.format(userId);
        Set<Object> tokens = store.members("读取用户会话索引", userTokensKey);
        String exceptDigest = accessToken == null || accessToken.isBlank() ? null : TokenDigestService.digest(accessToken);
        for (Object token : tokens) {
            String tokenDigest = SecurityRedisValueParser.requiredText(token, "UserTokens.accessDigest");
            if (!Objects.equals(exceptDigest, tokenDigest)) {
                Map<Object, Object> session = store.hash("读取用户会话", SecurityRedisKey.SESSION.format(tokenDigest));
                if (session.isEmpty()) {
                    deleteExpiredAccessSessionState(tokenDigest);
                    store.redis().opsForSet().remove(userTokensKey, tokenDigest);
                } else {
                    deleteAccessDigest(tokenDigest);
                }
            }
        }
        Long remaining = store.redis().opsForSet().size(userTokensKey);
        if (remaining == null) {
            throw new SecurityRedisUnavailableException("安全 Redis 未返回用户剩余会话数量", null);
        }
        if (remaining == 0) {
            store.redis().delete(userTokensKey);
            store.redis().opsForSet().remove(SecurityRedisKey.ONLINE_USERS.getPattern(), userId.toString());
        }
    }

    private void deleteExpiredAccessSessionState(String accessDigest) {
        String accessRefreshKey = SecurityRedisKey.REFRESH_TOKEN.format(accessDigest);
        Object refreshDigestValue = store.value("读取过期 Access Refresh 映射", accessRefreshKey);
        if (refreshDigestValue == null) {
            return;
        }
        String refreshDigest = SecurityRedisValueParser.requiredText(refreshDigestValue, "Access.refreshDigest");
        String refreshKey = SecurityRedisKey.REFRESH_TOKEN.format(refreshDigest);
        Map<Object, Object> refreshData = store.hash("读取过期会话 Refresh Token", refreshKey);
        String familyId = SecurityRedisValueParser.requiredText(refreshData.get("familyId"), "Refresh.familyId");
        deleteRefreshFamily(familyId);
        store.redis().opsForSet().remove(SecurityRedisKey.SESSION_FAMILY.format(familyId), accessDigest);
        store.redis().delete(accessRefreshKey);
    }

    @Override
    public void deleteByUserIdAndClient(String userId, ClientType clientType) {
        run("按用户和客户端撤销会话", () -> {
            String normalizedUserId = SecurityRedisValueParser.requiredText(userId, "userId");
            Objects.requireNonNull(clientType, "clientType");
            String ucKey = SecurityRedisKey.USER_CLIENT.format(normalizedUserId, clientType.getName());
            Object token = store.value("读取用户客户端会话", ucKey);
            if (token != null) {
                deleteAccessDigest(SecurityRedisValueParser.requiredText(token, "UserClient.accessDigest"));
            }
        });
    }

    /** 清理 Refresh Family 下所有 Refresh Hash 及其一次性消费声明。 */
    void deleteRefreshFamily(String familyId) {
        String refreshFamilyKey = SecurityRedisKey.REFRESH_FAMILY.format(familyId);
        Set<Object> refreshDigests = store.members("读取 Refresh Family", refreshFamilyKey);
        for (Object refreshDigest : refreshDigests) {
            String digest = SecurityRedisValueParser.requiredText(refreshDigest, "RefreshFamily.digest");
            store.redis().delete(SecurityRedisKey.REFRESH_TOKEN.format(digest));
            store.redis().delete(SecurityRedisKey.REFRESH_CLAIM.format(digest));
        }
        store.redis().delete(refreshFamilyKey);
    }

    /** Refresh Rotation 重放或部分写入失败时撤销整个 Access Token Family。 */
    void revokeFamilyForRefreshReplay(String familyId) {
        Set<Object> familyTokens = store.members("读取 Session Family", SecurityRedisKey.SESSION_FAMILY.format(familyId));
        for (Object familyToken : familyTokens) {
            deleteAccessDigest(SecurityRedisValueParser.requiredText(familyToken, "SessionFamily.accessDigest"));
        }
        store.redis().delete(SecurityRedisKey.SESSION_FAMILY.format(familyId));
    }

}
