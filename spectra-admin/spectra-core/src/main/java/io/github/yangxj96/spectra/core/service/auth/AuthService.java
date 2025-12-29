package io.github.yangxj96.spectra.core.service.auth;


import org.springframework.security.core.Authentication;

/**
 * 认证服务
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/29 11:28
 */
public interface AuthService {


    /**
     * 用户名密码登录
     *
     * @param username 用户名
     * @param password 密码
     * @return {@link Authentication} 对象
     */
    Authentication login(String username, String password);

}
