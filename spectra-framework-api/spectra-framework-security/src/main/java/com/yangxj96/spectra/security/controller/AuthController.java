package com.yangxj96.spectra.security.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.yangxj96.spectra.core.response.R;
import com.yangxj96.spectra.security.entity.from.UsernamePasswordFrom;
import com.yangxj96.spectra.security.entity.vo.TokenVO;
import com.yangxj96.spectra.security.service.AuthService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.LoginException;

/**
 * 认证控制器
 *
 * @author 杨新杰
 * @since 2025/5/26 16:56
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService bindService;

    @SaIgnore
    @PostMapping("/login")
    public R<TokenVO> login(@Validated UsernamePasswordFrom params) throws LoginException {
        return R.success(bindService.login(params));
    }

    @SaCheckLogin
    @PostMapping("/logout")
    public void logout() {
        bindService.logout();
    }
}
