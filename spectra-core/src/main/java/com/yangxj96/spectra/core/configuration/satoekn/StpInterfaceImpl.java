package com.yangxj96.spectra.core.configuration.satoekn;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
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
 * @version 1.0
 * @since 2025-6-14
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private RoleService roleService;

    @Resource
    private AuthorityService authorityService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Object userId = StpUtil.getTerminalInfo().getExtra("user_id");
        if (userId instanceof Long uid) {
            List<Role> roles = roleService.getByUserId(uid);
            if (roles.isEmpty()) {
                return Collections.emptyList();
            }
            List<Authority> authorities = authorityService.getByRoleIds(roles.stream().map(Role::getId).toList());
            return authorities.stream().map(Authority::getCode).toList();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Object userId = StpUtil.getTerminalInfo().getExtra("user_id");
        if (userId instanceof Long uid) {
            List<Role> roles = roleService.getByUserId(uid);
            return roles.stream().map(Role::getCode).toList();
        } else {
            return Collections.emptyList();
        }
    }
}
