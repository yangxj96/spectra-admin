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
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.port.security.SecurityToken;
import com.devops00.spectra.common.security.policy.SessionPolicy;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisExecutor;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.token.TokenDigestService;
import com.devops00.spectra.framework.security.redis.value.SecurityRedisValueParser;
import com.devops00.spectra.framework.security.session.lifecycle.SecuritySessionIssuer;
import com.devops00.spectra.framework.security.session.concurrency.SessionConcurrencyStrategyResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 安全 Session 签发用例，负责创建会话和不可变的非敏感摘要。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/07
 */
@Component
@NullMarked
public class SecuritySessionIssueService implements SecuritySessionIssuer {

    private static final String HEADER_CLIENT_TYPE = "X-Client-Type";

    private static final String HEADER_DEVICE_ID = "X-Device-Id";

    private final SecuritySessionStore store;

    private final SecuritySessionRevocationService revocationService;

    private final SessionConcurrencyStrategyResolver concurrencyStrategyResolver;

    public SecuritySessionIssueService(SecuritySessionStore store,
                                       SecuritySessionRevocationService revocationService,
                                       SessionConcurrencyStrategyResolver concurrencyStrategyResolver) {
        this.store = store;
        this.revocationService = revocationService;
        this.concurrencyStrategyResolver = concurrencyStrategyResolver;
    }

    /**
     * 创建并持久化访问令牌及刷新令牌对应的安全状态。
     *
     * @param user 当前认证用户资料，不包含可记录的敏感凭据。
     * @return 返回新建会话的访问令牌和刷新令牌；会话状态写入 Redis 或策略校验失败时抛出异常，不返回 null。
     */
    @Override
    public SecurityToken createToken(SecurityPrincipal user) {
        return SecurityRedisExecutor.execute("签发安全会话", () -> createToken(user, resolveClientType(), UUID.randomUUID().toString()));
    }

    /**
     * 创建并持久化访问令牌及刷新令牌对应的安全状态。
     *
     * @param user       当前认证用户资料，不包含可记录的敏感凭据。
     * @param clientType 客户端类型，用于选择对应的安全会话策略。
     * @return 返回按指定客户端策略新建的访问令牌和刷新令牌；策略、令牌状态或 Redis 写入失败时抛出异常，不返回 null。
     */
    @Override
    public SecurityToken createToken(SecurityPrincipal user, ClientType clientType) {
        return SecurityRedisExecutor.execute("签发安全会话",
                () -> createToken(user, clientType, UUID.randomUUID().toString()));
    }

    /** 按已有 Token Family 创建下一代会话，供 Refresh 用例调用。 */
    SecurityToken createToken(SecurityPrincipal user, ClientType clientType, String familyId) {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(clientType, "clientType");
        Objects.requireNonNull(familyId, "familyId");

        String userId = user.getId().toString();
        String clientCode = clientType.getName();
        String userTokensKey = SecurityRedisKey.USER_TOKENS.format(userId);
        SessionPolicy policy = store.sessionPolicy(clientCode);
        Set<String> activeTokens = activeTokenDigests(userId);
        concurrencyStrategyResolver.resolve(policy.concurrencyMode())
                .enforce(policy, clientCode, activeTokens, revocationService::deleteAccessDigest);

        Duration accessTtl = Duration.ofSeconds(policy.accessTtlSeconds());
        Duration refreshTtl = Duration.ofSeconds(policy.refreshTtlSeconds());
        String token = TokenDigestService.generateToken();
        String refreshToken = TokenDigestService.generateToken();
        String tokenDigest = TokenDigestService.digest(token);
        String refreshDigest = TokenDigestService.digest(refreshToken);
        long now = System.currentTimeMillis();

        Map<String, Object> session = new LinkedHashMap<>();
        session.put("userId", userId);
        session.put("username", user.getUsername());
        session.put("clientType", clientCode);
        session.put("deviceId", resolveDeviceId());
        session.put("ip", resolveClientIp());
        session.put("loginTime", now);
        session.put("lastActiveTime", now);
        session.put("familyId", familyId);

        RedisTemplate<String, Object> redis = store.redis();
        String sessionKey = SecurityRedisKey.SESSION.format(tokenDigest);
        String ucKey = SecurityRedisKey.USER_CLIENT.format(userId, clientCode);
        String accessRefreshKey = SecurityRedisKey.REFRESH_TOKEN.format(tokenDigest);
        String sessionFamilyKey = SecurityRedisKey.SESSION_FAMILY.format(familyId);
        Map<String, Object> refreshData = new LinkedHashMap<>();
        refreshData.put("accessToken", tokenDigest);
        refreshData.put("userId", userId);
        refreshData.put("clientType", clientCode);
        refreshData.put("familyId", familyId);
        String refreshKey = SecurityRedisKey.REFRESH_TOKEN.format(refreshDigest);
        String refreshFamilyKey = SecurityRedisKey.REFRESH_FAMILY.format(familyId);
        String summaryKey = SecurityRedisKey.SESSION_SUMMARY.format(tokenDigest);
        Map<String, Object> summary = new LinkedHashMap<>(session);
        summary.remove("familyId");
        var keys = new PartialSessionKeys(sessionKey, ucKey, userTokensKey, accessRefreshKey, sessionFamilyKey,
                refreshKey, refreshFamilyKey, summaryKey);
        var identity = new PartialSessionIdentity(tokenDigest, refreshDigest, userId);
        try {
            redis.opsForHash().putAll(sessionKey, session);
            redis.expire(sessionKey, accessTtl);
            redis.opsForValue().set(ucKey, tokenDigest, accessTtl);
            redis.opsForSet().add(userTokensKey, tokenDigest);
            redis.expire(userTokensKey, refreshTtl);
            redis.opsForValue().set(accessRefreshKey, refreshDigest, refreshTtl);
            redis.opsForSet().add(sessionFamilyKey, tokenDigest);
            redis.expire(sessionFamilyKey, refreshTtl);
            redis.opsForHash().putAll(refreshKey, refreshData);
            redis.expire(refreshKey, refreshTtl);
            redis.opsForSet().add(refreshFamilyKey, refreshDigest);
            redis.expire(refreshFamilyKey, refreshTtl);
            redis.opsForSet().add(SecurityRedisKey.ONLINE_USERS.getPattern(), userId);
            redis.opsForSet().add(SecurityRedisKey.ONLINE_SESSIONS.getPattern(), tokenDigest);
            redis.opsForValue().set(summaryKey, summary, accessTtl);

            var authorities = user.getAuthorityNames()
                    .stream()
                    .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                    .toList();
            var authentication = new UsernamePasswordAuthenticationToken(user, token, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return buildToken(user, token, refreshToken);
        } catch (RuntimeException exception) {
            cleanupPartialSession(redis, keys, identity);
            throw exception;
        }
    }

    private void cleanupPartialSession(RedisTemplate<String, Object> redis, PartialSessionKeys keys,
                                       PartialSessionIdentity identity) {
        deleteQuietly(() -> redis.delete(keys.sessionKey()));
        deleteQuietly(() -> redis.delete(keys.userClientKey()));
        deleteQuietly(() -> redis.delete(keys.accessRefreshKey()));
        deleteQuietly(() -> redis.delete(keys.refreshKey()));
        deleteQuietly(() -> redis.delete(keys.refreshFamilyKey()));
        deleteQuietly(() -> redis.delete(keys.summaryKey()));
        deleteQuietly(() -> redis.opsForSet().remove(keys.userTokensKey(), identity.tokenDigest()));
        deleteQuietly(() -> redis.opsForSet().remove(keys.sessionFamilyKey(), identity.tokenDigest()));
        deleteQuietly(() -> redis.opsForSet()
                .remove(SecurityRedisKey.ONLINE_SESSIONS.getPattern(),
                        identity.tokenDigest()));
        deleteQuietly(() -> redis.opsForSet().remove(SecurityRedisKey.ONLINE_USERS.getPattern(), identity.userId()));
        deleteQuietly(() -> redis.opsForSet().remove(keys.refreshFamilyKey(), identity.refreshDigest()));
    }

    private record PartialSessionKeys(String sessionKey, String userClientKey, String userTokensKey,
                                      String accessRefreshKey, String sessionFamilyKey, String refreshKey,
                                      String refreshFamilyKey, String summaryKey) {
    }

    private record PartialSessionIdentity(String tokenDigest, String refreshDigest, String userId) {
    }

    private void deleteQuietly(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ignored) {
            // 原始 Redis 错误已经保持 fail-closed，清理失败不能覆盖原始原因。
        }
    }

    private Set<String> activeTokenDigests(String userId) {
        Set<Object> tokens = store.members("读取用户会话索引", SecurityRedisKey.USER_TOKENS.format(userId));
        Set<String> active = new java.util.LinkedHashSet<>();
        for (Object token : tokens) {
            String digest = SecurityRedisValueParser.requiredText(token, "UserTokens.accessDigest");
            if (store.hasKey("检查活动安全会话", SecurityRedisKey.SESSION.format(digest))) {
                active.add(digest);
            } else {
                store.redis().opsForSet().remove(SecurityRedisKey.USER_TOKENS.format(userId), digest);
            }
        }
        return active;
    }

    private SecurityToken buildToken(SecurityPrincipal user, String token, String refreshToken) {
        var permissions = new ArrayList<String>();
        for (String authority : user.getAuthorityNames()) {
            if (authority != null && !authority.startsWith("ROLE_")) {
                permissions.add(authority);
            }
        }
        return SecurityToken.builder()
                .id(user.getId())
                .username(user.getUsername())
                .accessToken(token)
                .refreshToken(refreshToken)
                .permissions(permissions)
                .passwordChangeRequired(user.isPasswordChangeRequired())
                .build();
    }

    private ClientType resolveClientType() {
        HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            return ClientType.WEB;
        }
        String headerType = request.getHeader(HEADER_CLIENT_TYPE);
        if (headerType != null && !headerType.isBlank()) {
            return ClientType.fromName(headerType);
        }
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isBlank()) {
            return ClientType.WEB;
        }
        String lower = userAgent.toLowerCase();
        if (lower.contains("miniprogram")
                || lower.contains("wechat")
                || lower.contains("alipay")
                || lower.contains("bytedance")
                || lower.contains("toutiao")) {
            return ClientType.MINI;
        }
        if (lower.contains("uni-app")
                || lower.contains("uniapp")
                || lower.contains("html5plus")
                || lower.contains("uts")
                || lower.contains("okhttp")
                || lower.contains("retrofit")
                || lower.contains("af-android-sdk")
                || lower.contains("alibc")
                || lower.contains("flutter")
                || lower.contains("reactnative")) {
            return ClientType.APP;
        }
        return ClientType.WEB;
    }

    private String resolveDeviceId() {
        HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            return "unknown";
        }
        String deviceId = request.getHeader(HEADER_DEVICE_ID);
        return deviceId == null || deviceId.isBlank() ? "unknown" : deviceId;
    }

    private String resolveClientIp() {
        return com.devops00.spectra.framework.web.request.IpUtils.getClientIP(getHttpServletRequest());
    }

    private @Nullable HttpServletRequest getHttpServletRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes servletAttributes ? servletAttributes.getRequest() : null;
    }
}
