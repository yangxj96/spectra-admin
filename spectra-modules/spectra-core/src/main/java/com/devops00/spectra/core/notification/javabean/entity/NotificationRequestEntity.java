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

package com.devops00.spectra.core.notification.javabean.entity;

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
 * 逻辑通知请求实体。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ntf_request", schema = "spectra_notification", autoResultMap = true)
public class NotificationRequestEntity extends BaseEntity {

    /**
     * 调用方生成的外部请求 ID。
     */
    @TableField("external_request_id")
    private String externalRequestId;
    /**
     * 业务幂等键。
     */
    @TableField("idempotency_key")
    private String idempotencyKey;
    /**
     * 通知用途。
     */
    @TableField("purpose")
    private String purpose;
    /**
     * 逻辑模板组编码。
     */
    @TableField("template_group_code")
    private String templateGroupCode;
    /**
     * 来源模块。
     */
    @TableField("source_module")
    private String sourceModule;
    /**
     * 业务类型弱引用。
     */
    @TableField("business_type")
    private String businessType;
    /**
     * 业务 ID 弱引用。
     */
    @TableField("business_id")
    private String businessId;
    /**
     * 发起方类型。
     */
    @TableField("initiator_type")
    private String initiatorType;
    /**
     * 发起用户 ID。
     */
    @TableField("initiator_user_id")
    private UUID initiatorUserId;
    /**
     * 来源部门快照。
     */
    @TableField("source_department_id")
    private UUID sourceDepartmentId;
    /**
     * 非敏感参数。
     */
    @TableField(value = "parameters", typeHandler = PgJsonbTypeHandler.class, updateStrategy = FieldStrategy.ALWAYS)
    private Map<String, Object> parameters;
    /**
     * 各渠道实际选中的模板版本元数据，不保存渲染正文或敏感参数。
     */
    @TableField(value = "template_snapshot", typeHandler = PgJsonbTypeHandler.class, updateStrategy = FieldStrategy.ALWAYS)
    private Map<String, Object> templateSnapshot;
    /**
     * 敏感参数密文。
     */
    @TableField("sensitive_parameters_ciphertext")
    private String sensitiveParametersCiphertext;
    /**
     * 敏感参数密钥版本。
     */
    @TableField("encryption_key_id")
    private String encryptionKeyId;
    /**
     * 请求状态。
     */
    @TableField("status")
    private String status;
    /**
     * 展开后的接收人数。
     */
    @TableField("recipient_count")
    private Integer recipientCount;
    /**
     * 展开后的任务数。
     */
    @TableField("task_count")
    private Integer taskCount;
    /**
     * 计划投递时间。
     */
    @TableField("scheduled_at")
    private Instant scheduledAt;
    /**
     * 请求过期时间。
     */
    @TableField("expires_at")
    private Instant expiresAt;
    /**
     * 请求优先级。
     */
    @TableField("priority")
    private Integer priority;
    /**
     * 链路追踪 ID。
     */
    @TableField("trace_id")
    private String traceId;
}
