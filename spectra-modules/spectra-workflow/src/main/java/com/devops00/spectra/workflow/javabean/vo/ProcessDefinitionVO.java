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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/// 流程定义响应VO
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/11 00:00
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDefinitionVO {

    /// 流程ID
    private String id;

    /// 流程key
    private String key;

    /// 流程名称
    private String name;

    /// 流程版本
    private Integer version;

    /// 部署ID
    private String deploymentId;

    /// 资源名称
    private String resourceName;

    /// 是否挂起
    private Boolean suspended;

    /// 流程描述
    private String description;

    /// 流程分类
    private String category;

    /// 部署时间
    private String deploymentTime;
}
