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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作流-表单定义表
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/17
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wf_form_definition", schema = "spectra_workflow")
public class FormDefinition extends BaseEntity {

    /**
     * 表单名称
     */
    @TableField("name")
    private String name;

    /**
     * 表单编码（唯一，用于程序引用）
     */
    @TableField("code")
    private String code;

    /**
     * 当前版本号
     */
    @TableField("current_version")
    private Integer currentVersion;

    /**
     * 是否启用
     */
    @TableField("active")
    private Boolean active;

    /**
     * 描述
     */
    @TableField("description")
    private String description;
}
