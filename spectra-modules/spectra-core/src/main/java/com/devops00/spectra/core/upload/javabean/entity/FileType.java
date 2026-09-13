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

package com.devops00.spectra.core.upload.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.framework.persistence.base.BaseEntity;
import com.devops00.spectra.framework.persistence.mybatis.PgJsonbNodeTypeHandler;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import tools.jackson.databind.JsonNode;

/**
 * 文件类型的上传、校验、预览和下载策略。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "file_type", schema = "spectra_core", autoResultMap = true)
public class FileType extends BaseEntity {

    /**
     * 文件类型策略的唯一业务编码。
     */
    @TableField("code")
    private String code;

    /**
     * 文件类型策略的显示名称。
     */
    @TableField("display_name")
    private String displayName;

    /**
     * 允许上传的文件扩展名集合，以 JSONB 格式保存。
     */
    @TableField(value = "allowed_extensions", typeHandler = PgJsonbNodeTypeHandler.class)
    private JsonNode allowedExtensions;

    /**
     * 允许上传的媒体类型集合，以 JSONB 格式保存。
     */
    @TableField(value = "allowed_content_types", typeHandler = PgJsonbNodeTypeHandler.class)
    private JsonNode allowedContentTypes;

    /**
     * 用于识别文件实际格式的文件头规则，以 JSONB 格式保存。
     */
    @TableField(value = "magic_rules", typeHandler = PgJsonbNodeTypeHandler.class)
    private JsonNode magicRules;

    /**
     * 该文件类型允许的最大文件大小，单位为字节。
     */
    @TableField("max_size")
    private Long maxSize;

    /**
     * 是否允许在线预览此类型的文件。
     */
    @TableField("preview_enabled")
    private Boolean previewEnabled;

    /**
     * 是否允许下载此类型的文件。
     */
    @TableField("download_enabled")
    private Boolean downloadEnabled;

    /**
     * 是否允许上传此类型的文件。
     */
    @TableField("upload_enabled")
    private Boolean uploadEnabled;

    /**
     * 是否将此类型标记为高风险文件。
     */
    @TableField("dangerous")
    private Boolean dangerous;

    /**
     * 该文件类型策略当前是否启用。
     */
    @TableField("enabled")
    private Boolean enabled;
}
