package com.yangxj96.spectra.core.common.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.yangxj96.spectra.core.common.service.KaptchaService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 通用的一些接口
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/25
 */
@SaIgnore
@RestController
@RequestMapping("/common")
public class CommonController {

    @Resource
    private KaptchaService kaptchaService;

    /**
     * 获取验证码
     */
    @GetMapping("/kaptcha")
    public void kaptcha() throws IOException {
        kaptchaService.generate();
    }

    @GetMapping("/check")
    public String check() {
        return "OK";
    }
}
