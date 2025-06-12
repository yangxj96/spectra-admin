package com.yangxj96.spectra.security.service;

import com.yangxj96.spectra.core.base.BaseService;
import com.yangxj96.spectra.security.entity.dto.Role;

import java.util.List;

public interface RoleService extends BaseService<Role> {


    /**
     * 根据账号ID获取所拥有的角色
     *
     * @param accountId 账号 ID
     * @return 角色列表
     */
    List<Role> getByAccountId(Long accountId);
}
