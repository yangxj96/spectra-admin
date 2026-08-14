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

import com.devops00.spectra.common.utils.IpUtils;
import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.security.base.constant.AuthRedisKey;
import com.devops00.spectra.security.base.constant.ClientType;
import com.devops00.spectra.security.base.holder.SecHolderStrategy;
import com.devops00.spectra.security.base.holder.SecurityUserLoader;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import com.devops00.spectra.security.base.javabean.vo.TokenVO;
import com.devops00.spectra.security.base.javabean.vo.UserOnlineVO;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.base.root.RootAuthorizationPolicy;
import com.devops00.spectra.security.base.session.SessionConcurrencyMode;
import com.devops00.spectra.security.base.session.SessionPolicy;
import com.devops00.spectra.security.base.util.RefreshTokenRotationStore;
import com.devops00.spectra.security.base.util.TokenDigestService;
import com.devops00.spectra.security.starter.web.javabean.converter.UserOnlineConverter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.*;

/**
 * Redis 方式存储 Token（简化版：5 个 key 覆盖全部场景）
 *
 * @author yangxj96
 * @version 2.0
 * @since 2025/12/11 10:06
 */
@Slf4j
@NullMarked
public class RedisSecHolderStrategy implements SecHolderStrategy {

    private static final String HEADER_CLIENT_TYPE = "X-Client-Type";

    private static final String HEADER_DEVICE_ID = "X-Device-Id";

    private final ObjectMapper om;
    private final RedisTemplate<String, Object> redis;
    private final SecurityProperties properties;
    private final UserOnlineConverter userOnlineConverter;

    private final @Nullable SecurityUserLoader securityUserLoader;

    public RedisSecHolderStrategy(@Qualifier("securityObjectMapper") ObjectMapper om,
                                  @Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redis, SecurityProperties properties,
                                  UserOnlineConverter userOnlineConverter, @Nullable SecurityUserLoader securityUserLoader) {
        this.om = om;
        this.redis = redis;
        this.properties = properties;
        this.userOnlineConverter = userOnlineConverter;
        this.securityUserLoader = securityUserLoader;
    }

    @Override
    public String administrators() {
        return RootAuthorizationPolicy.ROOT_ROLE;
    }

    // ==================== Token 创建 & 续期 ====================

    @Override
    public TokenVO createToken(SecurityUser user) {
        return this.createToken(user, resolveClientType());
    }

    @Override
    public TokenVO createToken(SecurityUser user, ClientType clientType) {
        return createToken(user, clientType, UUID.randomUUID().toString());
    }

    private TokenVO createToken(SecurityUser user, ClientType clientType, String familyId) {
        if (properties.isMfaRequiredForDevOps() && isRoot(user) && !isMfaVerified(user)) {
            throw new IllegalStateException("DEV_OPS 必须先完成 MFA 验证");
        }
        String userId = user.getId().toString();
        String ucKey = AuthRedisKey.USER_CLIENT.format(userId, clientType.getName());

        SessionPolicy policy = sessionPolicy();
        Set<Object> activeTokens = activeTokenDigests(userId);
        if (policy.concurrencyMode() == SessionConcurrencyMode.KICK_OLD) {
            for (Object activeToken : activeTokens) {
                String activeDigest = activeToken.toString();
                Map<Object, Object> activeSession = redis.opsForHash().entries(AuthRedisKey.SESSION.format(activeDigest));
                if (clientType.getName().equals(Objects.toString(activeSession.get("clientType"), null))) {
                    deleteAccessDigest(activeDigest);
                }
            }
        } else if (policy.concurrencyMode() == SessionConcurrencyMode.REJECT_NEW
                && activeTokens.size() >= policy.maxSessions()) {
            throw new IllegalStateException("已达到该账号的最大并发会话数");
        }

        Duration accessTtl = Duration.ofSeconds(policy.accessTtlSeconds());
        Duration refreshTtl = Duration.ofSeconds(policy.refreshTtlSeconds());
        String token = TokenDigestService.generateToken();
        String refreshToken = TokenDigestService.generateToken();
        String tokenDigest = TokenDigestService.digest(token);
        String refreshDigest = TokenDigestService.digest(refreshToken);
        String ip = IpUtils.getClientIP(this.getHttpServletRequest());
        long now = System.currentTimeMillis();

        // Redis 只保存 Session 聚合和摘要，绝不保存完整 SecurityUser 或明文 Token。
        Map<String, Object> session = new LinkedHashMap<>();
        session.put("userId", userId);
        session.put("username", user.getUsername());
        session.put("email", user.getEmail());
        session.put("clientType", clientType.getName());
        session.put("deviceId", resolveDeviceId());
        session.put("ip", ip);
        session.put("loginTime", now);
        session.put("lastActiveTime", now);
        session.put("familyId", familyId);
        session.put("aal", isMfaVerified(user) ? "AAL2" : "AAL1");

        String sessionKey = AuthRedisKey.SESSION.format(tokenDigest);
        String userTokensKey = AuthRedisKey.USER_TOKENS.format(userId);
        String rtKey = AuthRedisKey.REFRESH_TOKEN.format(tokenDigest);

        redis.opsForHash().putAll(sessionKey, session);
        redis.expire(sessionKey, accessTtl);
        redis.opsForValue().set(ucKey, tokenDigest, accessTtl);
        redis.opsForSet().add(userTokensKey, tokenDigest);
        redis.expire(userTokensKey, refreshTtl);
        redis.opsForValue().set(rtKey, refreshDigest, refreshTtl);
        redis.opsForSet().add(AuthRedisKey.SESSION_FAMILY.format(familyId), tokenDigest);
        redis.expire(AuthRedisKey.SESSION_FAMILY.format(familyId), refreshTtl);
        // Refresh Hash 只保存摘要、主体标识和 Rotation Family。
        Map<String, Object> rtData = new LinkedHashMap<>();
        rtData.put("accessToken", tokenDigest);
        rtData.put("userId", userId);
        rtData.put("clientType", clientType.getName());
        rtData.put("familyId", familyId);
        String rtRefreshKey = AuthRedisKey.REFRESH_TOKEN.format(refreshDigest);
        redis.opsForHash().putAll(rtRefreshKey, rtData);
        redis.expire(rtRefreshKey, refreshTtl);
        redis.opsForSet().add(AuthRedisKey.ONLINE_USERS.getPattern(), userId);

        // 设置 SecurityContext
        var auth = new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        return buildTokenVO(user, token, refreshToken);
    }

    @Override
    public void refreshToken(String token) {
        String tokenDigest = TokenDigestService.digest(token);
        String sessionKey = AuthRedisKey.SESSION.format(tokenDigest);
        Object userIdObj = redis.opsForHash().get(sessionKey, "userId");
        if (userIdObj == null) {
            return;
        }
        String userId = userIdObj.toString();
        Object clientTypeObj = redis.opsForHash().get(sessionKey, "clientType");
        String clientType = clientTypeObj != null ? clientTypeObj.toString() : ClientType.WEB.getName();

        this.refreshTTL(tokenDigest, userId, clientType);
        redis.opsForHash().put(sessionKey, "lastActiveTime", System.currentTimeMillis());
    }

    @Override
    public TokenVO refreshByRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("刷新token不能为空");
        }
        String refreshDigest = TokenDigestService.digest(refreshToken);
        String rtKey = AuthRedisKey.REFRESH_TOKEN.format(refreshDigest);
        Map<Object, Object> rtData = redis.opsForHash().entries(rtKey);
        if (rtData.isEmpty()) {
            throw new IllegalArgumentException("刷新token无效或已过期");
        }

        String accessDigest = Objects.toString(rtData.get("accessToken"), null);
        String userId = Objects.toString(rtData.get("userId"), null);
        String familyId = Objects.toString(rtData.get("familyId"), null);
        if (accessDigest == null || userId == null || familyId == null) {
            throw new IllegalArgumentException("刷新token数据异常");
        }
        UUID parsedUserId;
        try {
            parsedUserId = UUID.fromString(userId);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("刷新token用户标识异常", exception);
        }
        if (securityUserLoader == null) {
            throw new IllegalStateException("未配置安全主体加载器，拒绝使用旧 SecurityUser 快照刷新");
        }
        SecurityUser currentUser = securityUserLoader.load(parsedUserId);
        if (currentUser == null) {
            throw new IllegalArgumentException("刷新token所属账号当前不可用");
        }

        Duration refreshTtl = Duration.ofSeconds(sessionPolicy().refreshTtlSeconds());
        String replayFenceKey = AuthRedisKey.REFRESH_REPLAY_FENCE.format(familyId);
        if (Boolean.TRUE.equals(redis.hasKey(replayFenceKey))) {
            throw new IllegalArgumentException("刷新token所属会话已因重放风险撤销");
        }

        var claimResult = RefreshTokenRotationStore.claim(redis, rtKey,
                AuthRedisKey.REFRESH_CLAIM.format(refreshDigest), sessionPolicy().refreshTtlSeconds());
        if (claimResult != RefreshTokenRotationStore.ClaimResult.CLAIMED) {
            if (claimResult == RefreshTokenRotationStore.ClaimResult.REPLAY) {
                redis.opsForValue().set(replayFenceKey, "REVOKED", refreshTtl);
                revokeFamilyForRefreshReplay(familyId);
                throw new IllegalArgumentException("刷新token重放，所属 Token Family 已撤销");
            }
            throw new IllegalArgumentException("刷新token无效或已过期");
        }

        String sessionKey = AuthRedisKey.SESSION.format(accessDigest);
        Map<Object, Object> session = redis.opsForHash().entries(sessionKey);
        String clientType = Objects.toString(session.get("clientType"),
                Objects.toString(rtData.get("clientType"), ClientType.WEB.getName()));

        try {
            removeRotatedAccessSession(accessDigest, refreshDigest, userId, clientType, familyId);
            if (Boolean.TRUE.equals(redis.hasKey(replayFenceKey))) {
                throw new IllegalArgumentException("刷新token所属会话已因重放风险撤销");
            }
            return createToken(currentUser, ClientType.fromName(clientType), familyId);
        } catch (RuntimeException exception) {
            revokeFamilyForRefreshReplay(familyId);
            throw exception;
        }
    }

    // ==================== Token 删除 & 踢出 ====================

    @Override
    public void deleteToken(String token) {
        deleteAccessDigest(TokenDigestService.digest(token));
    }

    private void deleteAccessDigest(String tokenDigest) {
        String sessionKey = AuthRedisKey.SESSION.format(tokenDigest);
        Map<Object, Object> session = redis.opsForHash().entries(sessionKey);
        if (session.isEmpty()) {
            return;
        }

        String userId = Objects.toString(session.get("userId"), null);
        String clientType = Objects.toString(session.get("clientType"), null);
        if (userId == null) {
            redis.delete(sessionKey);
            return;
        }

        String ucKey = AuthRedisKey.USER_CLIENT.format(userId, clientType);
        String userTokensKey = AuthRedisKey.USER_TOKENS.format(userId);

        // 清理 refresh token 映射
        // auth:rt:{accessToken} → refreshToken (String)
        Object refreshDigestObj = redis.opsForValue().get(AuthRedisKey.REFRESH_TOKEN.format(tokenDigest));
        if (refreshDigestObj != null) {
            redis.delete(AuthRedisKey.REFRESH_TOKEN.format(refreshDigestObj.toString()));
        }
        redis.delete(AuthRedisKey.REFRESH_TOKEN.format(tokenDigest));

        redis.delete(sessionKey);
        redis.delete(ucKey);
        redis.opsForSet().remove(userTokensKey, tokenDigest);
        Object familyId = session.get("familyId");
        if (familyId != null) {
            redis.opsForSet().remove(AuthRedisKey.SESSION_FAMILY.format(familyId.toString()), tokenDigest);
        }

        // 用户已无任何 token → 移出在线
        Long remain = redis.opsForSet().size(userTokensKey);
        if (remain == null || remain == 0) {
            redis.opsForSet().remove(AuthRedisKey.ONLINE_USERS.getPattern(), userId);
            redis.delete(userTokensKey);
        }
    }

    @Override
    public void deleteByRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        String refreshDigest = TokenDigestService.digest(refreshToken);
        String rtRefreshKey = AuthRedisKey.REFRESH_TOKEN.format(refreshDigest);
        Map<Object, Object> rtData = redis.opsForHash().entries(rtRefreshKey);
        if (rtData.isEmpty()) {
            return;
        }

        String accessDigest = Objects.toString(rtData.get("accessToken"), null);
        String userId = Objects.toString(rtData.get("userId"), null);

        if (accessDigest != null) {
            deleteAccessDigest(accessDigest);
        }
        redis.delete(rtRefreshKey);
        if (userId != null && accessDigest == null) {
            redis.opsForSet().remove(AuthRedisKey.ONLINE_USERS.getPattern(), userId);
        }
    }

    @Override
    public void deleteByUserId(UUID userId) {
        String userTokensKey = AuthRedisKey.USER_TOKENS.format(userId);
        Set<Object> tokens = redis.opsForSet().members(userTokensKey);
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        for (Object t : tokens) {
            this.deleteAccessDigest(t.toString());
        }
    }

    @Override
    public void deleteByUserIdAndClient(String userId, ClientType clientType) {
        String ucKey = AuthRedisKey.USER_CLIENT.format(userId, clientType.getName());
        Object token = redis.opsForValue().get(ucKey);
        if (token != null) {
            this.deleteAccessDigest(token.toString());
        }
    }

    // ==================== 在线用户 ====================

    @Override
    public List<UserOnlineVO> listOnlineUsers() {
        Set<Object> userIds = redis.opsForSet().members(AuthRedisKey.ONLINE_USERS.getPattern());
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        List<UserOnlineVO> result = new ArrayList<>();
        for (Object uidObj : userIds) {
            String userId = uidObj.toString();
            String userTokensKey = AuthRedisKey.USER_TOKENS.format(userId);
            Set<Object> tokens = redis.opsForSet().members(userTokensKey);
            if (tokens == null || tokens.isEmpty()) {
                redis.opsForSet().remove(AuthRedisKey.ONLINE_USERS.getPattern(), userId);
                continue;
            }
            for (Object tokenObj : tokens) {
                String token = tokenObj.toString();
                String sessionKey = AuthRedisKey.SESSION.format(token);
                Map<Object, Object> session = redis.opsForHash().entries(sessionKey);
                if (session.isEmpty()) {
                    redis.opsForSet().remove(userTokensKey, token);
                    continue;
                }

                result.add(userOnlineConverter.toVO(Objects.toString(session.get("userId"), null),
                        Objects.toString(session.get("username"), null),
                        Objects.toString(session.get("clientType"), null), Objects.toString(session.get("ip"), null),
                        Long.parseLong(Objects.toString(session.get("loginTime"), "0"))));
            }
        }
        return result;
    }

    // ==================== 当前用户 ====================

    @Override
    public @Nullable SecurityUser getCurrentUser() {
        var user = this.getUserFromSecurityContext();
        if (user != null) {
            return user;
        }
        String token = this.getTokenFromHttpRequest();
        if (token == null) {
            return null;
        }
        return this.getCurrentUser(token);
    }

    @Override
    public @Nullable SecurityUser getCurrentUser(String token) {
        String tokenDigest = TokenDigestService.digest(token);
        String sessionKey = AuthRedisKey.SESSION.format(tokenDigest);
        Object userIdObj = redis.opsForHash().get(sessionKey, "userId");
        if (userIdObj == null || securityUserLoader == null) {
            if (userIdObj != null && securityUserLoader == null) {
                throw new IllegalStateException("未配置安全主体加载器，拒绝使用 Redis 快照认证");
            }
            return null;
        }
        try {
            SecurityUser currentUser = securityUserLoader.load(UUID.fromString(userIdObj.toString()));
            if (currentUser == null) {
                return null;
            }
            return currentUser;
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Session 用户标识无效", exception);
        }
    }

    @Override
    public @Nullable String getCurrentToken() {
        var token = this.getTokenFromSecurityContext();
        if (StrUtils.isNotBlank(token)) {
            return token;
        }
        return this.getTokenFromHttpRequest();
    }

    @Override
    public @Nullable UUID getCurrentUserId() {
        var user = this.getCurrentUser();
        return user != null ? user.getId() : null;
    }

    @Override
    public String getCurrentUserZoneId() {
        SecurityUser user = this.getCurrentUser();
        return user != null && user.getTimezone() != null ? user.getTimezone() : "UTC";
    }

    @Override
    public String getCurrentUsername() {
        SecurityUser user = this.getCurrentUser();
        return user != null ? user.getUsername() : "未找到用户名";
    }

    // ==================== 登录锁定 ====================

    @Override
    public void recordLoginFail(String username) {
        String key = AuthRedisKey.LOGIN_FAIL.format(username);
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1 && properties.getLockoutSeconds() > 0) {
            redis.expire(key, Duration.ofSeconds(properties.getLockoutSeconds()));
        }
    }

    @Override
    public boolean isLockedOut(String username) {
        if (properties.getLockoutSeconds() <= 0) {
            return false;
        }
        String key = AuthRedisKey.LOGIN_FAIL.format(username);
        Object count = redis.opsForValue().get(key);
        return count != null && Long.parseLong(count.toString()) >= properties.getLockoutMaxAttempts();
    }

    @Override
    public void clearLoginFail(String username) {
        redis.delete(AuthRedisKey.LOGIN_FAIL.format(username));
    }

    // ==================== 内部辅助 ====================

    /**
     * 删除已经被 Rotation 消费的旧 Access Session；消费声明独立保留用于重放检测。
     */
    private void removeRotatedAccessSession(String accessDigest, String refreshDigest, String userId, String clientType,
                                            String familyId) {
        redis.delete(AuthRedisKey.SESSION.format(accessDigest));

        String accessRefreshKey = AuthRedisKey.REFRESH_TOKEN.format(accessDigest);
        Object mappedRefreshDigest = redis.opsForValue().get(accessRefreshKey);
        if (refreshDigest.equals(Objects.toString(mappedRefreshDigest, null))) {
            redis.delete(accessRefreshKey);
        }

        String userClientKey = AuthRedisKey.USER_CLIENT.format(userId, clientType);
        Object mappedAccessDigest = redis.opsForValue().get(userClientKey);
        if (accessDigest.equals(Objects.toString(mappedAccessDigest, null))) {
            redis.delete(userClientKey);
        }
        redis.opsForSet().remove(AuthRedisKey.USER_TOKENS.format(userId), accessDigest);
        redis.opsForSet().remove(AuthRedisKey.SESSION_FAMILY.format(familyId), accessDigest);
    }

    /**
     * Refresh Token 重放或 Rotation 写入失败时，撤销整个 Token Family。
     */
    private void revokeFamilyForRefreshReplay(String familyId) {
        Set<Object> familyTokens = redis.opsForSet().members(AuthRedisKey.SESSION_FAMILY.format(familyId));
        if (familyTokens != null) {
            for (Object familyToken : familyTokens) {
                deleteAccessDigest(familyToken.toString());
            }
        }
        redis.delete(AuthRedisKey.SESSION_FAMILY.format(familyId));
    }

    /**
     * 刷新 session / user-client / user-tokens 三个 key 的 TTL
     */
    private void refreshTTL(String tokenDigest, String userId, String clientType) {
        SessionPolicy policy = sessionPolicy();
        Duration accessTtl = Duration.ofSeconds(policy.accessTtlSeconds());
        Duration refreshTtl = Duration.ofSeconds(policy.refreshTtlSeconds());
        redis.expire(AuthRedisKey.SESSION.format(tokenDigest), accessTtl);
        redis.expire(AuthRedisKey.USER_CLIENT.format(userId, clientType), accessTtl);
        redis.expire(AuthRedisKey.USER_TOKENS.format(userId), refreshTtl);
    }

    private SessionPolicy sessionPolicy() {
        return new SessionPolicy(properties.getSessionConcurrencyMode(), properties.getMaxSessions(),
                properties.getAccessTokenExpire(), properties.getRefreshTokenExpire(), null, null);
    }

    private Set<Object> activeTokenDigests(String userId) {
        Set<Object> tokens = redis.opsForSet().members(AuthRedisKey.USER_TOKENS.format(userId));
        if (tokens == null || tokens.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Set<Object> active = new LinkedHashSet<>();
        for (Object token : tokens) {
            String digest = token.toString();
            if (Boolean.TRUE.equals(redis.hasKey(AuthRedisKey.SESSION.format(digest)))) {
                active.add(digest);
            } else {
                redis.opsForSet().remove(AuthRedisKey.USER_TOKENS.format(userId), digest);
            }
        }
        return active;
    }

    private boolean isMfaVerified(SecurityUser user) {
        return user.getExtraData() != null && Boolean.TRUE.equals(user.getExtraData().get("mfaVerified"));
    }

    private boolean isRoot(SecurityUser user) {
        return user.getAuthorities().stream().anyMatch(authority -> RootAuthorizationPolicy.ROOT_ROLE
                .equals(authority.getAuthority()));
    }

    /**
     * 构造 TokenVO
     */
    private TokenVO buildTokenVO(SecurityUser user, String token, String refreshToken) {
        var permissions = new ArrayList<String>();
        for (GrantedAuthority ga : user.getAuthorities()) {
            String a = ga.getAuthority();
            if (a == null)
                continue;
            if (!a.startsWith("ROLE_"))
                permissions.add(a);
        }
        return TokenVO.builder()
                .id(user.getId())
                .username(user.getEmail())
                .accessToken(token)
                .refreshToken(refreshToken)
                .permissions(permissions)
                .build();
    }

    /**
     * 从 HTTP 请求自动解析 ClientType
     */
    private ClientType resolveClientType() {
        HttpServletRequest request = this.getHttpServletRequest();
        if (request == null) {
            return ClientType.WEB;
        }

        // 优先使用自定义请求头（兼容显式指定）
        var headerType = request.getHeader(HEADER_CLIENT_TYPE);
        if (headerType != null && !headerType.isBlank()) {
            return ClientType.fromName(headerType);
        }

        String ua = request.getHeader("User-Agent");
        if (ua == null || ua.isBlank()) {
            return ClientType.WEB;
        }
        String lower = ua.toLowerCase();

        // 小程序识别：微信/支付宝/抖音等小程序环境
        if (lower.contains("miniprogram")
                || lower.contains("miniprogramenv")
                || lower.contains("wechat")
                || lower.contains("alipay")
                || lower.contains("bytedance")
                || lower.contains("toutiao")) {
            return ClientType.MINI;
        }

        // APP识别：UniApp（含 uni-app / html5plus 标识）及原生App（含移动端SDK标识）
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
        HttpServletRequest request = this.getHttpServletRequest();
        if (request == null) {
            return "unknown";
        }
        String deviceId = request.getHeader(HEADER_DEVICE_ID);
        return deviceId == null || deviceId.isBlank() ? "unknown" : deviceId;
    }

    private @Nullable String getTokenFromSecurityContext() {
        var auth = this.getSecurityContextAuthentication();
        if (auth == null)
            return null;
        var cred = auth.getCredentials();
        return cred instanceof String s ? s : null;
    }

    private @Nullable String getTokenFromHttpRequest() {
        HttpServletRequest request = this.getHttpServletRequest();
        if (request == null)
            return null;
        var bearer = request.getHeader("authorization");
        if (bearer == null || !bearer.startsWith("Bearer "))
            return null;
        return bearer.substring(7);
    }

    private @Nullable SecurityUser getUserFromSecurityContext() {
        var auth = this.getSecurityContextAuthentication();
        if (auth == null)
            return null;
        var p = auth.getPrincipal();
        return p instanceof SecurityUser su ? su : null;
    }

    private @Nullable Authentication getSecurityContextAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private @Nullable HttpServletRequest getHttpServletRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        return attrs instanceof ServletRequestAttributes sra ? sra.getRequest() : null;
    }
}
