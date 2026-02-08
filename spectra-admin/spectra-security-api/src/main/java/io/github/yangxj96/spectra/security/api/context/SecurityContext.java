package io.github.yangxj96.spectra.security.api.context;

import java.util.Set;

public interface SecurityContext {

    /** 用户唯一标识 */
    String getUserId();

    /** 权限集合（不含 ROLE_） */
    Set<String> getPermissions();

    /** 角色集合 */
    Set<String> getRoles();

    /** 是否超级管理员 */
    default boolean isAdmin() {
        return false;
    }
}
