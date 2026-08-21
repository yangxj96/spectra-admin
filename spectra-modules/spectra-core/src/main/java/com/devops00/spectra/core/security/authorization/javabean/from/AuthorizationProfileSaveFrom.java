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

import com.devops00.spectra.common.base.Verify;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * 授权方案创建与修改入参。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
@Data
public class AuthorizationProfileSaveFrom {

    @Null(message = "新增不能指定授权方案 ID", groups = Verify.Insert.class)
    @NotNull(message = "授权方案 ID 不能为空", groups = Verify.Update.class)
    private UUID id;

    @NotBlank(message = "授权方案编码不能为空")
    @Pattern(regexp = "^PROFILE_[A-Z0-9_]+$", message = "授权方案编码格式必须为 PROFILE_*")
    private String code;

    @NotBlank(message = "授权方案名称不能为空")
    @Size(max = 120, message = "授权方案名称不能超过 120 个字符")
    private String name;

    @Size(max = 500, message = "授权方案说明不能超过 500 个字符")
    private String description;

    @NotNull(message = "授权方案版本不能为空", groups = Verify.Update.class)
    @Null(message = "新增不能指定授权方案版本", groups = Verify.Insert.class)
    private Long expectedVersion;

    @NotEmpty(message = "授权方案至少需要一个 Role")
    @Valid
    private List<AuthorizationProfileAssignmentFrom> assignments;
}
