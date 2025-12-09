package io.github.yangxj96.spectra.core.template;


import io.github.yangxj96.spectra.core.configure.security.properties.SecurityProperties;
import io.github.yangxj96.spectra.core.javabean.auth.SecurityUser;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 安全相关工具服务
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/5 14:30
 */
@Slf4j
@NullMarked
@Component("sec")
public class SecurityTemplate {

    private static final String PREFIX = "[SecurityTemplate]";

    @Resource
    private SecurityProperties properties;

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public @Nullable Long getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            log.debug("{}当前用户:为null", PREFIX);
            return null;
        }
        if (authentication.getPrincipal() instanceof SecurityUser user) {
            return user.getId();
        }
        log.debug("{}当前用户不是 SecurityUser", PREFIX);
        return null;
    }

    /**
     * 获取当前用户权限
     *
     * @return 用户权限列表,可能是空列表,但是不会是null
     */
    public List<? extends GrantedAuthority> getCurrentUserAuthority() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            log.debug("{}当前认证信息为 null", PREFIX);
            return Collections.emptyList();
        }
        return new ArrayList<>(authentication.getAuthorities());
    }

    /**
     * 获取当前用户token
     *
     * @return 用户ID
     */
    public @Nullable String getCurrentUserToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            log.debug("{}未能获取到 Authentication 对象或用户对象", PREFIX);
            return null;
        }
        if (authentication.getCredentials() instanceof String token) {
            return token;
        }
        log.debug("{}未能获取到 token", PREFIX);
        return null;
    }

    /**
     * 获取当前的管理员用户信息
     *
     * @return 管理员角色
     */
    public String getAdministrators() {
        return properties.getAdministrators();
    }

    /**
     * 根据用户 ID 提出用户
     * @param userId 用户 ID
     */
    public void kickByUserId(Long userId) {

    }
}
