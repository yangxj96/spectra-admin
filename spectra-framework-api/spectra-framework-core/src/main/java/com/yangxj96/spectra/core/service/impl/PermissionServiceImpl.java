package com.yangxj96.spectra.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yangxj96.spectra.common.base.javabean.from.PageFrom;
import com.yangxj96.spectra.core.javabean.entity.Role;
import com.yangxj96.spectra.core.javabean.from.RoleFrom;
import com.yangxj96.spectra.core.javabean.from.RolePageFrom;
import com.yangxj96.spectra.core.javabean.mapstruct.RoleMapstruct;
import com.yangxj96.spectra.core.javabean.vo.RoleVO;
import com.yangxj96.spectra.core.service.PermissionService;
import com.yangxj96.spectra.core.service.RoleService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 权限service层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private RoleService roleService;

    @Resource
    private RoleMapstruct roleMapstruct;

    @Override
    @Transactional
    public void createdRole(RoleFrom params) {
        Role role = new Role();
        BeanUtils.copyProperties(params, role);
        roleService.save(role);
    }

    @Override
    @Transactional
    public void modifyRole(RoleFrom params) {
        Role role = new Role();
        BeanUtils.copyProperties(params, role);
        roleService.updateById(role);
    }

    @Override
    public IPage<RoleVO> pageRole(PageFrom page, RolePageFrom params) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .like(StringUtils.isNotBlank(params.getName()), Role::getName, params.getName())
                .eq(null != params.getState(), Role::getState, params.getState())
                .orderByAsc(Role::getCreatedAt);
        Page<Role> db = roleService.page(new Page<>(page.getPageNum(), page.getPageSize()), wrapper);
        Page<RoleVO> result = new Page<>();
        BeanUtils.copyProperties(db, result);
        result.setRecords(roleMapstruct.toVOs(db.getRecords()));
        return result;
    }
}
