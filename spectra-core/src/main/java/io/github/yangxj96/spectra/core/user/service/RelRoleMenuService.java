package io.github.yangxj96.spectra.core.user.service;

import io.github.yangxj96.spectra.core.system.javabean.vo.MenuVO;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleMenuFrom;

import java.util.List;

/**
 * 关联服务-角色和菜单
 */
public interface RelRoleMenuService {

    /**
     * 授予角色菜单
     */
    void grant(Long roleId, RoleMenuFrom from);

    /**
     * 撤销角色菜单
     */
    void revoke(Long roleId);

    /**
     * 获取角色菜单
     *
     * @param roleId 角色ID
     * @return 菜单列表
     */
    List<MenuVO> get(Long roleId);

}
