package com.yangxj96.spectra.core.auth.service;

import com.yangxj96.spectra.core.system.javabean.entity.Organization;

import java.util.List;

/**
 * 权限类,主要用作在SpEL表达式中进行计算
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/28
 */
public interface PermissionService {

    /**
     * 判断是否有指定权限
     *
     * @param permission 指定权限CODE
     */
    void hasPermission(String permission);

    /**
     * 判断是否有指定角色
     *
     * @param role 指定角色CODE
     */
    void hasRole(String role);

    /**
     * 获取当前用户的数据范围
     * TODO 当前还是没考虑好数据范围怎么定义比较灵活,几个方案在脑海备选.等在考虑考虑
     * @return 组织机构ID列表
     */
    List<Organization> getCurrentDataScope();
}
