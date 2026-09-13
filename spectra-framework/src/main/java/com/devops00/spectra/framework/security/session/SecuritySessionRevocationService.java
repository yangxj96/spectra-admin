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
import com.devops00.spectra.framework.security.session.token.SecurityTokenAccessor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.access.AccessDeniedException;
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

    private final SecuritySessionHandleStore handleStore;

    private final SecurityTokenAccessor tokenAccessor;

    public SecuritySessionRevocationService(SecuritySessionStore store, SecurityTokenAccessor tokenAccessor) {
        this.store = store;
        this.handleStore = new SecuritySessionHandleStore(store.redis());
        this.tokenAccessor = tokenAccessor;
    }

    /**
     * 撤销访问令牌及其关联会话状态。
     *
     * @param token 待摘要、校验或撤销的访问令牌；不得写入日志。
     */
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
        handleStore.deleteFamilyHandle(familyId);

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

    /**
     * 按刷新令牌撤销关联的访问令牌和会话。
     *
     * @param refreshToken 待轮换或撤销的刷新令牌；不得写入日志。
     */
    @Override
    public void deleteByRefreshToken(String refreshToken) {
        run("按 Refresh Token 撤销会话", () -> deleteByRefreshTokenInternal(refreshToken));
    }

    /** 按随机会话句柄撤销其唯一关联的 Refresh Token Family。 */
    @Override
    public void deleteBySessionId(String sessionId) {
        run("按在线管理句柄撤销会话", () -> deleteBySessionIdInternal(sessionId));
    }

    private void deleteBySessionIdInternal(String sessionId) {
        String familyId = handleStore.resolveFamily(sessionId);
        String familyKey = SecurityRedisKey.SESSION_FAMILY.format(familyId);
        Set<Object> familyTokens = store.members("读取目标 Session Family", familyKey);
        if (familyTokens.isEmpty()) {
            handleStore.deleteFamilyHandle(familyId);
            throw new IllegalArgumentException("会话已失效，请刷新列表");
        }
        rejectCurrentSession(familyId);
        for (Object familyToken : familyTokens) {
            deleteAccessDigest(SecurityRedisValueParser.requiredText(familyToken, "SessionFamily.accessDigest"));
        }
        store.redis().delete(familyKey);
        handleStore.deleteFamilyHandle(familyId);
    }

    /** 拒绝通过在线管理句柄撤销发起当前请求的安全会话。 */
    private void rejectCurrentSession(String targetFamilyId) {
        Map<Object, Object> currentSession = readCurrentSession();
        String currentFamilyId = SecurityRedisValueParser.requiredText(currentSession.get("familyId"), "Session.familyId");
        if (targetFamilyId.equals(currentFamilyId)) {
            throw new AccessDeniedException("不能下线当前正在使用的会话");
        }
    }

    /** 拒绝按客户端批量撤销包含当前请求会话的客户端会话。 */
    private void rejectCurrentClientSession(String targetUserId, ClientType targetClientType) {
        Map<Object, Object> currentSession = readCurrentSession();
        String currentUserId = SecurityRedisValueParser.requiredText(currentSession.get("userId"), "Session.userId");
        String currentClientType = SecurityRedisValueParser.requiredClientType(currentSession.get("clientType"),
                "Session.clientType").getName();
        if (targetUserId.equals(currentUserId) && targetClientType.getName().equals(currentClientType)) {
            throw new AccessDeniedException("不能下线当前正在使用的客户端会话");
        }
    }

    /** 读取并校验当前请求对应的有效会话；无法确认时拒绝会话撤销。 */
    private Map<Object, Object> readCurrentSession() {
        String currentToken = tokenAccessor.getCurrentToken();
        if (currentToken == null || currentToken.isBlank()) {
            throw new AccessDeniedException("无法确认当前会话，拒绝下线");
        }
        String currentAccessDigest = TokenDigestService.digest(currentToken);
        Map<Object, Object> currentSession = store.hash("读取当前操作会话", SecurityRedisKey.SESSION.format(currentAccessDigest));
        if (currentSession.isEmpty()) {
            throw new AccessDeniedException("当前会话已失效，拒绝下线");
        }
        return currentSession;
    }

    /**
     * 删除或清理刷新令牌内部。
     */
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

    /**
     * 撤销用户的全部登录会话。
     *
     * @param userId 目标用户的唯一标识，用于限定会话和授权范围。
     */
    @Override
    public void deleteByUserId(UUID userId) {
        run("按用户撤销会话", () -> deleteByUserIdInternal(userId, null));
    }

    /**
     * 撤销用户除指定令牌外的全部登录会话。
     *
     * @param userId      目标用户的唯一标识，用于限定会话和授权范围。
     * @param accessToken 待排除或撤销的访问令牌；不得写入日志。
     */
    @Override
    public void deleteByUserIdExceptToken(UUID userId, String accessToken) {
        run("按用户撤销除当前会话外的其他会话", () -> deleteByUserIdInternal(userId, accessToken));
    }

    /**
     * 删除或清理用户标识内部。
     */
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

    /**
     * 删除或清理过期状态访问会话状态。
     */
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

    /**
     * 按用户和客户端类型撤销登录会话。
     *
     * @param userId     目标用户的唯一标识，用于限定会话和授权范围。
     * @param clientType 客户端类型，用于选择对应的安全会话策略。
     */
    @Override
    public void deleteByUserIdAndClient(String userId, ClientType clientType) {
        run("按用户和客户端撤销会话", () -> {
            String normalizedUserId = SecurityRedisValueParser.requiredText(userId, "userId");
            Objects.requireNonNull(clientType, "clientType");
            rejectCurrentClientSession(normalizedUserId, clientType);
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
