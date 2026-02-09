package io.github.yangxj96.spectra.security.starter.strategy.tokens;


import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/// 邮箱登录token参数
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/29 10:43
public class EmailAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;

    private final Object credentials;

    public EmailAuthenticationToken(String email, String code) {
        super(Collections.emptyList());
        this.principal = email;
        this.credentials = code;
        setAuthenticated(false);
    }

    public EmailAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = null;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }


}
