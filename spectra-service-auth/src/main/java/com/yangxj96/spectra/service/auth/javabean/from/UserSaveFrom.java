/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.service.auth.javabean.from;

import com.yangxj96.spectra.common.base.Verify;
import com.yangxj96.spectra.common.enums.UserState;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <p>
 * 用户新增/编辑操作入参
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSaveFrom {

    /**
     * 姓名
     */
    @Null(message = "用户ID不能为空", groups = Verify.Insert.class)
    @NotNull(message = "用户ID不能为空", groups = Verify.Update.class)
    private Long id;

    /**
     * 姓名
     */
    @NotBlank(message = "用户名不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private String name;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确", groups = {Verify.Insert.class, Verify.Update.class})
    @NotEmpty(message = "邮箱不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private String email;

    /**
     * 用户状态
     */
    @NotNull(message = "用户状态不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private UserState state;

    /**
     * 角色ID列表
     */
    @Size(message = "角色ID列表不能为空,最少需要有一个角色", min = 1, groups = {Verify.Insert.class, Verify.Update.class})
    private List<Long> roleIds;
}
