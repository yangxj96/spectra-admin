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

package com.devops00.spectra.core.user.imports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;

/**
 * 用户批量导入任务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_import_task", schema = "spectra_core")
public class UserImportTask extends BaseEntity {

    @TableField("operator_id")
    private UUID operatorId;

    @TableField("idempotency_key")
    private String idempotencyKey;

    @TableField("file_name")
    private String fileName;

    @TableField("file_hash")
    private String fileHash;

    @TableField("skip_existing")
    private boolean skipExisting;

    private String status;

    @TableField("request_hash")
    private String requestHash;

    @TableField("profile_version_hash")
    private String profileVersionHash;

    @TableField("preview_token_hash")
    private String previewTokenHash;

    @TableField("preview_expires_at")
    private Instant previewExpiresAt;

    @TableField("expires_at")
    private Instant expiresAt;

    @TableField("total_rows")
    private int totalRows;

    @TableField("valid_rows")
    private int validRows;

    @TableField("error_rows")
    private int errorRows;

    @TableField("skipped_rows")
    private int skippedRows;

    @TableField("applied_rows")
    private int appliedRows;

    @TableField("completed_rows")
    private int completedRows;

    @TableField("assignment_count")
    private int assignmentCount;

    @TableField("access_boundary_count")
    private int accessBoundaryCount;

    @TableField("grant_boundary_count")
    private int grantBoundaryCount;

    @TableField("preview_consumed_at")
    private Instant previewConsumedAt;
}
