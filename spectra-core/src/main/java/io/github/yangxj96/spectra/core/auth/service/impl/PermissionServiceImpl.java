package io.github.yangxj96.spectra.core.auth.service.impl;

import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.stp.StpUtil;
import io.github.yangxj96.spectra.core.auth.service.PermissionService;
import io.github.yangxj96.spectra.core.system.javabean.entity.Organization;
import io.github.yangxj96.spectra.core.system.service.OrganizationService;
import io.github.yangxj96.spectra.core.user.javabean.entity.Role;
import io.github.yangxj96.spectra.core.user.javabean.entity.User;
import io.github.yangxj96.spectra.core.user.service.RoleService;
import io.github.yangxj96.spectra.core.user.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * 权限类,主要用作在SpEL表达式中进行计算 <br/>
 * 根据查看sa-token的源码,发现他所有的验证都是不通过直接抛出异常 <br/>
 * 否则,就是有权限 <br/>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/28
 */
@Slf4j
@Service("ss")
public class PermissionServiceImpl implements PermissionService {

    private static final String ADMINISTRATORS = "DEV_ADMIN";

    @Resource
    private OrganizationService organizationService;

    @Resource
    private RoleService roleService;

    @Resource
    private UserService userService;

    @Override
    public void hasPermission(String permission) {
        if (absoluteness()) {
            return;
        }
        if (StpUtil.hasPermission(permission)) {
            return;
        }
        throw new NotPermissionException(permission);
    }

    @Override
    public void hasRole(String role) {
        if (absoluteness()) {
            return;
        }
        if (StpUtil.hasRole(role)) {
            return;
        }
        throw new NotRoleException(role);
    }

    @Override
    public List<Organization> getCurrentDataScope() {
        if (absoluteness()) {
            return organizationService.list();
        }
        // 获取当前用户信息备用
        User user = userService.getById(StpUtil.getLoginIdAsLong());
        // 获取他的角色列表,从角色列表中获取最高的一个数据范围为准
        List<Role> roles = roleService.getByUserId(user.getId());
        if (CollectionUtils.isEmpty(roles)) {
            return List.of();
        }
        Role maxRole = roles
                .stream()
                .min(Comparator.comparingInt(Role::getScope))
                .orElse(null);
        if (maxRole == null || maxRole.getScope() == null) {
            throw new RuntimeException("无角色或角色配置异常");
        }
        return switch (maxRole.getScope()) {
            // 全局
            case 0 -> organizationService.list();
            // 本级及以下
            case 1 -> organizationService.getAllChildrenById(user.getOrganizationId());
            // 本级
            case 2 -> List.of(organizationService.getById(user.getOrganizationId()));
            default -> List.of();
        };
    }

    /**
     * 内置的无限制通过的范围,在这里可以指定超级管理员的特征 <br/>
     * 比如存在角色CODE为DEV_ADMIN的 <br/>
     * 比如存在权限CODE为 * 的 <br/>
     * 等等方式,进行自定义 <br/>
     *
     * @return 是否为无限制
     */
    private boolean absoluteness() {
        return StpUtil.hasRole(ADMINISTRATORS);
    }
}
