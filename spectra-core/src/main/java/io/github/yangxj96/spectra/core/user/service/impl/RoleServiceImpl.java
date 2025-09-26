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
import io.github.yangxj96.spectra.common.base.BaseEntity;
import io.github.yangxj96.spectra.common.base.BaseServiceImpl;
import io.github.yangxj96.spectra.common.base.javabean.from.PageFrom;
import io.github.yangxj96.spectra.common.exception.DataNotExistException;
import io.github.yangxj96.spectra.core.system.javabean.vo.MenuVO;
import io.github.yangxj96.spectra.core.user.javabean.converter.RoleConverter;
import io.github.yangxj96.spectra.core.user.javabean.entity.*;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleAuthorityFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleMenuFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RolePageFrom;
import io.github.yangxj96.spectra.core.user.javabean.vo.AuthorityVO;
import io.github.yangxj96.spectra.core.user.javabean.vo.RoleVO;
import io.github.yangxj96.spectra.core.user.mapper.RelRoleAuthorityMapper;
import io.github.yangxj96.spectra.core.user.mapper.RelRoleMenuMapper;
import io.github.yangxj96.spectra.core.user.mapper.RelUserRoleMapper;
import io.github.yangxj96.spectra.core.user.mapper.RoleMapper;
import io.github.yangxj96.spectra.core.user.service.AuthorityService;
import io.github.yangxj96.spectra.core.user.service.RoleService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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
    private RoleConverter roleConverter;

    @Resource
    private RelRoleAuthorityMapper relRoleAuthorityMapper;

    @Resource
    private RelRoleMenuMapper relRoleMenuMapper;

    @Resource
    private RelUserRoleMapper relUserRoleMapper;

    @Override
    public List<Role> getByUserId(Long uid) {
        List<RelUserRole> relUserRoles = relUserRoleMapper.selectList(
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
    @Transactional
    public int insertUserRel(Long uid, List<Long> roleIds) {
        var coll = new ArrayList<RelUserRole>();
        for (Long roleId : roleIds) {
            coll.add(RelUserRole.builder().userId(uid).roleId(roleId).build());
        }
        return Math.toIntExact(relUserRoleMapper.insert(coll).size());
    }

    @Override
    @Transactional
    public int removeUserRel(Long uid) {
        var wrapper = new LambdaQueryWrapper<RelUserRole>().eq(RelUserRole::getUserId, uid);
        return relUserRoleMapper.delete(wrapper);
    }

    @Override
    @Transactional
    public int removeUserRel(Long uid, List<Long> roleIds) {
        var wrapper = new LambdaQueryWrapper<RelUserRole>()
                .eq(RelUserRole::getUserId, uid)
                .in(RelUserRole::getRoleId, roleIds);
        return relUserRoleMapper.delete(wrapper);
    }

    @Override
    public List<Authority> getAuthorityById(List<Long> ids) {
        List<RelRoleAuthority> relRoleAuthorities = relRoleAuthorityMapper.selectList(
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
    @Transactional
    public void clearRoleRel(Long id) {
        // 删除角色关联的权限
        relRoleAuthorityMapper.delete(new LambdaQueryWrapper<RelRoleAuthority>().eq(RelRoleAuthority::getRoleId, id));
        // 删除角色关联的菜单
        relRoleMenuMapper.delete(new LambdaQueryWrapper<RelRoleMenu>().eq(RelRoleMenu::getRoleId, id));
        // 删除用户关联的角色
        relUserRoleMapper.delete(new LambdaQueryWrapper<RelUserRole>().eq(RelUserRole::getRoleId, id));
    }

    @Override
    @Transactional
    public void created(RoleFrom params) {
        Role role = new Role();
        BeanUtils.copyProperties(params, role);
        this.save(role);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Role role = this.getById(id);
        if (role == null) {
            throw new DataNotExistException("角色不存在");
        }
        // 先清理关联的
        this.clearRoleRel(role.getId());
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
    public List<AuthorityVO> getRoleRelevanceAuthorityByRoleId(long roleId) {
        return List.of();
    }

    @Override
    public List<MenuVO> getRoleRelevanceMenuByRoleId(long roleId) {
        return List.of();
    }

    @Override
    @Transactional
    public void saveRoleRelevanceAuthorityByRoleId(long roleId, RoleAuthorityFrom from) {
        var currentIds = relRoleAuthorityMapper.getByRoleId(roleId)
                .stream().map(BaseEntity::getId).collect(Collectors.toSet());

        var targetIds = new HashSet<>(from.getAuthorityIds());
        // 计算删除且删除
        var removeIds = new HashSet<>(currentIds);
        removeIds.removeAll(targetIds); // current - target = 删除
        if (CollectionUtils.isNotEmpty(removeIds)) {
            var wrapper = new LambdaQueryWrapper<RelRoleAuthority>()
                    .eq(RelRoleAuthority::getRoleId, roleId)
                    .in(RelRoleAuthority::getAuthorityId, removeIds);
            relRoleAuthorityMapper.delete(wrapper);
        }
        // 计算新增且插入
        var addIds = new HashSet<>(targetIds);
        addIds.removeAll(currentIds); // target - current = 新增
        if (CollectionUtils.isNotEmpty(addIds)) {
            List<RelRoleAuthority> newRelations = addIds.stream()
                    .map(addId -> RelRoleAuthority.builder()
                            .roleId(roleId)
                            .authorityId(addId)
                            .build())
                    .collect(Collectors.toList());
            relRoleAuthorityMapper.insert(newRelations);
        }
    }

    @Override
    @Transactional
    public void saveRoleRelevanceMenuByRoleId(long roleId, RoleMenuFrom from) {
// 当前角色关联的菜单信息
        var currentIds = relRoleMenuMapper.getByRoleId(roleId)
                .stream().map(RelRoleMenu::getMenuId).collect(Collectors.toSet());

        var targetIds = new HashSet<>(from.getMenuIds());
        // 计算删除且删除
        var removeIds = new HashSet<>(currentIds);
        removeIds.removeAll(targetIds); // current - target = 删除
        if (CollectionUtils.isNotEmpty(removeIds)) {
            var wrapper = new LambdaQueryWrapper<RelRoleMenu>()
                    .eq(RelRoleMenu::getRoleId, roleId)
                    .in(RelRoleMenu::getRoleId, removeIds);
            relRoleMenuMapper.delete(wrapper);
        }
        // 计算新增且插入
        var addIds = new HashSet<>(targetIds);
        addIds.removeAll(currentIds);  // target - current = 新增
        if (CollectionUtils.isNotEmpty(addIds)) {
            List<RelRoleMenu> newMenu = addIds.stream()
                    .map(addId -> RelRoleMenu.builder()
                            .roleId(roleId)
                            .menuId(addId)
                            .build())
                    .collect(Collectors.toList());
            relRoleMenuMapper.insert(newMenu);
        }
    }
}
