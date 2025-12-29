package io.github.yangxj96.spectra.core.configure.security.holder;


import io.github.yangxj96.spectra.common.utils.IpUtils;
import io.github.yangxj96.spectra.common.utils.StrUtils;
import io.github.yangxj96.spectra.core.configure.redis.RedisCacheKey;
import io.github.yangxj96.spectra.core.configure.security.javabean.SecurityUser;
import io.github.yangxj96.spectra.core.configure.security.javabean.LoginType;
import io.github.yangxj96.spectra.core.configure.security.properties.SecurityProperties;
import io.github.yangxj96.spectra.core.javabean.auth.vo.TokenVO;
import io.github.yangxj96.spectra.core.service.common.IpLocationService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 方式存储 Token
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/11 10:06
 */
@Slf4j
@NullMarked
@Component("sec")
public class RedisSecHolder implements SecHolder {

    @Resource(name = "securityObjectMapper")
    private ObjectMapper om;

    @Resource(name = "securityRedisTemplate")
    private RedisTemplate<String, Object> redis;

    @Resource
    private IpLocationService ipLocationService;

    @Resource
    private SecurityProperties properties;

    public RedisSecHolder() {
        SecUtil.setHolder(this);
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
        // token 生成
        String token = UUID.randomUUID().toString().toUpperCase();
        // 扩展内容
        if (user.getExtraData() == null) {
            user.setExtraData(new HashMap<>());
        }
        var extra = user.getExtraData();
        extra.put("ip", IpUtils.getClientIP(this.getHttpServletRequest()));
        extra.put("address", ipLocationService.getCityEn(extra.get("ip").toString()));

        // 安全获取权限列表，防止 user.getAuthorities() 本身为 null
        Collection<? extends GrantedAuthority> authoritiesList = user.getAuthorities();

        List<String> roles = new ArrayList<>();
        List<String> authorities = new ArrayList<>();

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

        // 构造响应对象
        var tokenInfo = TokenVO.builder()
                .id(user.getId())
                .loginType(loginType)
                .username(user.getEmail())
                .accessToken(token)
                .authorities(authorities)
                .roles(roles)
                .build();

        // 确保所有流程都结束且没发生错误,才进行 security 上下文设置
        // 把当前用户信息放到上下文中,主要是为了日志记录的时候登录接口无法获取到当前用户信息
        var auth = new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // KEY 格式化
        // 默认用户名密码
        var redisLoginType = RedisCacheKey.AUTH_LOGIN_TYPE.formatted(loginType.getName(), user.getId());
        var redisToken = RedisCacheKey.AUTH_TOKEN.formatted(tokenInfo.getAccessToken());
        var redisSecurity = RedisCacheKey.AUTH_SECURITY.formatted(tokenInfo.getAccessToken());
        var redisTokenUser = RedisCacheKey.AUTH_TOKEN_USER.formatted(tokenInfo.getAccessToken());
        var redisUserLoginTypes = RedisCacheKey.AUTH_USER_LOGIN_TYPES.formatted(user.getId());
        //存储
        var ops = redis.opsForValue();
        ops.set(redisLoginType, tokenInfo.getAccessToken(), properties.getTokenExpire(), TimeUnit.SECONDS);
        ops.set(redisToken, tokenInfo, properties.getTokenExpire(), TimeUnit.SECONDS);
        ops.set(redisSecurity, user, properties.getTokenExpire(), TimeUnit.SECONDS);
        ops.set(redisTokenUser, user.getId(), properties.getTokenExpire(), TimeUnit.SECONDS);
        redis.opsForSet().add(redisUserLoginTypes, loginType.getName());

        return tokenInfo;
    }

    @Override
    public void deleteToken(String token) {
        // 先获取 token 的详细信息（包含 userId 和 loginType）
        var tokenKey = RedisCacheKey.AUTH_TOKEN.formatted(token);
        var tokenInfoObj = redis.opsForValue().get(tokenKey);

        if (!(tokenInfoObj instanceof TokenVO tokenInfo)) {
            // Token 不存在或已过期，可视为已登出
            return;
        }

        var userId = tokenInfo.getId();
        var loginType = tokenInfo.getLoginType();

        // 2. 删除 token 相关的所有 key
        redis.delete(RedisCacheKey.AUTH_TOKEN.formatted(token));
        redis.delete(RedisCacheKey.AUTH_SECURITY.formatted(token));
        redis.delete(RedisCacheKey.AUTH_TOKEN_USER.formatted(token));

        // 检查并清除 "当前活跃 Token"（防止顶号后旧 token 仍被误认为有效）
        var currentLoginTypeKey = RedisCacheKey.AUTH_LOGIN_TYPE.formatted(loginType, userId);
        var currentToken = redis.opsForValue().get(currentLoginTypeKey);
        if (token.equals(currentToken)) {
            // 只有当前 token 是活跃 token 时才删除，避免误删新登录的 token
            redis.delete(currentLoginTypeKey);
        }

        // 从用户登录方式集合中移除该 loginType
        redis.opsForSet().remove(RedisCacheKey.AUTH_USER_LOGIN_TYPES.formatted(userId), loginType);
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
        // 否则尝试从 redis 获取
        var ops = redis.opsForValue();
        var securityKey = RedisCacheKey.AUTH_SECURITY.formatted(token);
        Object o = ops.get(securityKey);
        if (o == null) {
            return null;
        }
        return om.convertValue(o, SecurityUser.class);
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
    public @Nullable Long getCurrentUserId() {
        SecurityUser user = this.getCurrentUser();
        if (user == null) {
            return null;
        }
        return user.getId();
    }


    //--------------------------  辅助方法  --------------------------------//


    /**
     * 从 SpringSecurity 上下文获取用户信息
     *
     * @return 上下文对象,有可能为空
     */
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

    /**
     * 从 HTTP 请求中获取 TOKEN
     *
     * @return Token,可能为null
     */
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

    /**
     * 从 SpringSecurity 上下文获取用户信息
     *
     * @return 上下文对象,有可能为空
     */
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

    /**
     * 获取 SpringSecurity 的 Authentication 对象
     *
     * @return Authentication对象,有可能为null
     */
    private @Nullable Authentication getSecurityContextAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取 request
     *
     * @return {@link HttpServletRequest}
     */
    private @Nullable HttpServletRequest getHttpServletRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getRequest();
        }
        return null;
    }
}
