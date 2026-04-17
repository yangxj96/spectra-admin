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

package com.devops00.spectra.core.service.user;

import com.devops00.spectra.core.javabean.system.vo.MenuVO;
import com.devops00.spectra.core.javabean.user.from.RoleMenuFrom;

import java.util.List;
import java.util.UUID;

/// 关联服务-角色和菜单
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-11
public interface RelRoleMenuService {

    /// 授予角色菜单
    ///
    /// @param roleId 角色ID
    /// @param from   角色关联菜单信息
    void grant(UUID roleId, RoleMenuFrom from);

    /// 撤销角色菜单
    ///
    /// @param roleId 角色ID
    void revoke(UUID roleId);

    /// 获取角色菜单
    ///
    /// @param roleId 角色ID
    /// @return 菜单列表
    List<MenuVO> get(UUID roleId);

}
