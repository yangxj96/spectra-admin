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
 * 单次渠道投递尝试实体。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Getter
@Setter
@ToString
@TableName(value = "ntf_delivery", schema = "spectra_notification", autoResultMap = true)
public class NotificationDeliveryEntity extends BaseEntity {

    /**
     * 所属通知任务 ID。
     */
    @TableField("notification_task_id")
    private UUID notificationTaskId;
    /**
     * 当前任务尝试序号。
     */
    @TableField("attempt_no")
    private Integer attemptNo;
    /**
     * 供应商标识。
     */
    @TableField("provider")
    private String provider;
    /**
     * 供应商返回的消息 ID。
     */
    @TableField("provider_message_id")
    private String providerMessageId;
    /**
     * 开始发送时间。
     */
    @TableField("started_at")
    private Instant startedAt;
    /**
     * 完成发送时间。
     */
    @TableField("completed_at")
    private Instant completedAt;
    /**
     * 标准化投递结果。
     */
    @TableField("result_status")
    private String resultStatus;
    /**
     * 标准化错误码。
     */
    @TableField("error_code")
    private String errorCode;
    /**
     * 脱敏错误信息。
     */
    @TableField("error_message_sanitized")
    private String errorMessageSanitized;
    /**
     * 发送耗时毫秒数。
     */
    @TableField("duration_ms")
    private Long durationMs;
    /**
     * 白名单响应摘要。
     */
    @TableField(value = "response_summary", typeHandler = PgJsonbTypeHandler.class, updateStrategy = FieldStrategy.ALWAYS)
    private Map<String, Object> responseSummary;
}
