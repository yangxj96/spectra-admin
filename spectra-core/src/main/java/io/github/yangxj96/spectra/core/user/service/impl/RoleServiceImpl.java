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
import io.github.yangxj96.spectra.common.base.BaseEntity;
import io.github.yangxj96.spectra.common.base.BaseServiceImpl;
import io.github.yangxj96.spectra.core.system.javabean.entity.Menu;
import io.github.yangxj96.spectra.core.system.service.MenuService;
import io.github.yangxj96.spectra.core.user.javabean.entity.*;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleAuthorityFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleMenuFrom;
import io.github.yangxj96.spectra.core.user.mapper.RelRoleAuthorityMapper;
import io.github.yangxj96.spectra.core.user.mapper.RelRoleMenuMapper;
import io.github.yangxj96.spectra.core.user.mapper.RelUserRoleMapper;
import io.github.yangxj96.spectra.core.user.mapper.RoleMapper;
import io.github.yangxj96.spectra.core.user.service.AuthorityService;
import io.github.yangxj96.spectra.core.user.service.RoleService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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
    private AuthorityService authorityService;

    @Resource
    private MenuService menuService;

    @Resource
    private RelRoleAuthorityMapper roleAuthorityMapper;

    @Resource
    private RelRoleMenuMapper roleMenuMapper;

    @Resource
    private RelUserRoleMapper userRoleMapper;

    @Override
    public List<Role> getByUserId(Long uid) {
        List<RelUserRole> relUserRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<RelUserRole>()
                        .eq(RelUserRole::getUserId, uid)
        );
        if (relUserRoles == null || CollectionUtils.isEmpty(relUserRoles)) {
            return new ArrayList<>();
        }
        List<Long> roleIds = relUserRoles.stream().map(RelUserRole::getRoleId).toList();
        return this.baseMapper.selectList(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getState, Boolean.TRUE)
                        .in(BaseEntity::getId, roleIds)
        );
    }

    @Override
    public List<Long> getRoleIdsByUserId(Long uid) {
        List<Role> roles = this.getByUserId(uid);
        if (roles == null || CollectionUtils.isEmpty(roles)) {
            return new ArrayList<>();
        }
        return roles.stream().map(BaseEntity::getId).toList();
    }

    @Override
    @Transactional
    public int insertUserRel(Long uid, List<Long> roleIds) {
        var coll = new ArrayList<RelUserRole>();
        for (Long roleId : roleIds) {
            coll.add(RelUserRole.builder().userId(uid).roleId(roleId).build());
        }
        return Math.toIntExact(userRoleMapper.insert(coll).size());
    }

    @Override
    @Transactional
    public int removeUserRel(Long uid) {
        var wrapper = new LambdaQueryWrapper<RelUserRole>().eq(RelUserRole::getUserId, uid);
        return userRoleMapper.delete(wrapper);
    }

    @Override
    @Transactional
    public int removeUserRel(Long uid, List<Long> roleIds) {
        var wrapper = new LambdaQueryWrapper<RelUserRole>()
                .eq(RelUserRole::getUserId, uid)
                .in(RelUserRole::getRoleId, roleIds);
        return userRoleMapper.delete(wrapper);
    }

    @Override
    public List<Authority> getAuthorityById(List<Long> ids) {
        List<RelRoleAuthority> relRoleAuthorities = roleAuthorityMapper.selectList(
                new LambdaQueryWrapper<RelRoleAuthority>()
                        .in(RelRoleAuthority::getRoleId, ids)
        );
        if (relRoleAuthorities == null || CollectionUtils.isEmpty(relRoleAuthorities)) {
            return new ArrayList<>();
        }
        List<Long> authorityIds = relRoleAuthorities.stream().map(RelRoleAuthority::getAuthorityId).toList();
        return authorityService.list(
                new LambdaQueryWrapper<Authority>()
                        .in(BaseEntity::getId, authorityIds)
        );
    }

    @Override
    public List<Authority> getAuthorityById(Long id) {
        List<RelRoleAuthority> relRoleAuthorities = roleAuthorityMapper.selectList(
                new LambdaQueryWrapper<RelRoleAuthority>()
                        .eq(RelRoleAuthority::getRoleId, id)
        );
        return authorityService.getByRelRoleAuthority(relRoleAuthorities);
    }

    @Override
    public List<Menu> getMenuById(Long id) {
        List<RelRoleMenu> relRoleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<RelRoleMenu>()
                        .eq(RelRoleMenu::getRoleId, id)
        );
        return menuService.getByRelRoleMenu(relRoleMenus);
    }

    @Override
    @Transactional
    public void saveAuthorityById(Long id, RoleAuthorityFrom from) {
        var currentIds = this.getAuthorityById(id).stream().map(BaseEntity::getId).collect(Collectors.toSet());
        var targetIds = new HashSet<>(from.getAuthorityIds());
        // 计算删除且删除
        var removeIds = new HashSet<>(currentIds);
        removeIds.removeAll(targetIds); // current - target = 删除
        if (CollectionUtils.isNotEmpty(removeIds)) {
            var wrapper = new LambdaQueryWrapper<RelRoleAuthority>()
                    .eq(RelRoleAuthority::getRoleId, id)
                    .in(RelRoleAuthority::getAuthorityId, removeIds);
            roleAuthorityMapper.delete(wrapper);
        }
        // 计算新增且插入
        var addIds = new HashSet<>(targetIds);
        addIds.removeAll(currentIds); // target - current = 新增
        if (CollectionUtils.isNotEmpty(addIds)) {
            List<RelRoleAuthority> newRelations = addIds.stream()
                    .map(addId -> RelRoleAuthority.builder()
                            .roleId(id)
                            .authorityId(addId)
                            .build())
                    .collect(Collectors.toList());
            roleAuthorityMapper.insert(newRelations);
        }
    }

    @Override
    @Transactional
    public void saveMenuById(Long id, RoleMenuFrom from) {
        var currentIds = this.getMenuById(id).stream().map(BaseEntity::getId).collect(Collectors.toSet());
        var targetIds = new HashSet<>(from.getMenuIds());
        // 计算删除且删除
        var removeIds = new HashSet<>(currentIds);
        removeIds.removeAll(targetIds); // current - target = 删除
        if (CollectionUtils.isNotEmpty(removeIds)) {
            var wrapper = new LambdaQueryWrapper<RelRoleMenu>()
                    .eq(RelRoleMenu::getRoleId, id)
                    .in(RelRoleMenu::getRoleId, removeIds);
            roleMenuMapper.delete(wrapper);
        }
        // 计算新增且插入
        var addIds = new HashSet<>(targetIds);
        addIds.removeAll(currentIds);  // target - current = 新增
        if (CollectionUtils.isNotEmpty(addIds)) {
            List<RelRoleMenu> newMenu = addIds.stream()
                    .map(addId -> RelRoleMenu.builder()
                            .roleId(id)
                            .menuId(addId)
                            .build())
                    .collect(Collectors.toList());
            roleMenuMapper.insert(newMenu);
        }
    }

    @Override
    @Transactional
    public void clearRoleRel(Long id) {
        // 删除角色关联的权限
        roleAuthorityMapper.delete(new LambdaQueryWrapper<RelRoleAuthority>().eq(RelRoleAuthority::getRoleId, id));
        // 删除角色关联的菜单
        roleMenuMapper.delete(new LambdaQueryWrapper<RelRoleMenu>().eq(RelRoleMenu::getRoleId, id));
        // 删除用户关联的角色
        userRoleMapper.delete(new LambdaQueryWrapper<RelUserRole>().eq(RelUserRole::getRoleId, id));
    }
}
