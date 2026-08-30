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

package com.devops00.spectra.upload.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.common.mybatis.PgJsonbNodeTypeHandler;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import tools.jackson.databind.JsonNode;

/** 文件类型策略实体。 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "file_type", schema = "spectra_core", autoResultMap = true)
public class FileType extends BaseEntity {

    @TableField("code")
    private String code;

    @TableField("display_name")
    private String displayName;

    @TableField(value = "allowed_extensions", typeHandler = PgJsonbNodeTypeHandler.class)
    private JsonNode allowedExtensions;

    @TableField(value = "allowed_content_types", typeHandler = PgJsonbNodeTypeHandler.class)
    private JsonNode allowedContentTypes;

    @TableField(value = "magic_rules", typeHandler = PgJsonbNodeTypeHandler.class)
    private JsonNode magicRules;

    @TableField("max_size")
    private Long maxSize;

    @TableField("preview_enabled")
    private Boolean previewEnabled;

    @TableField("download_enabled")
    private Boolean downloadEnabled;

    @TableField("upload_enabled")
    private Boolean uploadEnabled;

    @TableField("dangerous")
    private Boolean dangerous;

    @TableField("enabled")
    private Boolean enabled;
}
