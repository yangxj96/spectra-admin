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
import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;
import com.devops00.spectra.core.upload.javabean.constant.TransportMode;
import com.devops00.spectra.core.upload.javabean.constant.UploadSessionStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/**
 * 时间受限的分片上传会话及文件校验状态。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "file_upload_session", schema = "spectra_core")
public class FileUploadSession extends BaseEntity {

    /**
     * 创建该上传会话的用户 ID。
     */
    @TableField("owner_user_id")
    private UUID ownerUserId;

    /**
     * 会话关联文件的原始文件名。
     */
    @TableField("original_name")
    private String originalName;

    /**
     * 客户端声明的文件媒体类型。
     */
    @TableField("declared_content_type")
    private String declaredContentType;

    /**
     * 待上传文件的预期总大小，单位为字节。
     */
    @TableField("size")
    private Long size;

    /**
     * 待上传文件内容的预期 SHA-256 摘要。
     */
    @TableField("content_sha256")
    private String contentSha256;

    /**
     * 该会话使用的分片大小，单位为字节。
     */
    @TableField("chunk_size")
    private Long chunkSize;

    /**
     * 该会话预计包含的分片总数。
     */
    @TableField("total_parts")
    private Integer totalParts;

    /**
     * 接收该会话文件的对象存储提供方。
     */
    @TableField("storage_provider")
    private StorageProviderType storageProvider;

    /**
     * 该会话使用的上传传输模式。
     */
    @TableField("transport_mode")
    private TransportMode transportMode;

    /**
     * 会话目标对象存储的桶或容器名称。
     */
    @TableField("storage_container")
    private String storageContainer;

    /**
     * 临时上传内容在对象存储中的对象键。
     */
    @TableField("staging_key")
    private String stagingKey;

    /**
     * 对象存储提供方分配的分片上传任务 ID。
     */
    @TableField("provider_upload_id")
    private String providerUploadId;

    /**
     * 该会话完成后生成的文件资产 ID。
     */
    @TableField("file_asset_id")
    private UUID fileAssetId;

    /**
     * 上传会话当前的生命周期状态。
     */
    @TableField("status")
    private UploadSessionStatus status;

    /**
     * 上传会话失效的时间。
     */
    @TableField("expires_at")
    private Instant expiresAt;

    /**
     * 该会话最近一次收到有效上传活动的时间。
     */
    @TableField("last_activity_at")
    private Instant lastActivityAt;

    /**
     * 会话文件完成上传和组装的时间。
     */
    @TableField("completed_at")
    private Instant completedAt;

    /**
     * 服务端开始校验整个文件的时间。
     */
    @TableField("verify_started_at")
    private Instant verifyStartedAt;

    /**
     * 服务端完成整个文件校验的时间。
     */
    @TableField("verify_finished_at")
    private Instant verifyFinishedAt;

    /**
     * 服务端已完成校验的文件字节数。
     */
    @TableField("verify_processed_bytes")
    private Long verifyProcessedBytes;

    /**
     * 本次文件校验需要处理的总字节数。
     */
    @TableField("verify_total_bytes")
    private Long verifyTotalBytes;

    /**
     * 上传或校验失败时记录的稳定错误代码。
     */
    @TableField("failure_code")
    private String failureCode;

    /**
     * 针对过期或失败会话已执行的清理尝试次数。
     */
    @TableField("cleanup_attempts")
    private Integer cleanupAttempts;

    /**
     * 下一次计划清理该会话临时数据的时间。
     */
    @TableField("next_cleanup_at")
    private Instant nextCleanupAt;
}
