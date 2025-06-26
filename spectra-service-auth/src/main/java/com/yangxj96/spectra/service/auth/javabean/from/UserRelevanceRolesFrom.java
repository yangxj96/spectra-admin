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

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <p>
 * 用户关联角色
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRelevanceRolesFrom {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "需要关联的角色ID列表不能为空")
    private List<Long> roleIds;
}
