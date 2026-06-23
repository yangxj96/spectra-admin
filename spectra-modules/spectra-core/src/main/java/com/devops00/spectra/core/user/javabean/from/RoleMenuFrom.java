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

package com.devops00.spectra.core.user.javabean.from;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/// 角色关联菜单入参对象
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/11 00:00
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenuFrom {

    /// 角色ID
    @NotNull(message = "角色ID不能为null")
    private UUID roleId;

    /// 菜单ID列表
    @NotNull(message = "菜单列表不能为null")
    @Size(min = 1, message = "菜单列表至少需要一个权限ID")
    private List<UUID> menuIds;

}
