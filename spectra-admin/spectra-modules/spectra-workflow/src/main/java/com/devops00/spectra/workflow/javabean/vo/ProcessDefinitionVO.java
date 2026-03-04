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

package com.devops00.spectra.workflow.javabean.vo;

import lombok.Data;

import java.io.Serializable;

/// 流程定义响应VO
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-11
@Data
public class ProcessDefinitionVO implements Serializable {

    /**
     * 流程ID
     */
    private String id;

    /**
     * 流程key
     */
    private String key;

    /**
     * 流程名称
     */
    private String name;

    /**
     * 流程版本
     */
    private Integer version;

    /**
     * 部署ID
     */
    private String deploymentId;

    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 是否挂起
     */
    private Boolean suspended;
}
