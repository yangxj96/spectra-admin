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

package com.devops00.spectra.core.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.user.javabean.entity.RelRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/// 角色关联菜单中间表mapper
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-11
@Mapper
public interface RelRoleMenuMapper extends BaseMapper<RelRoleMenu> {

    /// 根据角色ID获取角色的菜单关联信息
    ///
    /// @param roleId 角色ID
    /// @return 关联信息
    List<RelRoleMenu> getByRoleId(@Param("roleId") UUID roleId);


}
