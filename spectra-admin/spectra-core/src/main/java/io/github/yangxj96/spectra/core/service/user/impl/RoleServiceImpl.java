/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.yangxj96.spectra.common.base.BaseServiceImpl;
import io.github.yangxj96.spectra.common.base.javabean.from.PageFrom;
import io.github.yangxj96.spectra.common.exception.BuiltinDataException;
import io.github.yangxj96.spectra.common.exception.DataNotExistException;
import io.github.yangxj96.spectra.common.exception.DefaultDataException;
import io.github.yangxj96.spectra.core.javabean.user.converter.RoleConverter;
import io.github.yangxj96.spectra.core.javabean.user.entity.Role;
import io.github.yangxj96.spectra.core.javabean.user.event.RoleDeletedEvent;
import io.github.yangxj96.spectra.core.javabean.user.from.RoleFrom;
import io.github.yangxj96.spectra.core.javabean.user.from.RolePageFrom;
import io.github.yangxj96.spectra.core.javabean.user.vo.RoleVO;
import io.github.yangxj96.spectra.core.mapper.user.RoleMapper;
import io.github.yangxj96.spectra.core.service.user.RelRoleAuthorityService;
import io.github.yangxj96.spectra.core.service.user.RelRoleMenuService;
import io.github.yangxj96.spectra.core.service.user.RoleService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
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

    @Resource
    private RelRoleMenuService relRoleMenuService;

    @Resource
    private RelRoleAuthorityService relRoleAuthorityService;

    @Resource
    private RoleConverter roleConverter;

    @Resource
    private ApplicationEventPublisher publisher;

    @Override
    @Transactional
    public void created(RoleFrom params) {
        Role role = new Role();
        // 生成一个角色CODE
        role.setCode(IdWorker.get32UUID());
        BeanUtils.copyProperties(params, role);
        this.save(role);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Role role = this.getById(id);
        if (role == null) {
            throw new DataNotExistException("角色不存在");
        }
        if (Boolean.TRUE.equals(role.getBuiltin())) {
            throw new BuiltinDataException("内置角色,不可删除");
        }
        Role defaultRole = this.getSystemDefaultUserRole();
        if (defaultRole == null || defaultRole.getId().equals(id)) {
            throw new DefaultDataException();
        }

        // 发布事物同步的事件
        publisher.publishEvent(new RoleDeletedEvent(id));
        // 在删除角色
        this.removeById(role.getId());
    }

    @Override
    @Transactional
    public void modify(RoleFrom params) {
        Role role = new Role();
        BeanUtils.copyProperties(params, role);
        this.updateById(role);
    }

    @Override
    public IPage<RoleVO> page(PageFrom page, RolePageFrom params) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .like(StringUtils.isNotBlank(params.getName()), Role::getName, params.getName())
                .eq(null != params.getState(), Role::getState, params.getState())
                .orderByAsc(Role::getCreatedAt);
        Page<Role> db = this.page(new Page<>(page.getPageNum(), page.getPageSize()), wrapper);
        Page<RoleVO> result = new Page<>();
        BeanUtils.copyProperties(db, result);
        result.setRecords(roleConverter.toVOs(db.getRecords()));
        return result;
    }

    @Override
    public List<RoleVO> all() {
        var wrapper = new LambdaQueryWrapper<Role>();
        wrapper.eq(Role::getState, Boolean.TRUE);
        return roleConverter.toVOs(this.list(wrapper));
    }

    @Override
    public Role getSystemDefaultUserRole() {
        var wrapper = new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, "ROLE_USER");
        return this.getOne(wrapper);
    }
}
