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

package com.devops00.spectra.notification.javabean.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.common.mybatis.PgJsonbTypeHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 受控发送短时 Preview 快照；过期后由通知清理任务物理删除。
 */
@Getter
@Setter
@ToString
@TableName(value = "ntf_send_preview", schema = "spectra_notification", autoResultMap = true)
public class NotificationSendPreviewEntity extends BaseEntity {

    /**
     * 操作人 ID。
     */
    @TableField("operator_user_id")
    private UUID operatorUserId;

    /**
     * Preview 请求摘要。
     */
    @TableField("request_hash")
    private String requestHash;

    /**
     * Preview token 的 SHA-256 摘要。
     */
    @TableField("preview_token_hash")
    private String previewTokenHash;

    /**
     * 当前数据范围/地址状态摘要，用于 Apply 时拒绝静默变化。
     */
    @TableField("resolution_hash")
    private String resolutionHash;

    /**
     * 非敏感的 Preview 请求快照，不保存地址、Secret 或敏感正文。
     */
    @TableField(value = "request_snapshot", typeHandler = PgJsonbTypeHandler.class, updateStrategy = FieldStrategy.ALWAYS)
    private Map<String, Object> requestSnapshot;

    /**
     * Preview 过期时间。
     */
    @TableField("expires_at")
    private Instant expiresAt;

    /**
     * Apply 消费时间。
     */
    @TableField("consumed_at")
    private Instant consumedAt;

    /**
     * Apply 生成的逻辑通知请求 ID。
     */
    @TableField("applied_request_id")
    private UUID appliedRequestId;

    /**
     * PREVIEWED、APPLYING、APPLIED 或 EXPIRED。
     */
    @TableField("status")
    private String status;
}
