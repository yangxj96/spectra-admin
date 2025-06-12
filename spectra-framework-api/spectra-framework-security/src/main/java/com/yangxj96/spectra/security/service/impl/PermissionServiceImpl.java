package com.yangxj96.spectra.security.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yangxj96.spectra.core.javabean.from.PageFrom;
import com.yangxj96.spectra.security.entity.dto.Role;
import com.yangxj96.spectra.security.entity.from.RoleFrom;
import com.yangxj96.spectra.security.entity.from.RolePageFrom;
import com.yangxj96.spectra.security.entity.vo.RoleVO;
import com.yangxj96.spectra.security.entity.mapstruct.RoleMapstruct;
import com.yangxj96.spectra.security.service.PermissionService;
import com.yangxj96.spectra.security.service.RoleService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 权限操作实现层
 *
 * @author 杨新杰
 * @since 2025/6/9 23:46
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
        BeanUtils.copyProperties(db,result);
        result.setRecords(roleMapstruct.toVOs(db.getRecords()));
        return result;
    }
}
