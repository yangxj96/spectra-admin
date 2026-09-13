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

import com.devops00.spectra.common.security.policy.PasswordPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改密码入参
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordFrom {

    /**
     * 旧密码
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(max = PasswordPolicy.MAX_LENGTH, message = "密码长度不能超过 20 位")
    private String newPassword;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String verifyPassword;
}
