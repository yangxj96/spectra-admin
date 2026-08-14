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
import com.devops00.spectra.security.base.util.RefreshTokenRotationStore;
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
        return properties.getRootRoleCode();
    }

    // ==================== Token 创建 & 续期 ====================

    @Override
    public TokenVO createToken(SecurityUser user) {
        return this.createToken(user, resolveClientType());
    }

    @Override
    public TokenVO createToken(SecurityUser user, ClientType clientType) {
        String userId = user.getId().toString();
        String ucKey = AuthRedisKey.USER_CLIENT.format(userId, clientType.getName());

        Duration accessTtl = Duration.ofSeconds(properties.getAccessTokenExpire());
        Duration refreshTtl = Duration.ofSeconds(properties.getRefreshTokenExpire());

        // 同端复用：检查是否已有有效 token
        Object oldToken = redis.opsForValue().get(ucKey);
        if (oldToken != null) {
            String oldSessionKey = AuthRedisKey.SESSION.format(oldToken);
            if (Boolean.TRUE.equals(redis.hasKey(oldSessionKey))) {
                Object oldRefreshToken = redis.opsForValue().get(AuthRedisKey.REFRESH_TOKEN.format(oldToken));
                if (oldRefreshToken != null) {
                    this.refreshTTL(oldToken.toString(), userId, clientType.getName());
                    return buildTokenVO(user, oldToken.toString(), oldRefreshToken.toString());
                }
            }
            // 旧 session 已过期，清理残留
            redis.delete(ucKey);
        }

        // 生成新 token
        String token = UUID.randomUUID().toString().toUpperCase();
        String refreshToken = UUID.randomUUID().toString().toUpperCase();
        String ip = IpUtils.getClientIP(this.getHttpServletRequest());

        // 构造 session hash（事实源）
        Map<String, Object> session = new LinkedHashMap<>();
        session.put("userId", userId);
        session.put("username", user.getUsername());
        session.put("email", user.getEmail());
        session.put("clientType", clientType.getName());
        session.put("ip", ip);
        session.put("loginTime", System.currentTimeMillis());
        session.put("lastActiveTime", System.currentTimeMillis());
        session.put("user", user);

        String sessionKey = AuthRedisKey.SESSION.format(token);
        String userTokensKey = AuthRedisKey.USER_TOKENS.format(userId);
        String rtKey = AuthRedisKey.REFRESH_TOKEN.format(token);

        redis.opsForHash().putAll(sessionKey, session);
        redis.expire(sessionKey, accessTtl);
        redis.opsForValue().set(ucKey, token, accessTtl);
        redis.opsForSet().add(userTokensKey, token);
        redis.expire(userTokensKey, refreshTtl);
        // refreshToken 存储：auth:rt:{accessToken} → refreshToken (String)
        redis.opsForValue().set(rtKey, refreshToken, refreshTtl);
        // auth:rt:{refreshToken} → Hash{accessToken, userId, user}（会话过期时用于重建）
        Map<String, Object> rtData = new LinkedHashMap<>();
        rtData.put("accessToken", token);
        rtData.put("userId", userId);
        rtData.put("clientType", clientType.getName());
        rtData.put("user", user);
        String rtRefreshKey = AuthRedisKey.REFRESH_TOKEN.format(refreshToken);
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
        String sessionKey = AuthRedisKey.SESSION.format(token);
        Object userIdObj = redis.opsForHash().get(sessionKey, "userId");
        if (userIdObj == null) {
            return;
        }
        String userId = userIdObj.toString();
        Object clientTypeObj = redis.opsForHash().get(sessionKey, "clientType");
        String clientType = clientTypeObj != null ? clientTypeObj.toString() : ClientType.WEB.getName();

        this.refreshTTL(token, userId, clientType);
        redis.opsForHash().put(sessionKey, "lastActiveTime", System.currentTimeMillis());
    }

    @Override
    public TokenVO refreshByRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("刷新token不能为空");
        }
        String rtKey = AuthRedisKey.REFRESH_TOKEN.format(refreshToken);
        Map<Object, Object> rtData = redis.opsForHash().entries(rtKey);
        if (rtData.isEmpty()) {
            throw new IllegalArgumentException("刷新token无效或已过期");
        }

        String accessToken = Objects.toString(rtData.get("accessToken"), null);
        String userId = Objects.toString(rtData.get("userId"), null);
        Object userObj = rtData.get("user");
        if (accessToken == null || userId == null || !(userObj instanceof SecurityUser user)) {
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

        Duration refreshTtl = Duration.ofSeconds(properties.getRefreshTokenExpire());
        String replayFenceKey = AuthRedisKey.REFRESH_REPLAY_FENCE.format(userId);
        if (Boolean.TRUE.equals(redis.hasKey(replayFenceKey))) {
            throw new IllegalArgumentException("刷新token所属会话已因重放风险撤销");
        }

        var claimResult = RefreshTokenRotationStore.claim(redis, rtKey,
                AuthRedisKey.REFRESH_CLAIM.format(refreshToken), properties.getRefreshTokenExpire());
        if (claimResult != RefreshTokenRotationStore.ClaimResult.CLAIMED) {
            if (claimResult == RefreshTokenRotationStore.ClaimResult.REPLAY) {
                redis.opsForValue().set(replayFenceKey, "REVOKED", refreshTtl);
                revokeUserForRefreshReplay(userId);
                throw new IllegalArgumentException("刷新token重放，所属会话已撤销");
            }
            throw new IllegalArgumentException("刷新token无效或已过期");
        }

        String sessionKey = AuthRedisKey.SESSION.format(accessToken);
        Map<Object, Object> session = redis.opsForHash().entries(sessionKey);
        String clientType = Objects.toString(session.get("clientType"),
                Objects.toString(rtData.get("clientType"), ClientType.WEB.getName()));

        try {
            removeRotatedAccessSession(accessToken, refreshToken, userId, clientType);
            if (Boolean.TRUE.equals(redis.hasKey(replayFenceKey))) {
                throw new IllegalArgumentException("刷新token所属会话已因重放风险撤销");
            }
            return createToken(currentUser, ClientType.fromName(clientType));
        } catch (RuntimeException exception) {
            revokeUserForRefreshReplay(userId);
            throw exception;
        }
    }

    // ==================== Token 删除 & 踢出 ====================

    @Override
    public void deleteToken(String token) {
        String sessionKey = AuthRedisKey.SESSION.format(token);
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
        Object refreshTokenObj = redis.opsForValue().get(AuthRedisKey.REFRESH_TOKEN.format(token));
        if (refreshTokenObj != null) {
            // auth:rt:{refreshToken} → Hash，需要删除
            redis.delete(AuthRedisKey.REFRESH_TOKEN.format(refreshTokenObj.toString()));
        }
        redis.delete(AuthRedisKey.REFRESH_TOKEN.format(token));

        redis.delete(sessionKey);
        redis.delete(ucKey);
        redis.opsForSet().remove(userTokensKey, token);

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
        String rtRefreshKey = AuthRedisKey.REFRESH_TOKEN.format(refreshToken);
        Map<Object, Object> rtData = redis.opsForHash().entries(rtRefreshKey);
        if (rtData.isEmpty()) {
            return;
        }

        String accessToken = Objects.toString(rtData.get("accessToken"), null);
        String userId = Objects.toString(rtData.get("userId"), null);

        if (accessToken != null) {
            deleteToken(accessToken);
        }
        redis.delete(rtRefreshKey);
        if (userId != null && accessToken == null) {
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
            this.deleteToken(t.toString());
        }
    }

    @Override
    public void deleteByUserIdAndClient(String userId, ClientType clientType) {
        String ucKey = AuthRedisKey.USER_CLIENT.format(userId, clientType.getName());
        Object token = redis.opsForValue().get(ucKey);
        if (token != null) {
            this.deleteToken(token.toString());
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

                Object userObj = session.get("user");
                SecurityUser su = userObj != null ? om.convertValue(userObj, SecurityUser.class) : null;

                result.add(userOnlineConverter.toVO(Objects.toString(session.get("userId"), null),
                        su != null ? su.getUsername() : Objects.toString(session.get("username"), null),
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
        String sessionKey = AuthRedisKey.SESSION.format(token);
        Object userObj = redis.opsForHash().get(sessionKey, "user");
        if (userObj == null) {
            return null;
        }
        return om.convertValue(userObj, SecurityUser.class);
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
    private void removeRotatedAccessSession(String accessToken, String refreshToken, String userId, String clientType) {
        redis.delete(AuthRedisKey.SESSION.format(accessToken));

        String accessRefreshKey = AuthRedisKey.REFRESH_TOKEN.format(accessToken);
        Object mappedRefreshToken = redis.opsForValue().get(accessRefreshKey);
        if (refreshToken.equals(Objects.toString(mappedRefreshToken, null))) {
            redis.delete(accessRefreshKey);
        }

        String userClientKey = AuthRedisKey.USER_CLIENT.format(userId, clientType);
        Object mappedAccessToken = redis.opsForValue().get(userClientKey);
        if (accessToken.equals(Objects.toString(mappedAccessToken, null))) {
            redis.delete(userClientKey);
        }
        redis.opsForSet().remove(AuthRedisKey.USER_TOKENS.format(userId), accessToken);
    }

    /**
     * Refresh Token 重放或 Rotation 写入失败时，撤销该用户的全部旧会话。
     */
    private void revokeUserForRefreshReplay(String userId) {
        try {
            deleteByUserId(UUID.fromString(userId));
        } catch (IllegalArgumentException exception) {
            log.warn("Refresh Token 重放但 userId 无法解析，无法撤销用户会话: {}", userId);
        }
    }

    /**
     * 刷新 session / user-client / user-tokens 三个 key 的 TTL
     */
    private void refreshTTL(String token, String userId, String clientType) {
        Duration accessTtl = Duration.ofSeconds(properties.getAccessTokenExpire());
        Duration refreshTtl = Duration.ofSeconds(properties.getRefreshTokenExpire());
        redis.expire(AuthRedisKey.SESSION.format(token), accessTtl);
        redis.expire(AuthRedisKey.USER_CLIENT.format(userId, clientType), accessTtl);
        redis.expire(AuthRedisKey.USER_TOKENS.format(userId), refreshTtl);
    }

    /**
     * 构造 TokenVO
     */
    private TokenVO buildTokenVO(SecurityUser user, String token, String refreshToken) {
        var roles = new ArrayList<String>();
        var authorities = new ArrayList<String>();
        for (GrantedAuthority ga : user.getAuthorities()) {
            String a = ga.getAuthority();
            if (a == null)
                continue;
            if (a.startsWith("ROLE"))
                roles.add(a);
            else
                authorities.add(a);
        }
        return TokenVO.builder()
                .id(user.getId())
                .username(user.getEmail())
                .accessToken(token)
                .refreshToken(refreshToken)
                .authorities(authorities)
                .roles(roles)
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
