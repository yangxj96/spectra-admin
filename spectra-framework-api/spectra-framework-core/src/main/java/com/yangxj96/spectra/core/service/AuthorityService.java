package com.yangxj96.spectra.core.service;

import com.yangxj96.spectra.common.base.BaseService;
import com.yangxj96.spectra.core.javabean.entity.Authority;

import java.util.List;

/**
 * 权限service层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public interface AuthorityService extends BaseService<Authority> {

    /**
     * 根据角色ID获取权限列表
     *
     * @param roleIds 角色ID列表
     * @return 权限列表
     */
    List<Authority> getByRoleIds(List<Long> roleIds);
}
