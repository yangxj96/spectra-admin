/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.constant.DataScopeType;
import com.devops00.spectra.common.exception.BuiltinDataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DefaultDataException;
import com.devops00.spectra.common.exception.DataScopeViolationException;
import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.core.user.javabean.converter.RoleConverter;
import com.devops00.spectra.core.user.javabean.entity.Role;
import com.devops00.spectra.core.user.javabean.entity.RoleDataScope;
import com.devops00.spectra.core.user.javabean.entity.RoleDataScopeTarget;
import com.devops00.spectra.core.user.javabean.event.RoleDeletedEvent;
import com.devops00.spectra.core.user.javabean.from.RoleFrom;
import com.devops00.spectra.core.user.javabean.from.RolePageFrom;
import com.devops00.spectra.core.user.javabean.vo.RoleVO;
import com.devops00.spectra.core.user.mapper.RoleMapper;
import com.devops00.spectra.core.user.mapper.RoleDataScopeMapper;
import com.devops00.spectra.core.user.mapper.RoleDataScopeTargetMapper;
import com.devops00.spectra.core.user.service.RoleService;
import com.devops00.spectra.security.base.holder.SecUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/// 角色service层-实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/14 00:00
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends BaseServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleConverter roleConverter;

    private final ApplicationEventPublisher publisher;

    private final RoleDataScopeMapper roleDataScopeMapper;

    private final RoleDataScopeTargetMapper roleDataScopeTargetMapper;

    @Override
    @Transactional
    public void created(RoleFrom params) {
        validateScope(params.getScope(), params.getTargetIds());
        Role role = roleConverter.toEntity(params);
        // 生成一个角色 CODE
        role.setCode(IdWorker.get32UUID());
        // 保存角色范围
        this.save(role);
        syncRoleScope(role.getId(), params.getScope(), params.getTargetIds());
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        Role role = this.getById(id);
        if (role == null) {
            throw new DataNotExistException("角色不存在");
        }
        if (role.getBuiltin()) {
            throw new BuiltinDataException("内置角色,不可删除");
        }
        Role defaultRole = this.getSystemDefaultUserRole();
        if (defaultRole.getId().equals(id)) {
            throw new DefaultDataException();
        }

        // 发布事务同步的事件
        publisher.publishEvent(new RoleDeletedEvent(id));
        // 在删除角色
        this.removeById(role.getId());
    }

    @Override
    @Transactional
    public void modify(RoleFrom params) {
        validateScope(params.getScope(), params.getTargetIds());
        Role role = roleConverter.toEntity(params);
        this.updateById(role);
        syncRoleScope(role.getId(), params.getScope(), params.getTargetIds());
    }

    @Override
    public IPage<RoleVO> page(PageFrom page, RolePageFrom params) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .like(StrUtils.isNotBlank(params.getName()), Role::getName, params.getName())
                .eq(null != params.getState(), Role::getState, params.getState())
                .orderByAsc(Role::getCreatedAt);
        Page<Role> db = this.page(new Page<>(page.getPageNum(), page.getPageSize()), wrapper);
        return roleConverter.toVOPage(db);
    }

    @Override
    public List<RoleVO> all() {
        var wrapper = new LambdaQueryWrapper<Role>();
        wrapper.eq(Role::getState, Boolean.TRUE);
        //return roleConverter.toVOs(this.list(wrapper));
        return this.list(wrapper).stream()
                .map(roleConverter::toVO)
                .toList();
    }

    @Override
    public Role getSystemDefaultUserRole() {
        var wrapper = new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, "ROLE_USER");
        return this.getOne(wrapper);
    }

    private void validateScope(DataScopeType type, List<UUID> targetIds) {
        if (type == DataScopeType.GLOBAL && !canManageGlobalScope()) {
            throw new DataScopeViolationException("只有系统运维角色可以授予 GLOBAL 数据范围");
        }
        if (type == DataScopeType.CUSTOM && (targetIds == null || targetIds.isEmpty())) {
            throw new DataScopeViolationException("CUSTOM 数据范围必须指定至少一个目标部门");
        }
    }

    private void syncRoleScope(UUID roleId, DataScopeType type, List<UUID> targetIds) {
        var current = roleDataScopeMapper.selectOne(new LambdaQueryWrapper<RoleDataScope>()
                .eq(RoleDataScope::getRoleId, roleId)
                .isNull(RoleDataScope::getDeleted));
        if (type == null) {
            if (current != null) {
                roleDataScopeMapper.deleteById(current.getId());
            }
            roleDataScopeTargetMapper.delete(new LambdaQueryWrapper<RoleDataScopeTarget>()
                    .eq(RoleDataScopeTarget::getRoleId, roleId));
            return;
        }
        if (current == null) {
            current = new RoleDataScope();
            current.setRoleId(roleId);
        }
        current.setScopeType(type);
        roleDataScopeMapper.insertOrUpdate(current);

        roleDataScopeTargetMapper.delete(new LambdaQueryWrapper<RoleDataScopeTarget>()
                .eq(RoleDataScopeTarget::getRoleId, roleId));
        if (type == DataScopeType.CUSTOM) {
            var targets = targetIds.stream().map(targetId -> {
                var target = new RoleDataScopeTarget();
                target.setRoleId(roleId);
                target.setTargetId(targetId);
                target.setTargetType(type.getCode());
                return target;
            }).toList();
            roleDataScopeTargetMapper.insert(targets);
        }
    }

    private boolean canManageGlobalScope() {
        var currentUser = SecUtil.getCurrentUser();
        return currentUser != null && currentUser.getAuthorities().stream().anyMatch(authority ->
                "ROLE_DEV_OPS".equals(authority.getAuthority()) || "*".equals(authority.getAuthority()));
    }
}
