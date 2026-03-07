package com.devops00.spectra.security.starter.web.controller;


import com.devops00.spectra.security.base.holder.SecUtil;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import com.devops00.spectra.security.base.javabean.from.LoginFrom;
import com.devops00.spectra.security.base.javabean.vo.TokenVO;
import com.devops00.spectra.security.starter.web.dispatcher.LoginDispatcher;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证处理器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/2/17 23:28
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginDispatcher loginDispatcher;


    public AuthController(LoginDispatcher loginDispatcher) {
        this.loginDispatcher = loginDispatcher;
    }

    @PermitAll
    @PostMapping("/login")
    // @ULog(value = "用户登录", type = SysLogType.SAFETY)
    public TokenVO login(@Validated @RequestBody LoginFrom params) {
        var authentication = loginDispatcher.authenticate(params);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        if (authentication.getPrincipal() instanceof SecurityUser su) {
            return SecUtil.login(su);
        } else {
            throw new UsernameNotFoundException("未找到用户");
        }
    }


    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    // @ULog(value = "用户登出", type = SysLogType.SAFETY)
    public void logout() {
        SecUtil.logout();
    }


    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/check", version = "1.0.0+")
    //@ULog("token 检查", type = SysLogType.SAFETY)
    public void check() {
        // 能进入方法就说明 token 是正常的
    }
}
