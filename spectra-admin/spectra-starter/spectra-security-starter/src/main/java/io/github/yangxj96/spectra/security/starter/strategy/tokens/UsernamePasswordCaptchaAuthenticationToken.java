package io.github.yangxj96.spectra.security.starter.strategy.tokens;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/// 用户名密码+验证码登录
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/29 10:55
public class UsernamePasswordCaptchaAuthenticationToken extends AbstractAuthenticationToken {

    ///  用户名
    private final String username;

    /// 密码
    private final String password;

    /// 验证码
    @Getter
    private final String captcha;

    public UsernamePasswordCaptchaAuthenticationToken(
            String username, String password, String captcha) {
        super(Collections.emptyList());
        this.username = username;
        this.password = password;
        this.captcha = captcha;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return password;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

}
