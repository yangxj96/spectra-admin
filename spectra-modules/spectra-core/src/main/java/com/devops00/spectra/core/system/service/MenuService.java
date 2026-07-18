/*
 *  Copyright 2018-2026 yangxj96
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

package com.devops00.spectra.core.system.service;

import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.core.system.javabean.entity.Menu;
import com.devops00.spectra.core.system.javabean.from.MenuSaveFrom;
import com.devops00.spectra.core.system.javabean.vo.MenuTreeVO;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/// 菜单service层
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/14 00:00
public interface MenuService extends BaseService<Menu> {

    /// 创建菜单
    ///
    /// @param params 菜单信息
    void created(MenuSaveFrom params);

    /// 修改菜单信息
    ///
    /// @param params 修改参数
    void modify(MenuSaveFrom params);

    /// 生成树形菜单
    ///
    /// @return 生成的树形菜单
    @Nullable List<MenuTreeVO> tree();

    /// 根据角色ID获取角色关联的菜单
    ///
    /// @param id 角色ID
    /// @return 关联的菜单
    List<Menu> getByRelRoleId(UUID id);

    /// 根据ID删除菜单
    ///
    /// @param id 菜单ID
    void deleteById(UUID id);
}
