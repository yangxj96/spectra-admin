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

package com.yangxj96.spectra.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yangxj96.spectra.service.user.javabean.entity.Role;
import org.apache.ibatis.annotations.Param;

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
     * 删除关联的角色列表
     *
     * @param uid 用户ID
     * @return 删除的条目数
     */
    int removeRelevanceRoles(@Param("userId") Long uid);

    /**
     * 新增关联的角色列表
     *
     * @param id     主键ID
     * @param uid    用户ID
     * @param roleId 角色ID
     * @return 收影响的行数
     */
    int insertRelevanceRole(@Param("id") Long id, @Param("uid") Long uid, @Param("rid") Long roleId);
}
