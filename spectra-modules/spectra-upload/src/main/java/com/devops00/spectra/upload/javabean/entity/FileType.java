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
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.upload.javabean.domain.MagicRule;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 文件类型表
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/3/6 15:27
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "file_type", schema = "spectra_upload", autoResultMap = true)
public class FileType extends BaseEntity {

    /**
     * 文件类型名称
     */
    @TableField("name")
    private String name;

    /**
     * 文件后缀（.jpg .png 等）
     */
    @TableField(value = "extension", typeHandler = Jackson3TypeHandler.class)
    private List<String> extension;

    /**
     * MIME 类型
     */
    @TableField(value = "mime", typeHandler = Jackson3TypeHandler.class)
    private List<String> mime;

    /**
     * 文件魔数
     */
    @TableField(value = "magic_rules", typeHandler = Jackson3TypeHandler.class)
    private List<MagicRule> magicRules;

    /**
     * 最大文件大小（bytes）
     */
    @TableField("max_size")
    private Long maxSize;

    /**
     * 是否允许预览
     */
    @TableField("previewable")
    private Boolean previewable;

    /**
     * 是否允许上传
     */
    @TableField("allowed_upload")
    private Boolean allowedUpload;

    /**
     * 是否危险类型
     */
    @TableField("dangerous")
    private Boolean dangerous;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
