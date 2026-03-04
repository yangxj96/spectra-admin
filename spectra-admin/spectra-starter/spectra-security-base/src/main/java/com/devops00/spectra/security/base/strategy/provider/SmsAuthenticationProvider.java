package com.devops00.spectra.security.base.strategy.provider;


import com.devops00.spectra.common.exception.NotImplementedException;
import com.devops00.spectra.security.base.strategy.tokens.SmsAuthenticationToken;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/// 短信登录
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/29 10:44
@Component
@NullMarked
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
