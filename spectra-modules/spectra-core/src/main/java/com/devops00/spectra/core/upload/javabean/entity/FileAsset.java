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
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.core.upload.javabean.constant.FileAssetStatus;
import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/** 已完成文件资产实体。 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "file_asset", schema = "spectra_core")
public class FileAsset extends BaseEntity {

    @TableField("file_type_id")
    private UUID fileTypeId;

    @TableField("original_name")
    private String originalName;

    @TableField("content_sha256")
    private String contentSha256;

    @TableField("size")
    private Long size;

    @TableField("content_type")
    private String contentType;

    @TableField("storage_provider")
    private StorageProviderType storageProvider;

    @TableField("storage_container")
    private String storageContainer;

    @TableField("storage_key")
    private String storageKey;

    @TableField("status")
    private FileAssetStatus status;

    @TableField("completed_at")
    private Instant completedAt;

    @TableField("orphaned_at")
    private Instant orphanedAt;

    @TableField("cleanup_attempts")
    private Integer cleanupAttempts;

    @TableField("next_cleanup_at")
    private Instant nextCleanupAt;
}
