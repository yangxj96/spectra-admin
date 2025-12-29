package io.github.yangxj96.spectra.core.configure.security.strategy;


import io.github.yangxj96.spectra.core.configure.security.javabean.LoginType;
import io.github.yangxj96.spectra.core.configure.security.strategy.tokens.EmailAuthenticationToken;
import io.github.yangxj96.spectra.core.configure.security.strategy.tokens.SmsAuthenticationToken;
import io.github.yangxj96.spectra.core.configure.security.strategy.tokens.UsernamePasswordCaptchaAuthenticationToken;
import io.github.yangxj96.spectra.core.javabean.auth.from.LoginFrom;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * 登录分发器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/29 10:47
 */
@Component
@RequiredArgsConstructor
public class LoginDispatcher {

    private final AuthenticationManager authenticationManager;

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
