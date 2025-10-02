package io.github.yangxj96.spectra.core.auth.service;

import io.github.yangxj96.spectra.common.enums.AuthScope;
import io.github.yangxj96.spectra.core.system.javabean.entity.Menu;
import io.github.yangxj96.spectra.core.user.javabean.entity.Role;

import java.util.List;

/**
 * 安全服务
 */
public interface SecurityService {

    /**
     * 获取当前用户角色
     *
     * @return 当前用户角色列表
     */
    List<Role> getCurrentRoles();

    /**
     * 获取当前用户菜单
     *
     * @return 当前用户菜单列表
     */
    List<Menu> getCurrentMenus();

    /**
     * 获取当前用户最大权限范围
     *
     * @return 最大权限范围
     */
    AuthScope getCurrentMaxScope();
}
