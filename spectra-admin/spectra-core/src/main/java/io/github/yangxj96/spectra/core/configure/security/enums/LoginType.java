package io.github.yangxj96.spectra.core.configure.security.enums;

/**
 * 登录方式支持
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/2 23:14
 */
public enum LoginType {
    PASSWORD,      // 账号密码
    SMS,           // 手机验证码
    SCAN,          // 扫码
    WECHAT,        // 微信（可选）
    GITHUB         // GitHub（可选）
}
