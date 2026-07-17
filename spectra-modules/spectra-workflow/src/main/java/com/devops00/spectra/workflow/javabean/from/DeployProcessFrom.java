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

/// 部署流程定义请求参数
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/17
@Data
public class DeployProcessFrom {

    /// BPMN XML 内容
    @NotBlank(message = "BPMN XML 内容不能为空")
    private String bpmnXml;

    /// 流程名称（可选，为空时从 XML 解析）
    private String name;

    /// 流程 KEY（可选，为空时从 XML 解析）
    private String key;

    /// 流程分类（可选）
    private String category;
}
