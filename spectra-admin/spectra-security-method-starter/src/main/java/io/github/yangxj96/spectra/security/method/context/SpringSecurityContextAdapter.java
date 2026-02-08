package io.github.yangxj96.spectra.security.method.context;

import io.github.yangxj96.spectra.security.api.context.SecurityContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 将 Spring Security 的 Authentication 适配为自定义 SecurityContext
 */
public class SpringSecurityContextAdapter implements SecurityContext {

    private final Authentication authentication;

    public SpringSecurityContextAdapter(Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public String getUserId() {
        if (authentication == null) return null;
        return authentication.getName(); // 这里用用户名作为唯一标识
    }

    @Override
    public Set<String> getPermissions() {
        if (authentication == null) return Set.of();
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_")) // 排除角色
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getRoles() {
        if (authentication == null) return Set.of();
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isAdmin() {
        return getRoles().contains("ROLE_ADMIN");
    }
}
