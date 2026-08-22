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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 角色编辑器最终提交入参。
 * <p>
 * ID 为空时新增角色，否则编辑已有角色；角色基本信息、授权和菜单在同一个事务中提交。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Data
public class RoleEditorSaveFrom {

    /**
     * 角色 ID；新增时为空。
     */
    private UUID id;

    /**
     * 角色名称。
     */
    @NotBlank
    @Size(max = 120)
    private String name;

    /**
     * 角色编码；为空时由后端生成。
     */
    @Size(max = 80)
    @Pattern(regexp = "^$|ROLE_[A-Z0-9_]+$")
    private String code;

    /**
     * 角色备注。
     */
    @Size(max = 500)
    private String remark;

    /**
     * 角色授权并发版本；新增时可以为空，后端使用新角色当前版本。
     */
    private Long expectedVersion;

    /**
     * 角色授权管理等级。
     */
    @NotNull
    @Positive
    private Integer authorityLevel;

    /**
     * 角色可以使用的权限编码。
     */
    @NotNull
    private Set<@NotBlank String> permissionCodes;

    /**
     * 角色可以向下授予的权限编码。
     */
    @NotNull
    private Set<@NotBlank String> grantablePermissionCodes;

    /**
     * 角色菜单 ID 列表。
     */
    @NotNull
    private List<@NotNull UUID> menuIds;
}
