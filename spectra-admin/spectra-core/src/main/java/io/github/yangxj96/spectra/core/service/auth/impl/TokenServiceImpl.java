package io.github.yangxj96.spectra.core.service.auth.impl;


import io.github.yangxj96.spectra.common.utils.IpUtils;
import io.github.yangxj96.spectra.core.configure.redis.RedisCacheKey;
import io.github.yangxj96.spectra.core.configure.security.properties.SecurityProperties;
import io.github.yangxj96.spectra.core.javabean.auth.SecurityUser;
import io.github.yangxj96.spectra.core.javabean.auth.vo.TokenVO;
import io.github.yangxj96.spectra.core.service.auth.TokenService;
import io.github.yangxj96.spectra.core.template.IpLocationTemplate;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Token 服务
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/2 23:32
 */
@Service
@NullMarked
public class TokenServiceImpl implements TokenService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ObjectMapper om;

    @Resource
    private HttpServletRequest request;

    @Resource
    private IpLocationTemplate ipLocationTemplate;

    @Resource
    private SecurityProperties securityProperties;

    @Override
    public TokenVO createToken(SecurityUser user) {
        // token 生成
        String token = UUID.randomUUID().toString().toUpperCase();
        // 扩展内容
        if (user.getExtend() == null) {
            user.setExtend(new HashMap<>());
        }
        var terminalExtraData = user.getExtend();
        var ip = IpUtils.getClientIP(request);
        terminalExtraData.put("ip", ip);
        terminalExtraData.put("address", ipLocationTemplate.getCityEn(ip));

        // 存储 token
        String mainKey = String.format(RedisCacheKey.AUTH_TOKEN_KEY, user.getId(), token);
        String refKey = String.format(RedisCacheKey.TOKEN_TO_USER_KEY, token);

        var ops = redisTemplate.opsForValue();
        ops.set(mainKey, user, securityProperties.getTokenExpire(), TimeUnit.SECONDS);
        ops.set(refKey, user.getId(), securityProperties.getTokenExpire(), TimeUnit.SECONDS);

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
        TokenVO vo = TokenVO.builder()
                .id(user.getId())
                .username(user.getEmail())
                .accessToken(token)
                .authorities(authorities)
                .roles(roles)
                .build();

        // 确保所有流程都结束且没发生错误,才进行 security 上下文设置
        // 把当前用户信息放到上下文中,主要是为了日志记录的时候登录接口无法获取到当前用户信息
        var auth = new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 组件 token
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
        redisTemplate.delete(mainKey);
        redisTemplate.delete(refKey);
    }

    @Override
    public @Nullable SecurityUser getUserByToken(String token) {
        var ops = redisTemplate.opsForValue();
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

}
