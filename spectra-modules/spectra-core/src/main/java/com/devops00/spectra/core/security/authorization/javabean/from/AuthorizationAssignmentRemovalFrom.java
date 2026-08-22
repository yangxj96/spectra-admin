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

package com.devops00.spectra.core.security.authorization.javabean.from;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.UUID;

/**
 * 撤销用户 RoleAssignment 请求。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Data
public class AuthorizationAssignmentRemovalFrom {

    /**
     * 待撤销的授权实例 ID。
     */
    @NotNull(message = "待移除的角色授权不能为空")
    private UUID assignmentId;

    /**
     * 授权实例的并发版本。
     */
    @NotNull(message = "待移除角色授权的版本不能为空")
    @PositiveOrZero(message = "待移除角色授权的版本不能为负数")
    private Long expectedVersion;
}
