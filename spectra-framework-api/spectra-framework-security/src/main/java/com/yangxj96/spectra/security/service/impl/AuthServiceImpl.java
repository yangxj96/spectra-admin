package com.yangxj96.spectra.security.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.yangxj96.spectra.security.entity.dto.Account;
import com.yangxj96.spectra.security.entity.from.UsernamePasswordFrom;
import com.yangxj96.spectra.security.entity.vo.TokenVO;
import com.yangxj96.spectra.security.service.AccountService;
import com.yangxj96.spectra.security.service.AuthService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;

/**
 * 认证服务
 *
 * @author 杨新杰
 * @since 2025/5/26 19:01
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private AccountService accountService;

    @Override
    public TokenVO login(UsernamePasswordFrom params) throws LoginException {
        Account datum = accountService.getByUsername(params.getUsername());
        if (null == datum) {
            throw new LoginException("账号或密码错误");
        }

        return null;
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

}
