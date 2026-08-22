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

import com.devops00.spectra.common.base.Verify;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 角色操作入参
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleFrom {

    /**
     * 角色ID
     */
    @Null(message = "新增不能指定角色ID", groups = Verify.Insert.class)
    @NotNull(message = "角色ID不能为空", groups = Verify.Update.class)
    private UUID id;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    @Size(max = 120, message = "角色名称不能超过120个字符", groups = {Verify.Insert.class, Verify.Update.class})
    private String name;

    /**
     * 稳定角色编码；未提供时由后端生成 ROLE_* 编码。
     */
    @Size(max = 80, message = "角色编码不能超过80个字符", groups = {Verify.Insert.class, Verify.Update.class})
    @Pattern(regexp = "^$|ROLE_[A-Z0-9_]+$", message = "角色编码格式必须为 ROLE_*", groups = {Verify.Insert.class, Verify.Update.class})
    private String code;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注不能超过500个字符", groups = {Verify.Insert.class, Verify.Update.class})
    private String remark;
}
