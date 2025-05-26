package com.yangxj96.spectra.security.service;

import com.yangxj96.spectra.core.base.BaseService;
import com.yangxj96.spectra.security.entity.dto.Authority;

import java.util.List;

public interface AuthorityService extends BaseService<Authority> {

    /**
     * 根据角色ID获取权限列表
     *
     * @param roleIds 角色ID列表
     * @return 权限列表
     */
    List<Authority> getByRoleIds(List<Long> roleIds);
}
