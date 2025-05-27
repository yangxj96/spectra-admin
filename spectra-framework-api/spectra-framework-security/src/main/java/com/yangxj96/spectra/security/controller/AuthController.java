package com.yangxj96.spectra.security.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.yangxj96.spectra.core.response.R;
import com.yangxj96.spectra.security.entity.from.UsernamePasswordFrom;
import com.yangxj96.spectra.security.entity.vo.TokenVO;
import com.yangxj96.spectra.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.LoginException;

/**
 * 认证控制器
 *
 * @author 杨新杰
 * @since 2025/5/26 16:56
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "认证控制器", description = "用户登录,退出等操作")
public class AuthController {

    @Resource
    private AuthService bindService;

    @SaIgnore
    @PostMapping("/login")
    @Operation(
            summary = "用户登录",
            description = "使用账号密码进行登录",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "用户名密码登录的请求实体",
                    content = @Content(schema = @Schema(implementation = UsernamePasswordFrom.class))
            )
    )
    public R<TokenVO> login(@RequestBody @Validated UsernamePasswordFrom params) throws LoginException {
        return R.success(bindService.login(params));
    }

    @SaCheckLogin
    @PostMapping("/logout")
    @Operation(summary = "用户退出", description = "通用用户退出")
    public void logout() {
        bindService.logout();
    }
}
