package com.devops00.spectra.security.base.strategy.provider;


import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import org.springframework.security.authentication.AuthenticationProvider;

import java.util.List;

/**
 * 基础的认证适配器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/2/17 23:47
 */
public interface SpectraAuthenticationProvider extends AuthenticationProvider {

    /// 用户对象转SecurityUser对象
    ///
    /// @param user 用户对象
    /// @return SecurityUser对象
    SecurityUser toSecurityUser(Object user);

    /// 根据UserId获取角色列表
    ///
    /// @param userId 用户ID
    /// @return 角色列表
    List<Object> getUserRole(String userId);

    /// 根据角色ID列表获取权限列表
    ///
    /// @param roles 角色ID列表
    /// @return 权限列表
    List<Object> getUserAuthority(List<String> roles);
}
