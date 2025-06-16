package com.yangxj96.spectra.core.service;

import com.yangxj96.spectra.common.base.BaseService;
import com.yangxj96.spectra.core.javabean.entity.Role;

import java.util.List;

/**
 * 角色service层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public interface RoleService extends BaseService<Role> {

    /**
     * 根据账号ID获取所拥有的角色
     *
     * @param uid 账号ID
     * @return 角色列表
     */
    List<Role> getByUserId(Long uid);

    /**
     * 删除关联的角色列表
     *
     * @param uid 用户ID
     * @return 删除的条目数
     */
    int removeRelevanceRoles(Long uid);

    /**
     * 新增关联角色列表
     *
     * @param uid     用户ID
     * @param roleIds 角色ID
     * @return 新增的条目数
     */
    int insertRelevanceRoles(Long uid, List<Long> roleIds);
}
