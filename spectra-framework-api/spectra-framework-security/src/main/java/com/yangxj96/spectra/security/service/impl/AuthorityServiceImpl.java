package com.yangxj96.spectra.security.service.impl;

import com.yangxj96.spectra.core.base.BaseServiceImpl;
import com.yangxj96.spectra.security.entity.dto.Authority;
import com.yangxj96.spectra.security.mapper.AuthorityMapper;
import com.yangxj96.spectra.security.service.AuthorityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorityServiceImpl extends BaseServiceImpl<AuthorityMapper, Authority> implements AuthorityService {

    protected AuthorityServiceImpl(AuthorityMapper bindMapper) {
        super(bindMapper);
    }

    @Override
    public List<Authority> getByRoleIds(List<Long> roleIds) {
        return this.bindMapper.getByRoleIds(roleIds);
    }
}
