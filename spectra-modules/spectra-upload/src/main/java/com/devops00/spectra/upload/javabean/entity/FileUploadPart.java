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
import com.devops00.spectra.upload.javabean.constant.UploadPartStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/** 上传分片实体。 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "file_upload_part", schema = "spectra_core")
public class FileUploadPart extends BaseEntity {

    @TableField("upload_session_id")
    private UUID uploadSessionId;

    @TableField("part_number")
    private Integer partNumber;

    @TableField("expected_size")
    private Long expectedSize;

    @TableField("expected_sha256")
    private String expectedSha256;

    @TableField("uploaded_size")
    private Long uploadedSize;

    @TableField("actual_sha256")
    private String actualSha256;

    @TableField("provider_etag")
    private String providerEtag;

    @TableField("status")
    private UploadPartStatus status;

    @TableField("upload_attempt")
    private Integer uploadAttempt;

    @TableField("uploaded_at")
    private Instant uploadedAt;
}
