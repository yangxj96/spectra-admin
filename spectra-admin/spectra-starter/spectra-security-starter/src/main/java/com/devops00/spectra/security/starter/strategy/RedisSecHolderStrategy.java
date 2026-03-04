package com.devops00.spectra.security.starter.strategy;


import com.devops00.spectra.common.utils.IpUtils;
import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.security.base.constant.AuthRedisKey;
import com.devops00.spectra.security.base.constant.LoginType;
import com.devops00.spectra.security.base.holder.SecHolderStrategy;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import com.devops00.spectra.security.base.javabean.vo.TokenVO;
import com.devops00.spectra.security.base.javabean.vo.UserOnlineVO;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.starter.renew.DefaultTokenTtlStrategy;
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

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/// Redis 方式存储 Token
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/11 10:06
@Slf4j
@NullMarked
public class RedisSecHolderStrategy implements SecHolderStrategy {

    private final ObjectMapper om;

    private final RedisTemplate<String, Object> redis;

    private final SecurityProperties properties;

    private final DefaultTokenTtlStrategy tokenTtlStrategy;

    public RedisSecHolderStrategy(
            @Qualifier("securityObjectMapper") ObjectMapper om,
            @Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redis,
            SecurityProperties properties,
            DefaultTokenTtlStrategy tokenTtlStrategy
    ) {
        this.om = om;
        this.redis = redis;
        this.properties = properties;
        this.tokenTtlStrategy = tokenTtlStrategy;
    }

    @Override
    public String administrators() {
        return properties.getAdministrators();
    }

    @Override
    public TokenVO createToken(SecurityUser user) {
        return this.createToken(user, LoginType.PASSWORD);
    }

    @Override
    public TokenVO createToken(SecurityUser user, LoginType loginType) {
        // =======================
        // 1. 生成 token
        // =======================
        String token = UUID.randomUUID().toString().toUpperCase();

        // =======================
        // 2. 扩展信息
        // =======================
        if (user.getExtraData() == null) {
            user.setExtraData(new HashMap<>());
        }
        var extra = user.getExtraData();
        extra.put("ip", IpUtils.getClientIP(this.getHttpServletRequest()));

        // =======================
        // 3. 权限拆分
        // =======================
        // 安全获取权限列表，防止 user.getAuthorities() 本身为 null
        var authoritiesList = user.getAuthorities();

        var roles = new ArrayList<String>();
        var authorities = new ArrayList<String>();

        for (GrantedAuthority ga : authoritiesList) {
            // 显式检查 getAuthority() 是否为 null
            String authority = ga.getAuthority();
            if (authority == null) {
                continue;
            }

            if (authority.startsWith("ROLE")) {
                roles.add(authority);
            } else {
                authorities.add(authority);
            }
        }

        // =======================
        // 4. 构造 TokenVO（返回用）
        // =======================
        var tokenInfo = TokenVO.builder()
                .id(user.getId())
                .loginType(loginType)
                .username(user.getEmail())
                .accessToken(token)
                .authorities(authorities)
                .roles(roles)
                .build();

        // =======================
        // 5. 写入 SecurityContext（保留）
        // =======================

        // 确保所有流程都结束且没发生错误,才进行 security 上下文设置
        // 把当前用户信息放到上下文中,主要是为了日志记录的时候登录接口无法获取到当前用户信息
        var auth = new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // =======================
        // 6. Redis Key 准备
        // =======================
        var sessionKey = AuthRedisKey.SESSION_TOKEN_DETAIL.format(token);
        var tokenUserKey = AuthRedisKey.TOKEN_USER.format(token);
        var userTokensKey = AuthRedisKey.USER_TOKENS.format(user.getId());
        var userClientTokensKey = AuthRedisKey.USER_CLIENT_TOKENS.format(
                user.getId(), loginType.getName()
        );
        var userDetailsKey = AuthRedisKey.USER_DETAIL.format(user.getId());
        var onlineUsersKey = AuthRedisKey.ONLINE_USER_IDS.getPattern();

        long ttl = tokenTtlStrategy.resolveTtlSeconds(loginType, "BROWSER");

        // =======================
        // 7. 构造 Session 数据（事实源）
        // =======================
        var sessionData = new HashMap<String, Object>();
        sessionData.put("userId", user.getId());
        sessionData.put("username", user.getEmail());
        sessionData.put("loginType", loginType.getName());
        // 如果区分 clientType，建议单独字段
        sessionData.put("clientType", "browser");
        sessionData.put("ip", extra.get("ip"));
        sessionData.put("address", extra.get("address"));
        sessionData.put("loginTime", System.currentTimeMillis());
        sessionData.put("lastActiveTime", System.currentTimeMillis());
        // 动态续期使用
        sessionData.put("ttlSeconds", ttl);
        sessionData.put("lastActiveAt", System.currentTimeMillis());

        // =======================
        // 8. 写入 Redis（核心）
        // =======================
        redis.opsForHash().putAll(sessionKey, sessionData);
        redis.expire(sessionKey, ttl, TimeUnit.SECONDS);

        redis.opsForValue().set(tokenUserKey, user.getId());
        redis.opsForSet().add(userTokensKey, token);
        redis.opsForSet().add(userClientTokensKey, token);

        // 用户详情
        // TODO 暂时不设置时长,但是后期要改成短时间,超时需要从db中读取
        redis.opsForValue().set(userDetailsKey, user);

        redis.opsForSet().add(onlineUsersKey, user.getId());
        redis.opsForSet().add(AuthRedisKey.SESSION_ONLINE.getPattern(), token);

        return tokenInfo;
    }

    @Override
    public void deleteToken(String token) {
        // =======================
        // 1. session 事实源 key
        // =======================
        String sessionKey = AuthRedisKey.SESSION_TOKEN_DETAIL.format(token);

        // session 不存在，说明已过期或已删除
        if (!Boolean.TRUE.equals(redis.hasKey(sessionKey))) {
            return;
        }

        // =======================
        // 2. 从 session 中读取必要字段
        // =======================
        Object userIdObj = redis.opsForHash().get(sessionKey, "userId");
        Object clientTypeObj = redis.opsForHash().get(sessionKey, "clientType");
        Object loginTypeObj = redis.opsForHash().get(sessionKey, "loginType");

        if (userIdObj == null || clientTypeObj == null) {
            // session 数据不完整，直接清理 session
            redis.delete(sessionKey);
            return;
        }

        String userId = userIdObj.toString();
        String clientType = clientTypeObj.toString();
        String loginType = loginTypeObj.toString();

        // =======================
        // 3. 构造关联 key
        // =======================
        String tokenUserKey = AuthRedisKey.TOKEN_USER.format(token);
        String userTokensKey = AuthRedisKey.USER_TOKENS.format(userId);
        String userClientTokensKey = AuthRedisKey.USER_CLIENT_TOKENS.format(userId, clientType);
        String onlineUsersKey = AuthRedisKey.ONLINE_USER_IDS.getPattern();
        String userDetailsKey = AuthRedisKey.USER_DETAIL.format(userId);

        // =======================
        // 4. 删除 token 相关数据
        // =======================
        redis.delete(sessionKey);
        redis.delete(tokenUserKey);

        redis.opsForSet().remove(userTokensKey, token);
        redis.opsForSet().remove(userClientTokensKey, token);
        redis.opsForSet().remove(AuthRedisKey.SESSION_ONLINE.getPattern(), token);
        redis.opsForSet().remove(AuthRedisKey.USER_CLIENT_TOKENS.format(userId, loginType), token);


        // =======================
        // 5. 如果用户已无任何 token，移出在线用户
        // =======================
        Long remain = redis.opsForSet().size(userTokensKey);
        if (remain == null || remain == 0) {
            redis.opsForSet().remove(onlineUsersKey, userId);
            // 同时移除用户详情
            redis.delete(userDetailsKey);
        }
    }

    @Override
    public void deleteByUserId(String userId) {
        String userTokensKey = AuthRedisKey.USER_TOKENS.format(userId);

        Set<Object> tokens = redis.opsForSet().members(userTokensKey);
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        for (Object tokenObj : tokens) {
            deleteToken(tokenObj.toString());
        }
    }

    @Override
    public void deleteByUserIdAndClient(String userId, LoginType clientType) {
        String userClientTokensKey =
                AuthRedisKey.USER_CLIENT_TOKENS.format(userId, clientType.getName());

        Set<Object> tokens = redis.opsForSet().members(userClientTokensKey);
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        for (Object tokenObj : tokens) {
            deleteToken(tokenObj.toString());
        }
    }

    @Override
    public List<UserOnlineVO> listOnlineUsers() {
        Set<Object> tokens = redis.opsForSet()
                .members(AuthRedisKey.SESSION_ONLINE.getPattern());

        if (tokens == null || tokens.isEmpty()) {
            return List.of();
        }

        List<UserOnlineVO> result = new ArrayList<>();

        for (Object tokenObj : tokens) {
            String token = tokenObj.toString();

            String sessionKey = AuthRedisKey.SESSION_TOKEN_DETAIL.format(token);

            Map<Object, Object> session = redis.opsForHash().entries(sessionKey);

            if (session == null || session.isEmpty()) {
                // session 已过期，顺手清理脏 token
                redis.opsForSet().remove(AuthRedisKey.SESSION_ONLINE.getPattern(), token);
                continue;
            }

            String userId = Objects.toString(session.get("userId"), null);
            if (userId == null) {
                continue;
            }

            // 从 USER_DETAIL 取用户信息
            Object userObj = redis.opsForValue()
                    .get(AuthRedisKey.USER_DETAIL.format(userId));

            if (userObj == null) {
                continue;
            }

            SecurityUser user = om.convertValue(userObj, SecurityUser.class);

            result.add(
                    UserOnlineVO.builder()
                            .token(token)
                            .userId(userId)
                            .username(user.getUsername())
                            .loginType(Objects.toString(session.get("loginType"), null))
                            .ip(Objects.toString(session.get("ip"), null))
                            .address(Objects.toString(session.get("address"), null))
                            .loginTime(Instant.ofEpochMilli(
                                    Long.parseLong(session.get("loginTime").toString())
                            ))
                            .build()
            );
        }

        return result;
    }

    @Override
    public @Nullable SecurityUser getCurrentUser() {
        // 优先从上下文获取用户信息
        var user = this.getUserFromSecurityContext();
        if (user != null) {
            return user;
        }

        // 上下文没有,则尝试从请求头中获取token,存在token则尝试从Redis中获取
        String token = this.getTokenFromHttpRequest();
        if (token == null) {
            return null;
        }
        return this.getCurrentUser(token);
    }

    @Override
    public @Nullable SecurityUser getCurrentUser(String token) {
        // 1. 校验 session 是否存在（事实源）
        String sessionKey = AuthRedisKey.SESSION_TOKEN_DETAIL.format(token);

        Object userIdObj = redis.opsForHash().get(sessionKey, "userId");
        if (userIdObj == null) {
            return null; // token 无效 / 过期
        }

        String userId = userIdObj.toString();

        // 2. 直接从 USER_DETAIL 取 SecurityUser
        String userKey = AuthRedisKey.USER_DETAIL.format(userId);

        Object cachedUser = redis.opsForValue().get(userKey);
        if (cachedUser == null) {
            return null;
        }

        return om.convertValue(cachedUser, SecurityUser.class);
    }

    @Override
    public @Nullable String getCurrentToken() {
        // 尝试从 SpringSecurity 上下文获取
        var token = this.getTokenFromSecurityContext();
        if (StrUtils.isNotBlank(token)) {
            return token;
        }
        // 不存在旧在尝试下从 http 请求获取
        return this.getTokenFromHttpRequest();
    }

    @Override
    public @Nullable String getCurrentUserId() {
        var user = this.getCurrentUser();
        if (user == null) {
            return null;
        }
        return user.getId();
    }


    //--------------------------  辅助方法  --------------------------------//

    /// 从 SpringSecurity 上下文获取用户信息
    ///
    /// @return 上下文对象,有可能为空
    private @Nullable String getTokenFromSecurityContext() {
        var authentication = this.getSecurityContextAuthentication();
        if (authentication == null) {
            return null;
        }
        var credentials = authentication.getCredentials();
        if (credentials instanceof String token) {
            return token;
        }
        return null;
    }

    /// 从 HTTP 请求中获取 TOKEN
    ///
    /// @return Token,可能为null
    private @Nullable String getTokenFromHttpRequest() {
        HttpServletRequest request = this.getHttpServletRequest();
        if (request == null) {
            return null;
        }
        var bearerToken = request.getHeader("authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return null;
        }
        return bearerToken.substring(7);
    }

    /// 从 SpringSecurity 上下文获取用户信息
    ///
    /// @return 上下文对象,有可能为空
    private @Nullable SecurityUser getUserFromSecurityContext() {
        var authentication = this.getSecurityContextAuthentication();
        if (authentication == null) {
            return null;
        }
        var principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser su) {
            return su;
        }
        return null;
    }

    /// 获取 SpringSecurity 的 Authentication 对象
    ///
    /// @return Authentication对象,有可能为null
    private @Nullable Authentication getSecurityContextAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /// 获取 request
    ///
    /// @return 请求体
    private @Nullable HttpServletRequest getHttpServletRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getRequest();
        }
        return null;
    }
}
