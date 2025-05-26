package com.yangxj96.spectra.security.service.impl;

import com.yangxj96.spectra.core.base.BaseServiceImpl;
import com.yangxj96.spectra.security.entity.dto.Role;
import com.yangxj96.spectra.security.mapper.RoleMapper;
import com.yangxj96.spectra.security.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl extends BaseServiceImpl<RoleMapper, Role> implements RoleService {

    protected RoleServiceImpl(RoleMapper bindMapper) {
        super(bindMapper);
    }

    @Override
    public List<Role> getByAccountId(Long accountId) {
        return this.bindMapper.getAccountId(accountId);
    }
}
