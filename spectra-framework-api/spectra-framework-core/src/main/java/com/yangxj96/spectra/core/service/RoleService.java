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
     * @param accountId 账号 ID
     * @return 角色列表
     */
    List<Role> getByAccountId(Long accountId);
}
