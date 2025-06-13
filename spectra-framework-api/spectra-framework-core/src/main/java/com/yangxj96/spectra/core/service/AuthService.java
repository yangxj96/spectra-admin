package com.yangxj96.spectra.core.service;

import com.yangxj96.spectra.core.javabean.from.UsernamePasswordFrom;
import com.yangxj96.spectra.core.javabean.vo.TokenVO;

import javax.security.auth.login.LoginException;

/**
 * 认证service层
 *
 * @author Jack Young
 * @since 2025/6/13 15:14
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param params 账号密码登录参数
     * @return 成功则响应token
     */
    TokenVO login(UsernamePasswordFrom params) throws LoginException;

    /**
     * 用户退出
     */
    void logout();

}
