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

package com.devops00.spectra.core.security.secret.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.framework.persistence.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 密钥定义元数据实体。 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_secret_definition", schema = "spectra_security")
public class SecretDefinitionEntity extends BaseEntity {

    /** 后端注册的稳定编码。 */
    @TableField("code")
    private String code;
    /** 页面显示名称。 */
    @TableField("name")
    private String name;
    /** 密钥分类。 */
    @TableField("category")
    private String category;
    /** 明文值类型。 */
    @TableField("value_type")
    private String valueType;
    /** 所属模块。 */
    @TableField("owner_module")
    private String ownerModule;
    /** 用途说明。 */
    @TableField("description")
    private String description;
    /** 是否允许创建新版本。 */
    @TableField("mutable")
    private Boolean mutable;
    /** 是否允许热刷新。 */
    @TableField("hot_reload")
    private Boolean hotReload;
    /** 是否可导出。 */
    @TableField("exportable")
    private Boolean exportable;
}
