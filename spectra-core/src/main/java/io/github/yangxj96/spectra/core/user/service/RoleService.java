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

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.yangxj96.spectra.common.base.BaseService;
import io.github.yangxj96.spectra.common.base.javabean.from.PageFrom;
import io.github.yangxj96.spectra.core.system.javabean.vo.MenuVO;
import io.github.yangxj96.spectra.core.user.javabean.entity.Authority;
import io.github.yangxj96.spectra.core.user.javabean.entity.Role;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleAuthorityFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleMenuFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RolePageFrom;
import io.github.yangxj96.spectra.core.user.javabean.vo.AuthorityVO;
import io.github.yangxj96.spectra.core.user.javabean.vo.RoleVO;

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
     * 清理这个角色的所有关联关系
     *
     * @param id 角色ID
     */
    void clearRoleRel(Long id);

    /**
     * 创建角色
     *
     * @param params 实体入参
     */
    void created(RoleFrom params);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void delete(long id);

    /**
     * 修改角色
     *
     * @param params 实体入参
     */
    void modify(RoleFrom params);

    /**
     * 分页查询角色信息
     *
     * @param page   分页信息
     * @param params 查询参数
     */
    IPage<RoleVO> page(PageFrom page, RolePageFrom params);

    /**
     * 查询所有角色列表
     *
     * @return 角色列表
     */
    List<RoleVO> all();

    /**
     * 根据角色获取角色关联的权限信息
     *
     * @param roleId 角色id
     * @return 权限信息
     */
    List<AuthorityVO> getRoleRelevanceAuthorityByRoleId(long roleId);

    /**
     * 根据角色获取角色关联的菜单信息
     *
     * @param roleId 角色id
     * @return 菜单信息
     */
    List<MenuVO> getRoleRelevanceMenuByRoleId(long roleId);

    /**
     * 保存角色关联权限的信息
     *
     * @param roleId 角色id
     * @param from   关联信息
     */
    void saveRoleRelevanceAuthorityByRoleId(long roleId, RoleAuthorityFrom from);

    /**
     * 保存角色关联权限的信息
     *
     * @param roleId 角色id
     * @param from   关联信息
     */
    void saveRoleRelevanceMenuByRoleId(long roleId, RoleMenuFrom from);
}
