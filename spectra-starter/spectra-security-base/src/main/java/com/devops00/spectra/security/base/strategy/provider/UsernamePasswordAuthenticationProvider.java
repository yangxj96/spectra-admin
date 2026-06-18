package com.devops00.spectra.security.base.strategy.provider;


import com.devops00.spectra.security.base.strategy.tokens.UsernamePasswordCaptchaAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/// 用户名密码登录
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/29 10:51
@Slf4j
@NullMarked
public abstract class UsernamePasswordAuthenticationProvider implements BasicAuthenticationProvider {

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof UsernamePasswordCaptchaAuthenticationToken params)) {
            throw new RuntimeException("登录失败,未知原因");
        }
        try {
            // 验证码验证
            kaptchaValidate(params.getCaptcha());

            if (params.getPrincipal() == null || params.getCredentials() == null) {
                throw new BadCredentialsException("用户名或密码不能为空");
            }

            return login(params.getPrincipal().toString(), params.getCredentials().toString());
        } finally {
            // 不管登录是否失败,都需要删除掉验证码
            kaptchaDelete();
        }
    }

    public abstract Authentication login(String username, String password) throws AuthenticationException;

    public abstract void kaptchaValidate(String kaptcha);

    public abstract void kaptchaDelete();

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordCaptchaAuthenticationToken.class
                .isAssignableFrom(authentication);
    }
}
