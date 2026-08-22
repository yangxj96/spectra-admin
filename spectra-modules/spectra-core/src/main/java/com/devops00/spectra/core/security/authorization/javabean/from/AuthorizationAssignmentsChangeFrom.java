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
import lombok.Data;

import java.util.List;

/**
 * 用户多角色授权一次性变更请求。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Data
public class AuthorizationAssignmentsChangeFrom {

    /**
     * 提交后保留或新增的角色授权，至少保留一个角色。
     */
    @NotEmpty(message = "至少保留一个角色授权")
    @Valid
    private List<AuthorizationAssignmentChangeFrom> assignments;

    /**
     * 本次编辑中移除的已有角色授权。
     */
    @Valid
    private List<AuthorizationAssignmentRemovalFrom> removedAssignments;
}
