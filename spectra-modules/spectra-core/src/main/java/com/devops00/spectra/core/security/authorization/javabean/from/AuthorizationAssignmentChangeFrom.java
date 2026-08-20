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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * RoleAssignment Preview/Apply 请求。expectedVersion 防止预览与提交之间的并发授权变更。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@Data
public class AuthorizationAssignmentChangeFrom {

    private UUID assignmentId;

    @NotNull(message = "目标 Role 不能为空")
    private UUID roleId;

    @NotNull(message = "目标用户安全版本不能为空")
    @PositiveOrZero(message = "目标用户安全版本不能为负数")
    private Long expectedVersion;

    @NotEmpty(message = "Access Boundary 不能为空")
    @Valid
    private List<AuthorizationBoundaryFrom> boundaries;
}
