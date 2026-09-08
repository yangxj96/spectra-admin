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

package com.devops00.spectra.core.user.service;

import com.devops00.spectra.core.system.javabean.vo.MenuVO;
import com.devops00.spectra.core.user.javabean.from.RoleMenuFrom;

import java.util.List;
import java.util.UUID;

/**
 * 关联服务-角色和菜单
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/11/11 00:00
 */
public interface RelRoleMenuService {

    /**
     * 授予角色菜单
     *
     * @param roleId 待更新菜单授权的角色唯一标识。
     * @param from   角色需要拥有的菜单 ID 集合；提交后按集合结果同步关联关系。
     */
    void grant(UUID roleId, RoleMenuFrom from);

    /**
     * 撤销角色菜单
     *
     * @param roleId 待撤销全部菜单授权的角色唯一标识。
     */
    void revoke(UUID roleId);

    /**
     * 获取角色菜单
     *
     * @param roleId 要读取菜单授权的角色唯一标识。
     * @return 返回指定角色已关联的菜单视图列表；角色没有关联菜单时返回空列表，不返回 null。
     */
    List<MenuVO> get(UUID roleId);
}
