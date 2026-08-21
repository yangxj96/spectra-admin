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

import com.devops00.spectra.security.base.authorization.ScopeMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 授权方案中的数据范围。部门使用稳定业务编码，不使用数据库 UUID。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
@Data
public class AuthorizationProfileScopeFrom {

    @NotNull(message = "授权方案 Scope 模式不能为空")
    private ScopeMode mode;

    @Size(max = 80, message = "授权方案资源编码不能超过 80 个字符")
    private String resourceCode;

    private List<@Size(max = 80, message = "部门编码不能超过 80 个字符") String> departmentCodes;

    private boolean includeDescendants;
}
