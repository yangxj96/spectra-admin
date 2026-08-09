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
import com.devops00.spectra.upload.javabean.constant.UploadType;
import lombok.*;

/**
 * 文件信息实体
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/8 00:03
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "file_info", schema = "spectra_upload")
public class FileInfo extends BaseEntity {

    /**
     * 存储文件名(系统生成)
     */
    @TableField(value = "filename")
    private String filename;

    /**
     * 原始文件名
     */
    @TableField(value = "original_name")
    private String originalName;

    /**
     * 文件类型(MIME)
     */
    @TableField(value = "content_type")
    private String contentType;

    /**
     * 文件大小(字节)
     */
    @TableField(value = "size")
    private Long size;

    /**
     * 文件哈希(MD5/SHA256，用于秒传)
     */
    @TableField(value = "hash")
    private String hash;

    /**
     * 存储类型(LOCAL/S3/OSS)
     */
    @TableField(value = "storage_type")
    private UploadType storageType;

    /**
     * 文件状态(ACTIVE/DELETED)
     */
    @TableField(value = "status")
    private String status;

    /**
     * 引用计数(用于秒传共享文件)
     */
    @TableField(value = "ref_count")
    private Integer refCount;
}
