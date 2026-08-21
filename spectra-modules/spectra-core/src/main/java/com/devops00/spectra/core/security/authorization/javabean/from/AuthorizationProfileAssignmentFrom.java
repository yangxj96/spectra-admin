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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.List;

/**
 * 授权方案中的一个 Role 配置。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
@Data
public class AuthorizationProfileAssignmentFrom {

    @NotBlank(message = "授权方案 Role 编码不能为空")
    private String roleCode;

    @NotNull(message = "授权方案 Role version 不能为空")
    @PositiveOrZero(message = "授权方案 Role version 不能为负数")
    private Long roleVersion;

    @NotEmpty(message = "授权方案至少需要一个 Permission Boundary")
    @Valid
    private List<AuthorizationProfileBoundaryFrom> boundaries;
}
