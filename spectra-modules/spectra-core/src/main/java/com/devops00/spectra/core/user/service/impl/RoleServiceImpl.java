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
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.security.authorization.entity.RoleAssignment;
import com.devops00.spectra.core.security.authorization.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.constant.SecurityAuthorizationState;
import com.devops00.spectra.core.security.authorization.constant.SecurityRoleCodes;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.user.javabean.from.RoleFrom;
import com.devops00.spectra.core.user.javabean.from.RolePageFrom;
import com.devops00.spectra.core.user.javabean.vo.RoleVO;
import com.devops00.spectra.core.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 目标 SecurityRole 角色目录服务。
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<SecurityRoleMapper, SecurityRole> implements RoleService {

    private static final String ROLE_CODE_PREFIX = "ROLE_";

    private static final int GENERATED_ROLE_CODE_SUFFIX_LENGTH = 8;

    private final RoleAssignmentMapper roleAssignmentMapper;

    @Override
    @Transactional
    public RoleVO save(RoleFrom params) {
        if (params.getId() == null) {
            var role = new SecurityRole();
            var name = normalize(params.getName());
            ensureNameAvailable(name, null);
            var codeProvided = StringUtils.hasText(params.getCode());
            var code = codeProvided ? normalize(params.getCode()) : generatedCode();
            if (codeProvided && codeExists(code, null)) {
                throw new DataException("角色编码已存在");
            }
            role.setCode(code);
            role.setName(name);
            role.setState(SecurityAuthorizationState.ACTIVE.name());
            role.setRoleKind("BUSINESS");
            role.setAuthorityLevel(1);
            role.setSystemManaged(false);
            role.setRemark(params.getRemark());
            if (getBaseMapper().insert(role) != 1) {
                throw new DataException("创建角色失败");
            }
            return toVO(role);
        }

        var role = getBaseMapper().selectById(params.getId());
        if (role == null) {
            throw new DataNotExistException("角色不存在");
        }
        ensureNotSystemManaged(role);
        var code = normalize(params.getCode());
        if (StringUtils.hasText(code) && !code.equals(role.getCode())) {
            throw new DataException("角色编码不可修改");
        }
        var name = normalize(params.getName());
        ensureNameAvailable(name, role.getId());
        role.setName(name);
        role.setRemark(params.getRemark());
        if (getBaseMapper().updateById(role) != 1) {
            throw new DataException("修改角色失败");
        }
        return toVO(role);
    }

    @Override
    @Transactional
    public void enable(UUID id) {
        var role = getRole(id);
        ensureNotSystemManaged(role);
        if (SecurityAuthorizationState.ACTIVE.name().equals(role.getState())) {
            return;
        }
        role.setState(SecurityAuthorizationState.ACTIVE.name());
        if (getBaseMapper().updateById(role) != 1) {
            throw new DataException("启用角色失败");
        }
    }

    @Override
    @Transactional
    public void disable(UUID id) {
        var role = getRole(id);
        ensureNotSystemManaged(role);
        if (SecurityAuthorizationState.DISABLED.name().equals(role.getState())) {
            return;
        }
        ensureNoActiveAssignments(id, "禁用");
        role.setState(SecurityAuthorizationState.DISABLED.name());
        if (getBaseMapper().updateById(role) != 1) {
            throw new DataException("禁用角色失败");
        }
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        var role = getRole(id);
        ensureNotSystemManaged(role);
        ensureNoActiveAssignments(id, "删除");
        if (!removeById(id)) {
            throw new DataException("删除角色失败");
        }
    }

    @Override
    public IPage<RoleVO> page(PageFrom page, RolePageFrom params) {
        var wrapper = new LambdaQueryWrapper<SecurityRole>()
                .like(StringUtils.hasText(params.getName()), SecurityRole::getName, params.getName())
                .eq(params.getState() != null, SecurityRole::getState,
                        Boolean.FALSE.equals(params.getState())
                                ? SecurityAuthorizationState.DISABLED.name()
                                : SecurityAuthorizationState.ACTIVE.name())
                .orderByDesc(true, SecurityRole::getSystemManaged)
                .orderByDesc(true, SecurityRole::getAuthorityLevel)
                .orderByAsc(true, SecurityRole::getState)
                .orderByAsc(true, SecurityRole::getName)
                .orderByAsc(true, SecurityRole::getId);
        var result = getBaseMapper().selectPage(new Page<>(page.getPageNum(), page.getPageSize()), wrapper);
        return result.convert(this::toVO);
    }

    @Override
    public List<RoleVO> all() {
        return getBaseMapper().selectList(new LambdaQueryWrapper<SecurityRole>()
                .eq(SecurityRole::getState, SecurityAuthorizationState.ACTIVE.name())
                .orderByAsc(true, SecurityRole::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleVO detail(UUID id) {
        return toVO(getRole(id));
    }

    @Override
    public SecurityRole getSystemDefaultUserRole() {
        return getBaseMapper().selectOne(new LambdaQueryWrapper<SecurityRole>()
                .eq(SecurityRole::getCode, SecurityRoleCodes.DEFAULT_USER)
                .eq(SecurityRole::getState, SecurityAuthorizationState.ACTIVE.name()));
    }

    /**
     * 转换、解析或规范化数据（{@code toVO}）。
     */
    private RoleVO toVO(SecurityRole role) {
        var vo = new RoleVO();
        vo.setId(role.getId());
        vo.setName(role.getName());
        vo.setCode(role.getCode());
        vo.setState(SecurityAuthorizationState.ACTIVE.name().equals(role.getState()));
        vo.setBuiltin(Boolean.TRUE.equals(role.getSystemManaged()));
        vo.setRemark(role.getRemark());
        vo.setAuthorityLevel(role.getAuthorityLevel());
        vo.setRoleKind(role.getRoleKind());
        vo.setVersion(role.getVersion() == null ? Long.valueOf(0L) : role.getVersion());
        return vo;
    }

    /**
     * 创建或构建目标数据（{@code generatedCode}）。
     */
    private String generatedCode() {
        String code;
        do {
            var suffix = UUID.randomUUID().toString().replace("-", "").substring(0, GENERATED_ROLE_CODE_SUFFIX_LENGTH);
            code = ROLE_CODE_PREFIX + suffix.toUpperCase(Locale.ROOT);
        } while (codeExists(code, null));
        return code;
    }

    /**
     * 处理内部业务逻辑（{@code ensureNameAvailable}）。
     */
    private void ensureNameAvailable(String name, UUID excludedId) {
        var wrapper = new LambdaQueryWrapper<SecurityRole>().eq(SecurityRole::getName, name);
        if (excludedId != null) {
            wrapper.ne(SecurityRole::getId, excludedId);
        }
        if (count(wrapper) > 0) {
            throw new DataException("角色名称已存在");
        }
    }

    /**
     * 查询或获取目标数据（{@code codeExists}）。
     */
    private boolean codeExists(String code, UUID excludedId) {
        var wrapper = new LambdaQueryWrapper<SecurityRole>().eq(SecurityRole::getCode, code);
        if (excludedId != null) {
            wrapper.ne(SecurityRole::getId, excludedId);
        }
        return count(wrapper) > 0;
    }

    /**
     * 转换、解析或规范化数据（{@code normalize}）。
     */
    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * 查询或获取目标数据（{@code getRole}）。
     */
    private SecurityRole getRole(UUID id) {
        var role = getBaseMapper().selectById(id);
        if (role == null) {
            throw new DataNotExistException("角色不存在");
        }
        return role;
    }

    /**
     * 处理内部业务逻辑（{@code ensureNotSystemManaged}）。
     */
    private void ensureNotSystemManaged(SecurityRole role) {
        if (Boolean.TRUE.equals(role.getSystemManaged()) || !"BUSINESS".equals(role.getRoleKind())) {
            throw new BuiltinDataException("内置角色不可操作");
        }
    }

    /**
     * 处理内部业务逻辑（{@code ensureNoActiveAssignments}）。
     */
    private void ensureNoActiveAssignments(UUID roleId, String operation) {
        if (roleAssignmentMapper.selectCount(new LambdaQueryWrapper<RoleAssignment>()
                .eq(RoleAssignment::getRoleId, roleId)
                .eq(RoleAssignment::getState, SecurityAuthorizationState.ACTIVE.name())) > 0) {
            throw new DataException("角色仍有有效授权实例，不可" + operation);
        }
    }

}
