package io.github.yangxj96.spectra.core.configure.security.holder;


import io.github.yangxj96.spectra.common.utils.IpUtils;
import io.github.yangxj96.spectra.common.utils.StrUtils;
import io.github.yangxj96.spectra.core.configure.redis.RedisCacheKey;
import io.github.yangxj96.spectra.core.configure.security.properties.SecurityProperties;
import io.github.yangxj96.spectra.core.configure.security.SecurityUser;
import io.github.yangxj96.spectra.core.javabean.auth.vo.TokenVO;
import io.github.yangxj96.spectra.core.template.IpLocationTemplate;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
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
@NullMarked
@Component("sec")
public class RedisSecHolder implements SecHolder {

    @Resource
    private ObjectMapper om;

    @Resource
    private RedisTemplate<String, Object> redis;

    @Resource
    private IpLocationTemplate ipLocationTemplate;

    @Resource
    private SecurityProperties properties;

    @Resource
    private HttpServletRequest request;

    public RedisSecHolder() {
        SecUtil.setHolder(this);
    }

    @Override
    public String administrators() {
        return properties.getAdministrators();
    }

    @Override
    public TokenVO createToken(SecurityUser su) {
        // token 生成
        String token = UUID.randomUUID().toString().toUpperCase();
        // 扩展内容
        if (su.getExtend() == null) {
            su.setExtend(new HashMap<>());
        }
        var terminalExtraData = su.getExtend();
        var ip = IpUtils.getClientIP(request);
        terminalExtraData.put("ip", ip);
        terminalExtraData.put("address", ipLocationTemplate.getCityEn(ip));

        // 存储 token
        String mainKey = String.format(RedisCacheKey.AUTH_TOKEN_KEY, su.getId(), token);
        String refKey = String.format(RedisCacheKey.TOKEN_TO_USER_KEY, token);

        var ops = redis.opsForValue();
        ops.set(mainKey, su, properties.getTokenExpire(), TimeUnit.SECONDS);
        ops.set(refKey, su.getId(), properties.getTokenExpire(), TimeUnit.SECONDS);

        // 安全获取权限列表，防止 user.getAuthorities() 本身为 null
        Collection<? extends GrantedAuthority> authoritiesList = su.getAuthorities();

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
        TokenVO vo = TokenVO.builder()
                .id(su.getId())
                .username(su.getEmail())
                .accessToken(token)
                .authorities(authorities)
                .roles(roles)
                .build();

        // 确保所有流程都结束且没发生错误,才进行 security 上下文设置
        // 把当前用户信息放到上下文中,主要是为了日志记录的时候登录接口无法获取到当前用户信息
        var auth = new UsernamePasswordAuthenticationToken(su, token, su.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        return vo;
    }

    @Override
    public void deleteToken(String token) {
        SecurityUser user = getCurrentUser(token);
        if (user == null) {
            return;
        }
        // 构造两个 key
        String mainKey = String.format(RedisCacheKey.AUTH_TOKEN_KEY, user.getId(), token);
        String refKey = String.format(RedisCacheKey.TOKEN_TO_USER_KEY, token);
        // 删除
        redis.delete(mainKey);
        redis.delete(refKey);
    }

    @Override
    public @Nullable SecurityUser getCurrentUser() {
        // 优先从上下文获取用户信息
        var su = this.getUserFromSecurityContext();
        if (su != null) {
            return su;
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
        String refKey = String.format(RedisCacheKey.TOKEN_TO_USER_KEY, token);
        Object o1 = ops.get(refKey);
        if (o1 == null) {
            return null;
        }
        String userId = o1.toString();
        if (StringUtils.isEmpty(userId)) {
            return null;
        }
        String mainKey = String.format(RedisCacheKey.AUTH_TOKEN_KEY, userId, token);
        Object object = ops.get(mainKey);
        return om.convertValue(object, SecurityUser.class);
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
        if (principal != null && principal instanceof SecurityUser su) {
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
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication;
    }
}
