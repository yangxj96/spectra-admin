package io.github.yangxj96.spectra.security.auth.core;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 代表“已经通过 token 认证的用户”
 */
public class TokenAuthentication extends AbstractAuthenticationToken {

    /// 用户对象 / userId / username
    private final Object principal;

    /// 原始 token 字符串
    private final String token;

    ///
    /// @param principal   用户对象 / userId / username
    /// @param token       原始 token 字符串
    /// @param authorities 权限列表
    public TokenAuthentication(
            Object principal,
            String token,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.principal = principal;
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
