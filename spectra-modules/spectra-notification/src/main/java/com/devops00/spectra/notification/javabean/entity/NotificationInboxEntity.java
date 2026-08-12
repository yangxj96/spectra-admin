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
 * 当前用户站内信收件箱实体。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Getter
@Setter
@ToString
@TableName(value = "ntf_inbox_message", schema = "spectra_notification", autoResultMap = true)
public class NotificationInboxEntity extends BaseEntity {

    /**
     * 租户 ID。
     */
    @TableField("tenant_id")
    private UUID tenantId;
    /**
     * 消息所有者用户 ID。
     */
    @TableField("receiver_user_id")
    private UUID receiverUserId;
    /**
     * 逻辑通知请求 ID；迁移历史数据时可空。
     */
    @TableField("notification_request_id")
    private UUID notificationRequestId;
    /**
     * IN_APP 通知任务 ID；迁移历史数据时可空。
     */
    @TableField("notification_task_id")
    private UUID notificationTaskId;
    /**
     * 通知用途。
     */
    @TableField("purpose")
    private String purpose;
    /**
     * 消息标题。
     */
    @TableField("title")
    private String title;
    /**
     * 消息正文。
     */
    @TableField("content")
    private String content;
    /**
     * 站内信发送人。
     */
    @TableField("sender_user_id")
    private UUID senderUserId;
    /**
     * 发送人名称快照。
     */
    @TableField("sender_name")
    private String senderName;
    /**
     * 客户端跳转路径。
     */
    @TableField("link")
    private String link;
    /**
     * 是否已读。
     */
    @TableField("is_read")
    private Boolean isRead;
    /**
     * 阅读时间。
     */
    @TableField("read_at")
    private Instant readAt;
    /**
     * 白名单扩展信息。
     */
    @TableField(value = "extra", typeHandler = PgJsonbTypeHandler.class, updateStrategy = FieldStrategy.ALWAYS)
    private Map<String, Object> extra;
}
