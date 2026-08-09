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

package com.devops00.spectra.workflow.javabean.from;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建/更新表单定义请求参数
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/17
 */
@Data
public class FormDefinitionSaveFrom {

    /**
     * 表单名称
     */
    @NotBlank(message = "表单名称不能为空")
    private String name;

    /**
     * 表单编码（可选，后端自动生成）
     */
    private String code;

    /**
     * 描述
     */
    private String description;

    /**
     * form-create规则JSON
     */
    @NotBlank(message = "规则JSON不能为空")
    private String ruleJson;

    /**
     * form-create配置JSON
     */
    @NotBlank(message = "配置JSON不能为空")
    private String optionsJson;

    /**
     * form-create完整输出
     */
    @NotBlank(message = "表单JSON不能为空")
    private String formJson;
}
