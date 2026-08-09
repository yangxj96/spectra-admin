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

package com.devops00.spectra.workflow.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/// 工作流-表单版本表
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/17
@Getter
@Setter
@ToString
@TableName(value = "wf_form_version", schema = "spectra_workflow")
public class FormVersion extends BaseEntity {

    /// 关联表单定义ID
    @TableField("form_definition_id")
    private UUID formDefinitionId;

    /// 版本号（同一表单下唯一）
    @TableField("form_version")
    private Integer formVersion;

    /// form-create规则JSON（组件定义）
    @TableField("rule_json")
    private String ruleJson;

    /// form-create配置JSON（表单属性）
    @TableField("options_json")
    private String optionsJson;

    /// form-create getJson()完整输出
    @TableField("form_json")
    private String formJson;
}
