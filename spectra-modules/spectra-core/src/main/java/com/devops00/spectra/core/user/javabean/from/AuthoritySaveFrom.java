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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/// 权限保存入参
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/1
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthoritySaveFrom {

    /// 权限ID
    @Null(message = "新增不能指定权限ID", groups = Verify.Insert.class)
    @NotNull(message = "权限ID不能为空", groups = Verify.Update.class)
    private UUID id;

    /// 父级权限ID
    private UUID pid;

    /// 权限名称
    @NotBlank(message = "权限名称不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private String name;

    /// 权限编码
    @NotBlank(message = "权限编码不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private String code;
}
