package io.github.yangxj96.spectra.core.configure.security.strategy.provider;


import io.github.yangxj96.spectra.common.exception.KaptchaNotMatchException;
import io.github.yangxj96.spectra.core.configure.security.strategy.tokens.UsernamePasswordCaptchaAuthenticationToken;
import io.github.yangxj96.spectra.core.service.auth.AuthService;
import io.github.yangxj96.spectra.core.service.common.KaptchaService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * 用户名密码登录
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/29 10:51
 */
@Slf4j
@Component
@NullMarked
public class UsernamPasswordAuthenticationProvider implements AuthenticationProvider {

    @Resource
    private AuthService authService;

    @Resource
    private KaptchaService kaptchaService;

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof UsernamePasswordCaptchaAuthenticationToken params)) {
            throw new RuntimeException("登录失败,未知原因");
        }
        try {
            // 验证码验证
            if (kaptchaService.isCheck() == Boolean.TRUE) {
                var code = kaptchaService.getKaptchaCode();
                if (!params.getCaptcha().equals(code)) {
                    throw new KaptchaNotMatchException("验证码错误");
                }
            }

            if (params.getPrincipal() == null || params.getCredentials() == null) {
                throw new BadCredentialsException("用户名或密码不能为空");
            }
            return authService.login(params.getPrincipal().toString(), params.getCredentials().toString());
        } finally {
            // 不管登录是否失败,都需要删除掉验证码
            kaptchaService.deleteBySessionId();
        }
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordCaptchaAuthenticationToken.class
                .isAssignableFrom(authentication);
    }
}
