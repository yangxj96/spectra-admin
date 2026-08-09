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

/**
 * 用户关联角色
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/15 00:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRelevanceRolesFrom {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private UUID userId;

    /**
     * 角色列表
     */
    @NotNull(message = "需要关联的角色ID列表不能为空")
    @Size(min = 1, message = "至少需要关联一个角色")
    private List<UUID> roleIds;
}
