package io.github.yangxj96.spectra.core.javabean.auth.javabean.from;


import io.github.yangxj96.spectra.common.enums.LoginType;

/**
 * 登录入参
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/2 23:15
 */
public record LoginFrom(
        // 登录方式
        LoginType type,
        // email / phone / sceneId / oauthCode
        String identifier,
        // password / smsCode / "" / ""
        String credential,
        // 用于扫码登录（可选）
        String clientId,
        // 验证码,登录方式为email时需要进行验证
        String captcha
) {
}