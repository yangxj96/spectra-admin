package io.github.yangxj96.spectra.security.auth;

import io.github.yangxj96.spectra.security.api.checker.PermissionChecker;
import io.github.yangxj96.spectra.security.api.context.SecurityContext;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class DefaultPermissionChecker implements PermissionChecker {

    @Override
    public boolean hasPermission(SecurityContext context, String permission) {
        if (context == null) return false;

        // 1. 超级管理员直接放行
        if (context.isAdmin()) return true;

        // 2. 权限列表匹配
        return context.getPermissions().contains(permission);
    }
}
