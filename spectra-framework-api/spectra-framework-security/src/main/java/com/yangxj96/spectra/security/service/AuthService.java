package com.yangxj96.spectra.security.service;

import com.yangxj96.spectra.security.entity.from.UsernamePasswordFrom;
import com.yangxj96.spectra.security.entity.vo.TokenVO;

import javax.security.auth.login.LoginException;

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
