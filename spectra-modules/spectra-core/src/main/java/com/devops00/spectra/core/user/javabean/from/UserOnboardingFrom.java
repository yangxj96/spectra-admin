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
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentsChangeFrom;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.validation.groups.Default;
import lombok.Data;

/**
 * 用户新增/编辑与多角色授权的一次性提交请求。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Data
public class UserOnboardingFrom {

    /**
     * 用户基本信息。
     */
    @NotNull(message = "用户基本信息不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    @Valid
    private UserSaveFrom user;

    /**
     * 用户保留、新增、修改及移除的角色授权配置。
     */
    @NotNull(message = "用户授权配置不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    @Valid
    @ConvertGroup.List({
            @ConvertGroup(from = Verify.Insert.class, to = Default.class),
            @ConvertGroup(from = Verify.Update.class, to = Default.class)
    })
    private AuthorizationAssignmentsChangeFrom authorization;
}
