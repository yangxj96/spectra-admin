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

    @Resource
    private SecurityProperties securityProperties;

    @Override
    public TokenVO createTokenFor(SecurityUser user) {
        // token生成
        String token = UUID.randomUUID().toString().toUpperCase();
        // 扩展内容
        if (user.getExtend() == null) {
            user.setExtend(new HashMap<>());
        }
        var terminalExtraData = user.getExtend();
        var ip = IpUtils.getClientIP(request);
        terminalExtraData.put("ip", ip);
        terminalExtraData.put("address", ipLocationTemplate.getCityEn(ip));

        // 存储token
        String mainKey = String.format(RedisCacheKey.AUTH_TOKEN_KEY, user.getId(), token);
        String refKey = String.format(RedisCacheKey.TOKEN_TO_USER_KEY, token);

        var ops = redisTemplate.opsForValue();
        ops.set(mainKey, user, securityProperties.getTokenExpire(), TimeUnit.SECONDS);
        ops.set(refKey, user.getId(), securityProperties.getTokenExpire(), TimeUnit.SECONDS);

        // 组件token
        return TokenVO.builder()
                .id(user.getId())
                .username(user.getEmail())
                .accessToken(token)
                .authorities(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .build();
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
        //redisTemplate.execute((RedisCallback<Void>) connection -> {
        //    var cmd = connection.keyCommands();
        //    var opts = ScanOptions.scanOptions()
        //            .match(String.format(RedisCacheKey.AUTH_TOKEN_KEY, user.getId(), token) + "*")
        //            .build();
        //    try (var keys = cmd.scan(opts)) {
        //        var coll = new ArrayList<byte[]>();
        //        while (keys.hasNext()) {
        //            coll.add(keys.next());
        //        }
        //        if (CollUtils.isNotEmpty(coll)) {
        //            cmd.del(coll.toArray(new byte[0][]));
        //        }
        //    }
        //    return null;
        //});
    }

    @Override
    public SecurityUser getUserByToken(String token) {
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
