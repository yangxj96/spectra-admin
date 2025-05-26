package com.yangxj96.spectra.security.configuation;

import cn.dev33.satoken.stp.StpInterface;
import com.yangxj96.spectra.security.entity.dto.Authority;
import com.yangxj96.spectra.security.entity.dto.Role;
import com.yangxj96.spectra.security.service.AuthorityService;
import com.yangxj96.spectra.security.service.RoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义权限加载接口实现类
 *
 * @author 杨新杰
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
        List<Role> roles = roleService.getByAccountId((Long) loginId);
        List<Authority> authorities = authorityService.getByRoleIds(roles.stream().map(Role::getId).collect(Collectors.toList()));
        return authorities.stream().map(Authority::getName).collect(Collectors.toList());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<Role> roles = roleService.getByAccountId((Long) loginId);
        return roles.stream().map(Role::getName).collect(Collectors.toList());
    }
}
