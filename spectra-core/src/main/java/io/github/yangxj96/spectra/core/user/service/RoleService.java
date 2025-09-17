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

package io.github.yangxj96.spectra.core.user.service;

import io.github.yangxj96.spectra.common.base.BaseService;
import io.github.yangxj96.spectra.core.system.javabean.entity.Menu;
import io.github.yangxj96.spectra.core.user.javabean.entity.Authority;
import io.github.yangxj96.spectra.core.user.javabean.entity.Role;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleAuthorityFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleMenuFrom;

import java.util.List;

/**
 * 角色service层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public interface RoleService extends BaseService<Role> {

    /**
     * 根据账号ID获取所拥有的角色
     *
     * @param uid 账号ID
     * @return 角色列表
     */
    List<Role> getByUserId(Long uid);

    /**
     * 根据账号ID获取所拥有的角色的ID列表
     *
     * @param uid 账号ID
     * @return 角色ID列表
     */
    List<Long> getRoleIdsByUserId(Long uid);

    /**
     * 新增用户关联关系
     *
     * @param uid     用户ID
     * @param roleIds 角色ID
     * @return 新增的条目数
     */
    int insertUserRel(Long uid, List<Long> roleIds);

    /**
     * 移除用户关联关系
     *
     * @param uid 用户ID
     * @return 删除的条目数
     */
    int removeUserRel(Long uid);

    /**
     * 移除用户关联关系
     *
     * @param uid     用户ID
     * @param roleIds 角色列表
     * @return 删除的条目数
     */
    int removeUserRel(Long uid, List<Long> roleIds);

    /**
     * 根据角色ID获取角色关联的权限列表
     *
     * @param id 角色ID
     * @return 关联的权限列表
     */
    List<Authority> getAuthorityById(List<Long> ids);

    /**
     * 根据角色ID获取角色关联的权限列表
     *
     * @param id 角色ID
     * @return 关联的权限列表
     */
    List<Authority> getAuthorityById(Long id);

    /**
     * 根据角色ID获取角色关联的菜单列表
     *
     * @param id 角色ID
     * @return 关联的菜单列表
     */
    List<Menu> getMenuById(Long id);

    /**
     * 根据角色ID保存角色关联的权限列表
     *
     * @param id   角色ID
     * @param from 入参条件
     */
    void saveAuthorityById(Long id, RoleAuthorityFrom from);

    /**
     * 根据角色ID保存角色关联的菜单列表
     *
     * @param id   角色ID
     * @param from 入参条件
     */
    void saveMenuById(Long id, RoleMenuFrom from);

    /**
     * 清理这个角色的所有关联关系
     *
     * @param id 角色ID
     */
    void clearRoleRel(Long id);
}
