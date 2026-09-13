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

package com.devops00.spectra.core.user.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.framework.persistence.base.BaseEntity;
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

    /**
     * 发起该批量导入任务的操作人用户 ID。
     */
    @TableField("operator_id")
    private UUID operatorId;

    /**
     * 用于避免重复创建同一导入请求的幂等键。
     */
    @TableField("idempotency_key")
    private String idempotencyKey;

    /**
     * 本次导入文件的名称。
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 本次导入文件内容的摘要，用于识别文件版本。
     */
    @TableField("file_hash")
    private String fileHash;

    /**
     * 是否跳过已存在的用户记录。
     */
    @TableField("skip_existing")
    private boolean skipExisting;

    /**
     * 批量导入任务当前的生命周期状态。
     */
    private String status;

    /**
     * 导入请求参数的摘要，用于校验请求一致性。
     */
    @TableField("request_hash")
    private String requestHash;

    /**
     * 本次预览所依据的导入字段配置版本摘要。
     */
    @TableField("profile_version_hash")
    private String profileVersionHash;

    /**
     * 预览确认令牌的摘要，不保存令牌明文。
     */
    @TableField("preview_token_hash")
    private String previewTokenHash;

    /**
     * 本次导入预览结果失效的时间。
     */
    @TableField("preview_expires_at")
    private Instant previewExpiresAt;

    /**
     * 任务及其暂存数据的到期时间。
     */
    @TableField("expires_at")
    private Instant expiresAt;

    /**
     * 导入文件中读取到的数据行总数。
     */
    @TableField("total_rows")
    private int totalRows;

    /**
     * 通过预览校验且可执行的数据行数。
     */
    @TableField("valid_rows")
    private int validRows;

    /**
     * 预览校验失败的数据行数。
     */
    @TableField("error_rows")
    private int errorRows;

    /**
     * 按导入策略跳过的数据行数。
     */
    @TableField("skipped_rows")
    private int skippedRows;

    /**
     * 已成功应用到用户数据的数据行数。
     */
    @TableField("applied_rows")
    private int appliedRows;

    /**
     * 已完成处理的数据行数。
     */
    @TableField("completed_rows")
    private int completedRows;

    /**
     * 预览涉及的角色分配记录数。
     */
    @TableField("assignment_count")
    private int assignmentCount;

    /**
     * 预览涉及的权限访问边界记录数。
     */
    @TableField("access_boundary_count")
    private int accessBoundaryCount;

    /**
     * 预览涉及的权限授予边界记录数。
     */
    @TableField("grant_boundary_count")
    private int grantBoundaryCount;

    /**
     * 该导入预览令牌被成功消费的时间。
     */
    @TableField("preview_consumed_at")
    private Instant previewConsumedAt;
}
