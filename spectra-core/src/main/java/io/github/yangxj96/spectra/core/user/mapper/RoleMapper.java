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

package io.github.yangxj96.spectra.core.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.yangxj96.spectra.core.system.javabean.entity.Menu;
import io.github.yangxj96.spectra.core.user.javabean.entity.Authority;
import io.github.yangxj96.spectra.core.user.javabean.entity.Role;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleAuthorityFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleMenuFrom;
import org.apache.ibatis.annotations.Param;

import java.util.HashSet;
import java.util.List;

/**
 * 角色mapper层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户ID获取用户角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getByUserId(@Param("userId") Long userId);

    /**
     * 根据账号ID获取所拥有的角色的ID列表
     *
     * @param uid 账号ID
     * @return 角色ID列表
     */
    List<Long> getRoleIdsByUserId(Long uid);

    /**
     * 删除关联的角色列表
     *
     * @param uid 用户ID
     * @return 删除的条目数
     */
    int removeRelevanceRoles(@Param("userId") Long uid, @Param("rid") List<Long> roleId);

    /**
     * 新增关联的角色列表
     *
     * @param id     主键ID
     * @param uid    用户ID
     * @param roleId 角色ID
     * @return 收影响的行数
     */
    int insertRelevanceRole(@Param("id") Long id, @Param("uid") Long uid, @Param("rid") Long roleId);

    /**
     * 根据角色ID获取角色关联的权限列表
     *
     * @param id 角色ID
     * @return 关联的权限列表
     */
    List<Authority> getAuthorityById(long id);

    /**
     * 根据角色ID获取角色关联的菜单列表
     *
     * @param id 角色ID
     * @return 关联的菜单列表
     */
    List<Menu> getMenuById(long id);
}
