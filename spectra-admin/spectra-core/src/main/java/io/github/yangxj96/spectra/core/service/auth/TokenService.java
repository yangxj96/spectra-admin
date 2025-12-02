package io.github.yangxj96.spectra.core.service.auth;

import io.github.yangxj96.spectra.core.javabean.auth.javabean.dto.SecurityUser;
import io.github.yangxj96.spectra.core.javabean.auth.javabean.vo.TokenVO;

/**
 * Token服务
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/2 23:32
 */
public interface TokenService {

    /**
     * 根据用户创建token
     *
     * @param user 用户信息
     * @return token信息
     */
    TokenVO createTokenFor(SecurityUser user);

    /**
     * 根据token获取用户信息
     *
     * @param token token信息
     * @return 用户信息
     */
    SecurityUser getUserByToken(String token);
}
