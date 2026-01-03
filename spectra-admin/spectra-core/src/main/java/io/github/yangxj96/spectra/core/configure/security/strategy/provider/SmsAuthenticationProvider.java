package io.github.yangxj96.spectra.core.configure.security.strategy.provider;


import io.github.yangxj96.spectra.common.exception.NotImplementedException;
import io.github.yangxj96.spectra.core.configure.security.strategy.tokens.SmsAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * 短信登录
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/29 10:44
 */
@Component
@RequiredArgsConstructor
public class SmsAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        throw new NotImplementedException("暂未实现");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
