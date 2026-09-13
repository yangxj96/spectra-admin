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
import com.devops00.spectra.core.upload.javabean.constant.UploadPartStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/**
 * 分片上传会话中的单个文件分片及其校验状态。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "file_upload_part", schema = "spectra_core")
public class FileUploadPart extends BaseEntity {

    /**
     * 该分片所属的上传会话 ID。
     */
    @TableField("upload_session_id")
    private UUID uploadSessionId;

    /**
     * 分片在上传会话中的序号，从 1 开始。
     */
    @TableField("part_number")
    private Integer partNumber;

    /**
     * 客户端声明的该分片预期大小，单位为字节。
     */
    @TableField("expected_size")
    private Long expectedSize;

    /**
     * 客户端声明的该分片预期 SHA-256 摘要。
     */
    @TableField("expected_sha256")
    private String expectedSha256;

    /**
     * 服务端已接收的该分片大小，单位为字节。
     */
    @TableField("uploaded_size")
    private Long uploadedSize;

    /**
     * 服务端计算得到的该分片实际 SHA-256 摘要。
     */
    @TableField("actual_sha256")
    private String actualSha256;

    /**
     * 对象存储提供方返回的该分片 ETag。
     */
    @TableField("provider_etag")
    private String providerEtag;

    /**
     * 该分片当前的上传处理状态。
     */
    @TableField("status")
    private UploadPartStatus status;

    /**
     * 该分片已进行的上传尝试次数。
     */
    @TableField("upload_attempt")
    private Integer uploadAttempt;

    /**
     * 该分片最近一次成功上传的时间。
     */
    @TableField("uploaded_at")
    private Instant uploadedAt;
}
