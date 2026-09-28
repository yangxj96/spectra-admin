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

import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 部门直属成员的分页查询条件。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/28
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DepartmentMemberPageFrom extends PageFrom {

    /** 部门 ID。 */
    @NotNull
    private UUID departmentId;

    /** 用户名或姓名关键字。 */
    @Size(max = 120)
    private String keyword;
}
