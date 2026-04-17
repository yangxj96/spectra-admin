/*
 *  Copyright 2018-2025 yangxj96
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

package com.devops00.spectra.core.javabean.system.from;

import com.devops00.spectra.common.base.Verify;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/// 组织机构入参
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/7/14
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentFrom {

    @NotNull(message = "ID不能为空", groups = Verify.Update.class)
    @Null(message = "新增时不能有ID存在", groups = Verify.Insert.class)
    private UUID id;

    /// 上级ID
    private UUID pid;

    /// 名称
    private String name;

    /// 编码
    @Null(message = "组织机构编码只能自动生成", groups = Verify.Insert.class)
    private String code;

    /// 组织机构类型
    private Short type;

    /// 行政区划ID
    private UUID regionId;

    /// 备注
    private String remark;

}
