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

package com.yangxj96.spectra.core.user.javabean.from;

import com.yangxj96.spectra.common.base.Verify;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色操作入参
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
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
    private Long id;

    /**
     * 角色名称
     */
    @NotEmpty(message = "用户名不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private String name;

    /**
     * 范围
     */
    private Short scope;

    /**
     * 状态
     */
    private Boolean state;

    /**
     * 备注
     */
    private String remark;

}
