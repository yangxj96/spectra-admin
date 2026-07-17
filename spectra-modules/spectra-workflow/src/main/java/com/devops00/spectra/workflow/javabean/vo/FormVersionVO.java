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

package com.devops00.spectra.workflow.javabean.vo;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/// 表单版本响应VO
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/17
@Data
public class FormVersionVO {

    /// 版本ID
    private UUID id;

    /// 关联表单定义ID
    private UUID formDefinitionId;

    /// 版本号
    private Integer formVersion;

    /// form-create规则JSON
    private String ruleJson;

    /// form-create配置JSON
    private String optionsJson;

    /// form-create完整输出
    private String formJson;

    /// 创建人
    private UUID createdBy;

    /// 创建时间
    private Instant createdAt;

}
