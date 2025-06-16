package com.yangxj96.spectra.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.yangxj96.spectra.common.base.BaseServiceImpl;
import com.yangxj96.spectra.core.javabean.entity.Role;
import com.yangxj96.spectra.core.mapper.RoleMapper;
import com.yangxj96.spectra.core.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<Role> getByUserId(Long uid) {
        return this.baseMapper.getByUserId(uid);
    }

    @Override
    @Transactional
    public int removeRelevanceRoles(Long uid) {
        return this.baseMapper.removeRelevanceRoles(uid);
    }

    @Override
    @Transactional
    public int insertRelevanceRoles(Long uid, List<Long> roleIds) {
        int num = 0;
        for (Long roleId : roleIds) {
            num += this.baseMapper.insertRelevanceRole(IdWorker.getId(), uid, roleId);
        }
        return num;
    }
}
