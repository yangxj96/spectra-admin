package com.yangxj96.spectra.security.service;

import com.yangxj96.spectra.security.entity.from.RoleFrom;

/**
 * 权限操作业务层
 */
public interface PermissionService {

    /**
     * 新增角色
     *
     * @param params 角色入参实体
     */
    void createdRole(RoleFrom params);

}
