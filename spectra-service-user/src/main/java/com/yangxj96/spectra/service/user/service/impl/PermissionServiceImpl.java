/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yangxj96.spectra.common.base.javabean.from.PageFrom;
import com.yangxj96.spectra.common.constant.Common;
import com.yangxj96.spectra.common.utils.TreeBuilder;
import com.yangxj96.spectra.service.user.javabean.entity.Authority;
import com.yangxj96.spectra.service.user.javabean.entity.Role;
import com.yangxj96.spectra.service.user.javabean.from.RoleFrom;
import com.yangxj96.spectra.service.user.javabean.from.RolePageFrom;
import com.yangxj96.spectra.service.user.javabean.mapstruct.PermissionMapstruct;
import com.yangxj96.spectra.service.user.javabean.vo.AuthorityTreeVO;
import com.yangxj96.spectra.service.user.javabean.vo.RoleVO;
import com.yangxj96.spectra.service.user.service.AuthorityService;
import com.yangxj96.spectra.service.user.service.PermissionService;
import com.yangxj96.spectra.service.user.service.RoleService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private AuthorityService authorityService;

    @Resource
    private PermissionMapstruct mapstruct;

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
        result.setRecords(mapstruct.roleToVOs(db.getRecords()));
        return result;
    }

    @Override
    public List<RoleVO> listRole() {
        var wrapper = new LambdaQueryWrapper<Role>();
        wrapper.eq(Role::getState, Boolean.TRUE);
        return mapstruct.roleToVOs(roleService.list(wrapper));
    }

    @Override
    public List<AuthorityTreeVO> authorityTree() {
        List<Authority> authorities = authorityService.list();
        List<AuthorityTreeVO> vos = mapstruct.authorityToTreeVos(authorities);
        return new TreeBuilder<>(vos).buildTree(Common.PID);
    }
}
