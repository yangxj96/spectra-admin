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

import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.port.security.SecurityToken;
import com.devops00.spectra.common.port.security.SecurityUserLoader;
import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.common.security.policy.SessionPolicy;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisExecutor;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.store.RefreshTokenRotationStore;
import com.devops00.spectra.framework.security.redis.token.TokenDigestService;
import com.devops00.spectra.framework.security.redis.value.SecurityRedisValueParser;
import com.devops00.spectra.framework.security.session.lifecycle.SecuritySessionRefresher;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Refresh Token 轮换用例，集中处理一次性消费、重放和 Family 撤销。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/07
 */
@Component
@NullMarked
public class SecuritySessionRefreshService implements SecuritySessionRefresher {

    private final SecuritySessionStore store;

    private final SecuritySessionIssueService issueService;

    private final SecuritySessionRevocationService revocationService;

    private final SecurityUserLoader securityUserLoader;

    public SecuritySessionRefreshService(SecuritySessionStore store,
                                         SecuritySessionIssueService issueService,
                                         SecuritySessionRevocationService revocationService,
                                         SecurityUserLoader securityUserLoader) {
        this.store = store;
        this.issueService = issueService;
        this.revocationService = revocationService;
        this.securityUserLoader = securityUserLoader;
    }

    /**
     * 初始化或配置 Framework 的 refreshByRefreshToken。
     *
     * @param refreshToken 待轮换或撤销的刷新令牌；不得写入日志。
     * @return 返回轮换后的访问令牌和刷新令牌；刷新令牌缺失、过期、重放、所属用户不可用或 Redis 失败时抛出异常，不返回 null。
     */
    @Override
    public SecurityToken refreshByRefreshToken(String refreshToken) {
        return SecurityRedisExecutor.execute("刷新安全会话", () -> refreshInternal(refreshToken));
    }

    /**
     * 刷新内部。
     */
    private SecurityToken refreshInternal(String refreshToken) {
        RefreshContext context = loadContext(refreshToken);
        String refreshDigest = context.refreshDigest();
        SecurityPrincipal currentUser = context.currentUser();
        var parsedClientType = context.clientType();
        SessionPolicy policy = context.policy();
        String replayFenceKey = context.replayFenceKey();
        long fenceTtlSeconds = Math.max(policy.accessTtlSeconds(), policy.refreshTtlSeconds());

        var identity = new RefreshTokenRotationStore.ClaimIdentity(refreshDigest, context.accessDigest(),
                context.userId().toString(), context.familyId());
        RefreshTokenRotationStore.ClaimResult claimResult = RefreshTokenRotationStore.claim(store.redis(), identity,
                fenceTtlSeconds);
        if (claimResult != RefreshTokenRotationStore.ClaimResult.CLAIMED) {
            if (claimResult == RefreshTokenRotationStore.ClaimResult.REPLAY) {
                revocationService.revokeFamilyForRefreshReplay(context.familyId(), fenceTtlSeconds);
                throw new BadCredentialsException("刷新token重放，所属 Token Family 已撤销");
            }
            throw new BadCredentialsException("刷新token无效或已过期");
        }

        try {
            Boolean indexed = SecurityRedisExecutor.require("校验 Refresh 用户会话索引", () -> store.redis()
                    .opsForSet()
                    .isMember(SecurityRedisKey.USER_TOKENS.format(context.userId()), context.accessDigest()));
            if (!indexed) {
                throw new BadCredentialsException("刷新token所属会话已失效");
            }
            removeRotatedAccessSession(context.accessDigest(), refreshDigest, context.userId().toString(),
                    context.clientTypeName(), context.familyId());
            if (store.hasKey("检查 Refresh 重放栅栏", replayFenceKey)) {
                throw new BadCredentialsException("刷新token所属会话已因重放风险撤销");
            }
            return issueService.createToken(currentUser, parsedClientType, context.familyId());
        } catch (RuntimeException exception) {
            revocationService.revokeFamilyForRefreshReplay(context.familyId(), fenceTtlSeconds);
            throw exception;
        }
    }

    private RefreshContext loadContext(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadCredentialsException("刷新token不能为空");
        }
        String refreshDigest = TokenDigestService.digest(refreshToken);
        String refreshKey = SecurityRedisKey.REFRESH_TOKEN.format(refreshDigest);
        Map<Object, Object> refreshData = store.hash("读取 Refresh Token", refreshKey);
        if (refreshData.isEmpty()) {
            throw new BadCredentialsException("刷新token无效或已过期");
        }
        String accessDigest = SecurityRedisValueParser.requiredText(refreshData.get("accessToken"), "Refresh.accessToken");
        UUID userId = SecurityRedisValueParser.requiredUuid(refreshData.get("userId"), "Refresh.userId");
        String familyId = SecurityRedisValueParser.requiredText(refreshData.get("familyId"), "Refresh.familyId");
        String refreshClientType = SecurityRedisValueParser.requiredText(refreshData.get("clientType"), "Refresh.clientType");
        SecurityPrincipal currentUser = securityUserLoader.load(userId);
        if (currentUser == null) {
            throw new BadCredentialsException("刷新token所属账号当前不可用");
        }
        Map<Object, Object> session = store.hash("读取安全会话", SecurityRedisKey.SESSION.format(accessDigest));
        String clientTypeName = session.isEmpty()
                ? refreshClientType
                : SecurityRedisValueParser.requiredText(session.get("clientType"), "Session.clientType");
        var clientType = SecurityRedisValueParser.requiredClientType(clientTypeName, "Refresh.clientType");
        SessionPolicy policy = store.sessionPolicy(clientType.getName());
        String replayFenceKey = SecurityRedisKey.REFRESH_REPLAY_FENCE.format(familyId);
        if (store.hasKey("检查 Refresh 重放栅栏", replayFenceKey)) {
            throw new BadCredentialsException("刷新token所属会话已因重放风险撤销");
        }
        return new RefreshContext(refreshDigest, accessDigest, userId, familyId, clientTypeName,
                clientType, policy, replayFenceKey, currentUser);
    }

    private record RefreshContext(String refreshDigest, String accessDigest, UUID userId,
                                  String familyId, String clientTypeName,
                                  ClientType clientType,
                                  SessionPolicy policy, String replayFenceKey,
                                  SecurityPrincipal currentUser) {
    }

    /**
     * 删除或清理访问会话。
     */
    private void removeRotatedAccessSession(String accessDigest, String refreshDigest, String userId,
                                            String clientType, String familyId) {
        store.redis().delete(SecurityRedisKey.SESSION.format(accessDigest));
        store.redis().delete(SecurityRedisKey.SESSION_SUMMARY.format(accessDigest));
        store.redis().opsForSet().remove(SecurityRedisKey.ONLINE_SESSIONS.getPattern(), accessDigest);

        String accessRefreshKey = SecurityRedisKey.REFRESH_TOKEN.format(accessDigest);
        Object mappedRefreshDigest = store.value("读取 Access Refresh 映射", accessRefreshKey);
        if (mappedRefreshDigest != null) {
            SecurityRedisValueParser.requiredText(mappedRefreshDigest, "Access.refreshDigest");
            RefreshTokenRotationStore.compareAndDelete(store.redis(), accessRefreshKey, refreshDigest);
        }

        String userClientKey = SecurityRedisKey.USER_CLIENT.format(userId, clientType);
        Object mappedAccessDigest = store.value("读取用户客户端会话映射", userClientKey);
        if (mappedAccessDigest != null) {
            SecurityRedisValueParser.requiredText(mappedAccessDigest, "UserClient.accessDigest");
            RefreshTokenRotationStore.compareAndDelete(store.redis(), userClientKey, accessDigest);
        }
        store.redis().opsForSet().remove(SecurityRedisKey.USER_TOKENS.format(userId), accessDigest);
        store.redis().opsForSet().remove(SecurityRedisKey.SESSION_FAMILY.format(familyId), accessDigest);
    }
}
