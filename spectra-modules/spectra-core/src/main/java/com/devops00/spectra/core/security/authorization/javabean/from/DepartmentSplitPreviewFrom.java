/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.core.security.authorization.javabean.from;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * 部门部分拆分 Preview 请求。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/28
 */
@Data
public class DepartmentSplitPreviewFrom {

    @NotNull
    private UUID sourceDepartmentId;

    @NotEmpty
    private List<@NotNull UUID> userIds;

    @Valid
    @NotNull
    private DepartmentRestructureDepartmentFrom department;

    @NotNull
    private Long expectedOrganizationVersion;
}
