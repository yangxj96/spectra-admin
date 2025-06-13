package com.yangxj96.spectra.core.configuration.satoekn;

import cn.dev33.satoken.stp.StpInterface;
import com.yangxj96.spectra.core.javabean.entity.Authority;
import com.yangxj96.spectra.core.javabean.entity.Role;
import com.yangxj96.spectra.core.service.AuthorityService;
import com.yangxj96.spectra.core.service.RoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 自定义权限加载接口实现类
 *
 * @author Jack Young
 * @since 2025/5/26 19:07
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private RoleService roleService;

    @Resource
    private AuthorityService authorityService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<Role> roles = roleService.getByAccountId(Long.parseLong((String) loginId));
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Authority> authorities = authorityService.getByRoleIds(roles.stream().map(Role::getId).toList());
        return authorities.stream().map(Authority::getName).toList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<Role> roles = roleService.getByAccountId(Long.parseLong((String) loginId));
        return roles.stream().map(Role::getName).toList();
    }
}
