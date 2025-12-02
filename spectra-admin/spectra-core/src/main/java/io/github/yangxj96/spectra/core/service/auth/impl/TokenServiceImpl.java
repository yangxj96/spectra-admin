package io.github.yangxj96.spectra.core.service.auth.impl;


import io.github.yangxj96.spectra.common.utils.IpUtils;
import io.github.yangxj96.spectra.core.javabean.auth.javabean.dto.SecurityUser;
import io.github.yangxj96.spectra.core.javabean.auth.javabean.vo.TokenVO;
import io.github.yangxj96.spectra.core.service.auth.TokenService;
import io.github.yangxj96.spectra.framework.template.IpLocationTemplate;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Token服务
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/2 23:32
 */
@Service
public class TokenServiceImpl implements TokenService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ObjectMapper om;

    @Resource
    private HttpServletRequest request;

    @Resource
    private IpLocationTemplate ipLocationTemplate;

    private static final long TOKEN_EXPIRE_SECONDS = 2 * 60 * 60; // 2小时

    @Override
    public TokenVO createTokenFor(SecurityUser user) {
        // 存储token内容
        String token = generateToken();
        redisTemplate
                .opsForValue()
                .set(token, user, TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // 存储扩展内容
        var terminalExtraData = new HashMap<String, Object>();
        var ip = IpUtils.getClientIP(request);
        terminalExtraData.put("ip", ip);
        terminalExtraData.put("address", ipLocationTemplate.getCityEn(ip));
        redisTemplate
                .opsForValue()
                .set(token + ":" + "ext", terminalExtraData, TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // 组件token
        return TokenVO.builder()
                .id(user.getId())
                .username(user.getEmail())
                .accessToken(token)
                .authorities(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .build();
    }

    @Override
    public SecurityUser getUserByToken(String token) {
        Object object = redisTemplate.opsForValue().get(token);
        return om.convertValue(object, SecurityUser.class);
    }

    @NullMarked
    public static String generateToken() {
        return "tk_" + UUID.randomUUID().toString().replace("-", "");
    }

}
