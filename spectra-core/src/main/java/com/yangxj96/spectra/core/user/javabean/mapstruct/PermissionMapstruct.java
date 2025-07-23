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

package com.yangxj96.spectra.core.user.javabean.mapstruct;


import com.yangxj96.spectra.core.user.javabean.entity.Authority;
import com.yangxj96.spectra.core.user.javabean.entity.Role;
import com.yangxj96.spectra.core.user.javabean.vo.AuthorityTreeVO;
import com.yangxj96.spectra.core.user.javabean.vo.RoleVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 角色相关mapstruct
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Mapper(componentModel = "spring")
public interface PermissionMapstruct {

    /**
     * 角色实体到VO
     *
     * @param role 角色实体
     * @return 角色VO
     */
    RoleVO roleToVO(Role role);

    /**
     * 角色实体列表到VO列表
     *
     * @param roles 角色实体列表
     * @return 角色VO列表
     */
    List<RoleVO> roleToVOs(List<Role> roles);

    /**
     * 权限实体到权限树形VO
     *
     * @param authority 权限实体
     * @return 权限树形VO
     */
    @Mapping(target = "children", ignore = true)
    AuthorityTreeVO authorityToTreeVo(Authority authority);

    /**
     * 权限实体列表到权限树形VO列表
     *
     * @param authority 权限实体列表
     * @return 权限树形VO列表
     */
    List<AuthorityTreeVO> authorityToTreeVos(List<Authority> authority);
}
