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

package io.github.yangxj96.spectra.core.auth.mapper;

import io.github.yangxj96.spectra.core.system.javabean.entity.Menu;
import io.github.yangxj96.spectra.core.user.javabean.entity.Role;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 安全mapper
 */
public interface SecurityMapper {

    /**
     * 根据用户ID获取用户角色
     *
     * @param uid 用户ID
     * @return 角色列表
     */
    List<Role> getRolesByUserId(@Param("uid") long uid);

    /**
     * 根据用户ID获取用户菜单
     *
     * @param uid 用户ID
     * @return 菜单列表
     */
    List<Menu> getMenusByUserId(@Param("uid") long uid);
}
