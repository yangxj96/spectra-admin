package io.github.yangxj96.spectra.security.base.strategy.tokens;


import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/// 短信登录TOKEN
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/29 10:42
public class SmsAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final Object credentials;

    public SmsAuthenticationToken(String phone, String code) {
        super(Collections.emptyList());
        this.principal = phone;
        this.credentials = code;
        setAuthenticated(false);
    }

    public SmsAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
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
