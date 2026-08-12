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

package com.devops00.spectra.oa.application.javabean.vo;

import lombok.Data;

import java.util.UUID;

/**
 * OA 申请类型响应。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Data
public class ApplicationTypeVO {

    /**
     * 主键 ID。
     */
    private UUID id;

    /**
     * 编码。
     */
    private String code;

    /**
     * 名称。
     */
    private String name;

    /**
     * 表单定义 ID。
     */
    private UUID formDefinitionId;

    /**
     * 流程定义 Key。
     */
    private String processDefinitionKey;

    /**
     * 是否启用。
     */
    private Boolean enabled;

    /**
     * 排序序号。
     */
    private Integer sortOrder;

    /**
     * 描述。
     */
    private String description;
}
