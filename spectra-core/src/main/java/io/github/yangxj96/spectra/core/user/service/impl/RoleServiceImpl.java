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

package io.github.yangxj96.spectra.core.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.github.yangxj96.spectra.common.base.BaseEntity;
import io.github.yangxj96.spectra.common.base.BaseServiceImpl;
import io.github.yangxj96.spectra.core.system.javabean.entity.Menu;
import io.github.yangxj96.spectra.core.user.javabean.entity.Authority;
import io.github.yangxj96.spectra.core.user.javabean.entity.RelRoleAuthority;
import io.github.yangxj96.spectra.core.user.javabean.entity.RelRoleMenu;
import io.github.yangxj96.spectra.core.user.javabean.entity.Role;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleAuthorityFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleMenuFrom;
import io.github.yangxj96.spectra.core.user.javabean.vo.AuthorityTreeVO;
import io.github.yangxj96.spectra.core.user.mapper.RelRoleAuthorityMapper;
import io.github.yangxj96.spectra.core.user.mapper.RelRoleMenuMapper;
import io.github.yangxj96.spectra.core.user.mapper.RoleMapper;
import io.github.yangxj96.spectra.core.user.service.RelRoleAuthorityService;
import io.github.yangxj96.spectra.core.user.service.RelRoleMenuService;
import io.github.yangxj96.spectra.core.user.service.RoleService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private RelRoleAuthorityService roleAuthorityService;

    @Resource
    private RelRoleMenuService roleMenuService;

    @Override
    public List<Role> getByUserId(Long uid) {
        return this.baseMapper.getByUserId(uid);
    }

    @Override
    public List<Long> getRoleIdsByUserId(Long uid) {
        return this.baseMapper.getRoleIdsByUserId(uid);
    }

    @Override
    @Transactional
    public int removeRelevanceRoles(Long uid) {
        return this.baseMapper.removeRelevanceRoles(uid, null);
    }

    @Override
    @Transactional
    public int removeRelevanceRoles(Long uid, List<Long> roleIds) {
        return this.baseMapper.removeRelevanceRoles(uid, roleIds);
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

    @Override
    public List<Authority> getAuthorityById(long id) {
        return baseMapper.getAuthorityById(id);
    }

    @Override
    public List<Menu> getMenuById(long id) {
        return baseMapper.getMenuById(id);
    }

    @Override
    @Transactional
    public void saveAuthorityById(long id, RoleAuthorityFrom from) {
        var currentIds = this.getAuthorityById(id).stream().map(BaseEntity::getId).collect(Collectors.toSet());
        var targetIds = new HashSet<>(from.getAuthorityIds());
        // 计算删除且删除
        var removeIds = new HashSet<>(currentIds);
        removeIds.removeAll(targetIds); // current - target = 删除
        if (CollectionUtils.isNotEmpty(removeIds)) {
            var wrapper = new LambdaQueryWrapper<RelRoleAuthority>()
                    .eq(RelRoleAuthority::getRoleId, id)
                    .in(RelRoleAuthority::getAuthorityId, removeIds);
            roleAuthorityService.remove(wrapper);
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
            roleAuthorityService.saveBatch(newRelations);
        }
    }

    @Override
    @Transactional
    public void saveMenuById(long id, RoleMenuFrom from) {
        var currentIds = this.getMenuById(id).stream().map(BaseEntity::getId).collect(Collectors.toSet());
        var targetIds = new HashSet<>(from.getMenuIds());
        // 计算删除且删除
        var removeIds = new HashSet<>(currentIds);
        removeIds.removeAll(targetIds); // current - target = 删除
        if (CollectionUtils.isNotEmpty(removeIds)) {
            var wrapper = new LambdaQueryWrapper<RelRoleMenu>()
                    .eq(RelRoleMenu::getRoleId, id)
                    .in(RelRoleMenu::getRoleId, removeIds);
            roleMenuService.remove(wrapper);
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
            roleMenuService.saveBatch(newMenu);
        }
    }


}
