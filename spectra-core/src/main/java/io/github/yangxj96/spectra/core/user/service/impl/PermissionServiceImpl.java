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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.yangxj96.spectra.common.base.javabean.from.PageFrom;
import io.github.yangxj96.spectra.common.constant.Common;
import io.github.yangxj96.spectra.common.utils.TreeBuilder;
import io.github.yangxj96.spectra.core.system.javabean.entity.Menu;
import io.github.yangxj96.spectra.core.system.javabean.mapstruct.MenuMapstruct;
import io.github.yangxj96.spectra.core.system.javabean.vo.MenuVO;
import io.github.yangxj96.spectra.core.user.javabean.entity.Authority;
import io.github.yangxj96.spectra.core.user.javabean.entity.Role;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleAuthorityFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleMenuFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RolePageFrom;
import io.github.yangxj96.spectra.core.user.javabean.mapstruct.AuthorityMapstruct;
import io.github.yangxj96.spectra.core.user.javabean.mapstruct.PermissionMapstruct;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Resource
    private AuthorityMapstruct authorityMapstruct;

    @Resource
    private MenuMapstruct menuMapstruct;

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

    @Override
    public List<AuthorityVO> getRoleRelevanceAuthorityByRoleId(long id) {
        List<Authority> authority = roleService.getAuthorityById(id);
        return authorityMapstruct.toVOS(authority);
    }

    @Override
    public List<MenuVO> getRoleRelevanceMenuByRoleId(long id) {
        List<Menu> menus = roleService.getMenuById(id);
        return menuMapstruct.toVOS(menus);
    }

    @Override
    public void saveRoleRelevanceAuthorityByRoleId(long id, RoleAuthorityFrom from) {
        // 权限树过滤
        List<AuthorityTreeVO> authorityTree = authorityTree();
        // 压缩选中权限：全选子节点 → 只保留父节点
        from.setAuthorityIds(compressSelectedAuthorities(authorityTree, from.getAuthorityIds()));
        // 进行保存
        roleService.saveAuthorityById(id, from);
    }

    @Override
    public void saveRoleRelevanceMenuByRoleId(long id, RoleMenuFrom from) {
        roleService.saveMenuById(id, from);
    }

    //////////////////////////// 私有方法区

    /**
     * 处理选中的权限ID列表：如果父节点的所有子节点都被选中，则只保留父节点
     *
     * @param tree        权限树根节点列表
     * @param selectedIds 用户选中的权限ID集合
     * @return 处理后的权限ID集合（去除了被“父节点代表”的子节点）
     */
    public static List<Long> compressSelectedAuthorities(List<AuthorityTreeVO> tree, List<Long> selectedIds) {
        List<Long> result = new ArrayList<>();
        for (AuthorityTreeVO node : tree) {
            Set<Long> nodeResult = new HashSet<>();
            collectCompressedIds(node, selectedIds, nodeResult);
            result.addAll(nodeResult);
        }
        return result;
    }

    /**
     * 递归收集压缩后的权限ID
     *
     * @param node        当前节点
     * @param selectedIds 用户原始选中的ID集合
     * @param result      收集结果
     * @return 当前节点及其子树是否“被完全选中”（用于父级判断）
     */
    private static boolean collectCompressedIds(AuthorityTreeVO node, List<Long> selectedIds, Set<Long> result) {
        if (node == null || node.getId() == null) {
            return false;
        }
        // 叶子节点
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            if (selectedIds.contains(node.getId())) {
                result.add(node.getId());
                return true;
            }
            return false;
        }
        // 非叶子节点：先递归处理所有子节点
        List<AuthorityTreeVO> children = node.getChildren();
        boolean allChildrenSelected = true;
        List<Set<Long>> childResults = new ArrayList<>();
        for (AuthorityTreeVO child : children) {
            Set<Long> childResult = new HashSet<>();
            boolean isSelected = collectCompressedIds(child, selectedIds, childResult);
            childResults.add(childResult);
            if (!isSelected) {
                allChildrenSelected = false;
            }
        }
        // 如果所有子节点都被选中，则只保留当前父节点
        if (allChildrenSelected && selectedIds.contains(node.getId())) {
            result.add(node.getId());
            return true; // 表示本节点也被“逻辑选中”
        }
        // 否则，保留所有子节点的结果
        for (Set<Long> childResult : childResults) {
            result.addAll(childResult);
        }
        // 如果父节点本身也被单独选中（但子节点未全选），也保留父节点
        if (selectedIds.contains(node.getId())) {
            result.add(node.getId());
        }
        return allChildrenSelected && selectedIds.contains(node.getId());
    }


}
