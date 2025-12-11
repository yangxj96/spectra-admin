package io.github.yangxj96.spectra.core.configure.security.holder;


import io.github.yangxj96.spectra.common.utils.IpUtils;
import io.github.yangxj96.spectra.core.configure.redis.RedisCacheKey;
import io.github.yangxj96.spectra.core.configure.security.properties.SecurityProperties;
import io.github.yangxj96.spectra.core.javabean.auth.SecurityUser;
import io.github.yangxj96.spectra.core.javabean.auth.vo.TokenVO;
import io.github.yangxj96.spectra.core.template.IpLocationTemplate;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
        SecurityUser user = getUserByToken(token);
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
    public @Nullable SecurityUser getUserByToken(String token) {
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
    public @Nullable SecurityUser getCurrentUser() {
        String bearerToken = getCurrentToken();
        if (bearerToken == null) {
            return null;
        }
        return this.getUserByToken(bearerToken.substring(7));
    }

    @Override
    public @Nullable String getCurrentToken() {
        String bearerToken = request.getHeader("authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return null;
        }
        return bearerToken.substring(7);
    }

    @Override
    public @Nullable Long getCurrentUserId() {
        SecurityUser user = this.getCurrentUser();
        if (user == null) {
            return null;
        }
        return user.getId();
    }
}
