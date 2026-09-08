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

/**
 * 菜单service层
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
public interface MenuService extends BaseService<Menu> {

    /**
     * 创建菜单
     *
     * @param params 待创建菜单的名称、路由、父菜单、权限编码和显示状态。
     */
    void created(MenuSaveFrom params);

    /**
     * 修改菜单信息
     *
     * @param params 待修改菜单的唯一标识及需要更新的路由、权限和显示字段。
     */
    void modify(MenuSaveFrom params);

    /**
     * 生成树形菜单
     *
     * @return 返回全部已启用菜单组装的树形菜单；没有可用菜单时按当前实现返回 null，调用方需要先判空。
     */
    @Nullable
    List<MenuTreeVO> tree();

    /**
     * 获取当前用户的授权菜单树
     *
     * @param userId 用于解析授权菜单的用户唯一标识。
     * @return 返回用户当前拥有权限的菜单树；没有授权菜单时返回空列表，不返回 null。
     */
    List<MenuTreeVO> current(UUID userId);

    /**
     * 根据角色ID获取角色关联的菜单
     *
     * @param id 用于查询菜单关联关系的角色唯一标识。
     * @return 返回角色关联的菜单实体列表；角色没有关联菜单时返回空列表，不返回 null。
     */
    List<Menu> getByRelRoleId(UUID id);

    /**
     * 根据ID删除菜单
     *
     * @param id 菜单ID
     */
    void deleteById(UUID id);
}
