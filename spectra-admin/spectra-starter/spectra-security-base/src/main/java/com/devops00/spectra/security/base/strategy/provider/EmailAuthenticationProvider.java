package com.devops00.spectra.security.base.strategy.provider;


import com.devops00.spectra.common.exception.NotImplementedException;
import com.devops00.spectra.security.base.strategy.tokens.EmailAuthenticationToken;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/// 邮箱登录
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/29 10:46
@NullMarked
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