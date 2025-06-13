package com.yangxj96.spectra.core.service.impl;

import com.yangxj96.spectra.common.base.BaseServiceImpl;
import com.yangxj96.spectra.core.javabean.entity.Authority;
import com.yangxj96.spectra.core.mapper.AuthorityMapper;
import com.yangxj96.spectra.core.service.AuthorityService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限service层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Service
public class AuthorityServiceImpl extends BaseServiceImpl<AuthorityMapper, Authority> implements AuthorityService {


    @Override
    public List<Authority> getByRoleIds(List<Long> roleIds) {
        return this.baseMapper.getByRoleIds(roleIds);
    }
}
