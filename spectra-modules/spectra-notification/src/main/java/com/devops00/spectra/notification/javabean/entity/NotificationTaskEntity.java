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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 接收人×渠道通知任务实体。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ntf_task", schema = "spectra_notification", autoResultMap = true)
public class NotificationTaskEntity extends BaseEntity {

    /**
     * 所属逻辑通知请求 ID。
     */
    @TableField("notification_request_id")
    private UUID notificationRequestId;
    /**
     * 投递渠道。
     */
    @TableField("channel")
    private String channel;
    /**
     * 接收用户 ID；直接地址投递时为空。
     */
    @TableField("receiver_user_id")
    private UUID receiverUserId;
    /**
     * 接收人稳定哈希，用于任务幂等。
     */
    @TableField("recipient_key_hash")
    private String recipientKeyHash;
    /**
     * 脱敏后的接收地址。
     */
    @TableField("recipient_masked")
    private String recipientMasked;
    /**
     * 加密后的接收地址。
     */
    @TableField("recipient_ciphertext")
    private String recipientCiphertext;
    /**
     * 锁定的模板版本 ID。
     */
    @TableField("template_id")
    private UUID templateId;
    /**
     * 锁定的模板业务版本号。
     */
    @TableField("template_version_no")
    private Integer templateVersionNo;
    /**
     * 锁定的模板内容摘要。
     */
    @TableField("template_version_digest")
    private String templateVersionDigest;
    /**
     * 通知用途。
     */
    @TableField("purpose")
    private String purpose;
    /**
     * 渲染后的标题快照。
     */
    @TableField("title")
    private String title;
    /**
     * 渲染后的正文快照。
     */
    @TableField("content")
    private String content;
    /**
     * 站内消息跳转链接。
     */
    @TableField("link")
    private String link;
    /**
     * 非敏感扩展参数。
     */
    @TableField(value = "extra", typeHandler = PgJsonbTypeHandler.class, updateStrategy = FieldStrategy.ALWAYS)
    private Map<String, Object> extra;
    /**
     * 受保护的敏感渲染载荷。
     */
    @TableField("sensitive_parameters_ciphertext")
    private String sensitiveParametersCiphertext;
    /**
     * 任务优先级。
     */
    @TableField("priority")
    private Integer priority;
    /**
     * 已尝试次数。
     */
    @TableField("attempt_count")
    private Integer attemptCount;
    /**
     * 最大尝试次数。
     */
    @TableField("max_attempts")
    private Integer maxAttempts;
    /**
     * 计划投递时间。
     */
    @TableField("scheduled_at")
    private Instant scheduledAt;
    /**
     * 下次重试时间。
     */
    @TableField("next_retry_at")
    private Instant nextRetryAt;
    /**
     * 任务过期时间。
     */
    @TableField("expires_at")
    private Instant expiresAt;
    /**
     * Worker 标识。
     */
    @TableField("locked_by")
    private String lockedBy;
    /**
     * Worker 锁定时间。
     */
    @TableField("locked_at")
    private Instant lockedAt;
    /**
     * 任务状态。
     */
    @TableField("status")
    private String status;
    /**
     * 最后一次错误码。
     */
    @TableField("last_error_code")
    private String lastErrorCode;
}
