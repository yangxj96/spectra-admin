/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devops00.spectra.common.exception.BuiltinDataException;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.authorization.entity.SecurityRole;
import com.devops00.spectra.core.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.user.javabean.from.RoleFrom;
import com.devops00.spectra.core.user.javabean.from.RolePageFrom;
import com.devops00.spectra.core.user.javabean.vo.RoleVO;
import com.devops00.spectra.core.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 目标 SecurityRole 角色目录服务。
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<SecurityRoleMapper, SecurityRole> implements RoleService {

    private final RoleAssignmentMapper roleAssignmentMapper;

    @Override
    @Transactional
    public void created(RoleFrom params) {
        var role = new SecurityRole();
        role.setId(UUID.randomUUID());
        role.setCode(hasText(params.getCode()) ? params.getCode() : generatedCode());
        role.setName(params.getName());
        role.setState(Boolean.FALSE.equals(params.getState()) ? "DISABLED" : "ACTIVE");
        role.setRoleKind("BUSINESS");
        role.setAuthorityLevel(1);
        role.setSystemManaged(false);
        role.setRemark(params.getRemark());
        if (count(new LambdaQueryWrapper<SecurityRole>().eq(SecurityRole::getCode, role.getCode())) > 0) {
            throw new DataException("角色编码已存在");
        }
        if (getBaseMapper().insert(role) != 1) {
            throw new DataException("创建角色失败");
        }
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        var role = getBaseMapper().selectById(id);
        if (role == null) {
            throw new DataNotExistException("角色不存在");
        }
        if (Boolean.TRUE.equals(role.getSystemManaged())) {
            throw new BuiltinDataException("系统维护角色不可删除");
        }
        if (roleAssignmentMapper.selectCount(new LambdaQueryWrapper<com.devops00.spectra.core.authorization.entity.RoleAssignment>()
                .eq(com.devops00.spectra.core.authorization.entity.RoleAssignment::getRoleId, id)
                .eq(com.devops00.spectra.core.authorization.entity.RoleAssignment::getState, "ACTIVE")) > 0) {
            throw new DataException("角色仍有有效授权实例，不可停用");
        }
        role.setState("DISABLED");
        if (getBaseMapper().updateById(role) != 1) {
            throw new DataException("停用角色失败");
        }
    }

    @Override
    @Transactional
    public void modify(RoleFrom params) {
        var role = getBaseMapper().selectById(params.getId());
        if (role == null) {
            throw new DataNotExistException("角色不存在");
        }
        if (Boolean.TRUE.equals(role.getSystemManaged())) {
            throw new BuiltinDataException("系统维护角色不可修改");
        }
        if (hasText(params.getCode())
                && !params.getCode().equals(role.getCode())
                && count(new LambdaQueryWrapper<SecurityRole>().eq(SecurityRole::getCode, params.getCode())) > 0) {
            throw new DataException("角色编码已存在");
        }
        if (hasText(params.getCode())) {
            role.setCode(params.getCode());
        }
        role.setName(params.getName());
        role.setState(Boolean.FALSE.equals(params.getState()) ? "DISABLED" : "ACTIVE");
        role.setRemark(params.getRemark());
        if (getBaseMapper().updateById(role) != 1) {
            throw new DataException("修改角色失败");
        }
    }

    @Override
    public IPage<RoleVO> page(com.devops00.spectra.common.base.javabean.from.PageFrom page, RolePageFrom params) {
        var wrapper = new LambdaQueryWrapper<SecurityRole>()
                .like(hasText(params.getName()), SecurityRole::getName, params.getName())
                .eq(params.getState() != null, SecurityRole::getState, Boolean.FALSE.equals(params.getState()) ? "DISABLED" : "ACTIVE")
                .orderByAsc(true, SecurityRole::getId);
        var result = getBaseMapper().selectPage(new Page<>(page.getPageNum(), page.getPageSize()), wrapper);
        return result.convert(this::toVO);
    }

    @Override
    public List<RoleVO> all() {
        return getBaseMapper().selectList(new LambdaQueryWrapper<SecurityRole>()
                .eq(SecurityRole::getState, "ACTIVE")
                .orderByAsc(true, SecurityRole::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public SecurityRole getSystemDefaultUserRole() {
        return getBaseMapper().selectOne(new LambdaQueryWrapper<SecurityRole>()
                .eq(SecurityRole::getCode, "ROLE_USER")
                .eq(SecurityRole::getState, "ACTIVE"));
    }

    private RoleVO toVO(SecurityRole role) {
        var vo = new RoleVO();
        vo.setId(role.getId());
        vo.setName(role.getName());
        vo.setCode(role.getCode());
        vo.setState("ACTIVE".equals(role.getState()));
        vo.setBuiltin(Boolean.TRUE.equals(role.getSystemManaged()));
        vo.setRemark(role.getRemark());
        vo.setAuthorityLevel(role.getAuthorityLevel());
        vo.setRoleKind(role.getRoleKind());
        vo.setVersion(role.getVersion() == null ? 0L : role.getVersion());
        return vo;
    }

    private String generatedCode() {
        return "ROLE_" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
