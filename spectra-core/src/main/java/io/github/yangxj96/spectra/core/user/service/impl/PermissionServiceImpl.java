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

package io.github.yangxj96.spectra.core.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.yangxj96.spectra.common.base.javabean.from.PageFrom;
import io.github.yangxj96.spectra.common.constant.Common;
import io.github.yangxj96.spectra.common.exception.DataNotExistException;
import io.github.yangxj96.spectra.common.utils.TreeBuilder;
import io.github.yangxj96.spectra.common.utils.TreeUtils;
import io.github.yangxj96.spectra.core.system.javabean.converter.MenuConverter;
import io.github.yangxj96.spectra.core.system.javabean.entity.Menu;
import io.github.yangxj96.spectra.core.system.javabean.vo.MenuVO;
import io.github.yangxj96.spectra.core.system.service.MenuService;
import io.github.yangxj96.spectra.core.user.javabean.converter.AuthorityConverter;
import io.github.yangxj96.spectra.core.user.javabean.converter.PermissionConverter;
import io.github.yangxj96.spectra.core.user.javabean.entity.Authority;
import io.github.yangxj96.spectra.core.user.javabean.entity.Role;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleAuthorityFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleMenuFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RolePageFrom;
import io.github.yangxj96.spectra.core.user.javabean.vo.AuthorityTreeVO;
import io.github.yangxj96.spectra.core.user.javabean.vo.AuthorityVO;
import io.github.yangxj96.spectra.core.user.javabean.vo.RoleVO;
import io.github.yangxj96.spectra.core.user.service.AuthorityService;
import io.github.yangxj96.spectra.core.user.service.PermissionService;
import io.github.yangxj96.spectra.core.user.service.RoleService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
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
    private MenuService menuService;

    @Resource
    private AuthorityService authorityService;

    @Resource
    private PermissionConverter permissionConverter;

    @Resource
    private AuthorityConverter authorityConverter;

    @Resource
    private MenuConverter menuConverter;

    @Override
    @Transactional
    public void createdRole(RoleFrom params) {
        Role role = new Role();
        BeanUtils.copyProperties(params, role);
        roleService.save(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleService.getById(id);
        if (role == null) {
            throw new DataNotExistException("角色不存在");
        }
        // 先清理关联的
        roleService.clearRoleRel(role.getId());
        // 在删除角色
        roleService.removeById(role.getId());
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
        result.setRecords(permissionConverter.roleToVOs(db.getRecords()));
        return result;
    }

    @Override
    public List<RoleVO> listRole() {
        var wrapper = new LambdaQueryWrapper<Role>();
        wrapper.eq(Role::getState, Boolean.TRUE);
        return permissionConverter.roleToVOs(roleService.list(wrapper));
    }

    @Override
    public List<AuthorityTreeVO> authorityTree() {
        List<Authority> authorities = authorityService.list();
        List<AuthorityTreeVO> vos = permissionConverter.authorityToTreeVos(authorities);
        return new TreeBuilder<>(vos).buildTree(Common.PID);
    }

    @Override
    public List<AuthorityVO> getRoleRelevanceAuthorityByRoleId(long id) {
        List<Authority> authority = authorityService.getByRelRoleId(id);
        return authorityConverter.toVOS(authority);
    }

    @Override
    public List<MenuVO> getRoleRelevanceMenuByRoleId(long id) {
        List<Menu> menus = menuService.getByRelRoleId(id);
        return menuConverter.toVOS(menus);
    }

    @Override
    public void saveRoleRelevanceAuthorityByRoleId(long id, RoleAuthorityFrom from) {
        // 权限树过滤
        List<AuthorityTreeVO> authorityTree = authorityTree();
        // 压缩选中权限：全选子节点 → 只保留父节点
        from.setAuthorityIds(
                TreeUtils.compressSelectedNodes(
                        authorityTree,
                        new HashSet<>(from.getAuthorityIds()),
                        AuthorityTreeVO::getId
                ).stream().toList()
        );
        // 进行保存
        roleService.saveAuthorityById(id, from);
    }

    @Override
    public void saveRoleRelevanceMenuByRoleId(long id, RoleMenuFrom from) {
        roleService.saveMenuById(id, from);
    }


}
