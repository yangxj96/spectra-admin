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

/// 表单定义响应VO
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/17
@Data
public class FormDefinitionVO {

    /// 表单定义ID
    private UUID id;

    /// 表单名称
    private String name;

    /// 表单编码
    private String code;

    /// 当前版本号
    private Integer currentVersion;

    /// 是否启用
    private Boolean active;

    /// 描述
    private String description;

    /// form-create规则JSON（仅详情接口返回）
    private String ruleJson;

    /// form-create配置JSON（仅详情接口返回）
    private String optionsJson;

    /// form-create完整输出（仅详情接口返回）
    private String formJson;

    /// 创建人
    private UUID createdBy;

    /// 创建时间
    private Instant createdAt;

    /// 最后修改时间
    private Instant updatedAt;

}
