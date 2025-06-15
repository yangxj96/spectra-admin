package com.yangxj96.spectra.core.service.impl;

import com.yangxj96.spectra.common.base.BaseServiceImpl;
import com.yangxj96.spectra.core.javabean.entity.Role;
import com.yangxj96.spectra.core.mapper.RoleMapper;
import com.yangxj96.spectra.core.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色service层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Service
public class RoleServiceImpl extends BaseServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public List<Role> getByAccountId(Long accountId) {
        return this.baseMapper.getAccountId(accountId);
    }
}
