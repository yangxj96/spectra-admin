package io.github.yangxj96.spectra.security.api.checker;

import io.github.yangxj96.spectra.security.api.context.SecurityContext;

/**
 * 权限判断 SPI
 */
public interface PermissionChecker {

    boolean hasPermission(SecurityContext context, String permission);

}
