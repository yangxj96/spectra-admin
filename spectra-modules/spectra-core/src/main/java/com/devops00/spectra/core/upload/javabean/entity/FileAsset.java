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
import com.devops00.spectra.core.upload.javabean.constant.FileAssetStatus;
import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/**
 * 已完成上传并可供业务引用的文件资产元数据；文件内容由对象存储管理。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "file_asset", schema = "spectra_core")
public class FileAsset extends BaseEntity {

    /**
     * 该文件资产对应的文件类型策略 ID。
     */
    @TableField("file_type_id")
    private UUID fileTypeId;

    /**
     * 用户上传时提供的原始文件名。
     */
    @TableField("original_name")
    private String originalName;

    /**
     * 文件内容的 SHA-256 摘要，用于完整性校验。
     */
    @TableField("content_sha256")
    private String contentSha256;

    /**
     * 文件内容大小，单位为字节。
     */
    @TableField("size")
    private Long size;

    /**
     * 文件内容的媒体类型。
     */
    @TableField("content_type")
    private String contentType;

    /**
     * 保存文件内容的对象存储提供方。
     */
    @TableField("storage_provider")
    private StorageProviderType storageProvider;

    /**
     * 对象存储中的存储桶或容器名称。
     */
    @TableField("storage_container")
    private String storageContainer;

    /**
     * 文件内容在对象存储中的对象键。
     */
    @TableField("storage_key")
    private String storageKey;

    /**
     * 文件资产当前的处理或可用状态。
     */
    @TableField("status")
    private FileAssetStatus status;

    /**
     * 文件资产完成上传并可用的时间。
     */
    @TableField("completed_at")
    private Instant completedAt;

    /**
     * 文件资产被判定为无业务引用的时间。
     */
    @TableField("orphaned_at")
    private Instant orphanedAt;

    /**
     * 针对孤立资产已执行的清理尝试次数。
     */
    @TableField("cleanup_attempts")
    private Integer cleanupAttempts;

    /**
     * 下一次计划清理该孤立资产的时间。
     */
    @TableField("next_cleanup_at")
    private Instant nextCleanupAt;
}
