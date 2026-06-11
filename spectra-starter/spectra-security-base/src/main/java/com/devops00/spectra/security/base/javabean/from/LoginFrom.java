package com.devops00.spectra.security.base.javabean.from;

import com.devops00.spectra.security.base.constant.LoginType;

/// 登录入参
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/2 23:15
public record LoginFrom(
        // 登录方式
        LoginType type,
        // 账号密码
        String username,
        String password,
        String captcha,
        // 手机验证码
        String phone,
        String smsCode,
        // 邮箱验证码
        String email,
        String emailCode,
        // OTP
        String principal,
        String otp

) {
}