package io.github.yangxj96.spectra.core.configure.security.strategy.provider;


import io.github.yangxj96.spectra.common.exception.NotImplementedException;
import io.github.yangxj96.spectra.core.configure.security.strategy.tokens.EmailAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * 邮箱登录
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/29 10:46
 */
@Component
@RequiredArgsConstructor
public class EmailAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        throw new NotImplementedException("暂未实现");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return EmailAuthenticationToken.class.isAssignableFrom(authentication);
    }

}