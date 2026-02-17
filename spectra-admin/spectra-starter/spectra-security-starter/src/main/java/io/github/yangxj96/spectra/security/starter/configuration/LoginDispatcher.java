package io.github.yangxj96.spectra.security.starter.configuration;


import io.github.yangxj96.spectra.security.base.constant.LoginType;
import io.github.yangxj96.spectra.security.base.javabean.from.LoginFrom;
import io.github.yangxj96.spectra.security.base.strategy.tokens.EmailAuthenticationToken;
import io.github.yangxj96.spectra.security.base.strategy.tokens.SmsAuthenticationToken;
import io.github.yangxj96.spectra.security.base.strategy.tokens.UsernamePasswordCaptchaAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/// 登录分发器
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/29 10:47
@Component
public class LoginDispatcher {

    private final AuthenticationManager authenticationManager;

    public LoginDispatcher(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /// 进行登录
    ///
    /// @param request 登录请求参数
    /// @return 登录结果
    public Authentication authenticate(LoginFrom request) {

        return switch (request.type()) {
            case LoginType.PASSWORD -> authenticationManager.authenticate(
                    new UsernamePasswordCaptchaAuthenticationToken(
                            request.username(),
                            request.password(),
                            request.captcha()
                    )
            );

            case LoginType.SMS -> authenticationManager.authenticate(
                    new SmsAuthenticationToken(
                            request.phone(),
                            request.smsCode()
                    )
            );

            case LoginType.EMAIL -> authenticationManager.authenticate(
                    new EmailAuthenticationToken(
                            request.email(),
                            request.emailCode()
                    )
            );

            default -> throw new IllegalArgumentException("不支持的登录类型");
        };
    }

}
