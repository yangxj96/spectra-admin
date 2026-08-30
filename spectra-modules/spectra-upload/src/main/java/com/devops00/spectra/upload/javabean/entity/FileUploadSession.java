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
import com.devops00.spectra.upload.javabean.constant.StorageProviderType;
import com.devops00.spectra.upload.javabean.constant.TransportMode;
import com.devops00.spectra.upload.javabean.constant.UploadSessionStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/** 时间受限上传会话实体。 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "file_upload_session", schema = "spectra_core")
public class FileUploadSession extends BaseEntity {

    @TableField("owner_user_id")
    private UUID ownerUserId;

    @TableField("original_name")
    private String originalName;

    @TableField("declared_content_type")
    private String declaredContentType;

    @TableField("size")
    private Long size;

    @TableField("content_sha256")
    private String contentSha256;

    @TableField("chunk_size")
    private Long chunkSize;

    @TableField("total_parts")
    private Integer totalParts;

    @TableField("storage_provider")
    private StorageProviderType storageProvider;

    @TableField("transport_mode")
    private TransportMode transportMode;

    @TableField("storage_container")
    private String storageContainer;

    @TableField("staging_key")
    private String stagingKey;

    @TableField("provider_upload_id")
    private String providerUploadId;

    @TableField("file_asset_id")
    private UUID fileAssetId;

    @TableField("status")
    private UploadSessionStatus status;

    @TableField("expires_at")
    private Instant expiresAt;

    @TableField("last_activity_at")
    private Instant lastActivityAt;

    @TableField("completed_at")
    private Instant completedAt;

    @TableField("verify_started_at")
    private Instant verifyStartedAt;

    @TableField("verify_finished_at")
    private Instant verifyFinishedAt;

    @TableField("verify_processed_bytes")
    private Long verifyProcessedBytes;

    @TableField("verify_total_bytes")
    private Long verifyTotalBytes;

    @TableField("failure_code")
    private String failureCode;

    @TableField("cleanup_attempts")
    private Integer cleanupAttempts;

    @TableField("next_cleanup_at")
    private Instant nextCleanupAt;
}
