package com.yangxj96.spectra.core.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.yangxj96.spectra.core.annotation.ULog;
import com.yangxj96.spectra.core.javabean.from.UsernamePasswordFrom;
import com.yangxj96.spectra.core.javabean.vo.TokenVO;
import com.yangxj96.spectra.core.service.AuthService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.LoginException;

/**
 * 认证控制器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService bindService;

    @SaIgnore
    @ULog("登录")
    @PostMapping("/login")
    public TokenVO login(@Validated @RequestBody UsernamePasswordFrom params) throws LoginException {
        return bindService.login(params);
    }

    @SaCheckLogin
    @ULog("退出登录")
    @PostMapping("/logout")
    public void logout() {
        bindService.logout();
    }
}
