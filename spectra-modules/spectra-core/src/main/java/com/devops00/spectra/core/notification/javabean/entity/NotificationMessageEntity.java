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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.framework.persistence.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/**
 * 通知发送历史实体；站内信记录同时作为用户收件箱。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ntf_message", schema = "spectra_core")
public class NotificationMessageEntity extends BaseEntity {

    /**
     * 发送渠道：IN_APP、SMS 或 EMAIL。
     */
    @TableField("channel")
    private String channel;
    /**
     * 模板编码快照。
     */
    @TableField("template_code")
    private String templateCode;
    /**
     * 渲染后的标题。
     */
    @TableField("title")
    private String title;
    /**
     * 渲染后的纯文本正文。
     */
    @TableField("content")
    private String content;
    /**
     * 渲染后的 HTML 正文；仅邮件使用。
     */
    @TableField("html_content")
    private String htmlContent;
    /**
     * 发送结果状态：SUCCESS 或 FAILED。
     */
    @TableField("status")
    private String status;
    /**
     * 失败码。
     */
    @TableField("error_code")
    private String errorCode;
    /**
     * 失败信息。
     */
    @TableField("error_message")
    private String errorMessage;
    /**
     * 实际使用的供应商编码。
     */
    @TableField("provider")
    private String provider;
    /**
     * 实际使用的配置行 ID。
     */
    @TableField("config_id")
    private UUID configId;
    /**
     * 收件用户 ID；直接地址场景为空。
     */
    @TableField("recipient_user_id")
    private UUID recipientUserId;
    /**
     * 实际收件地址；站内信场景为空。
     */
    @TableField("recipient_address")
    private String recipientAddress;
    /**
     * 站内信已读时间；为空表示未读。
     */
    @TableField("read_at")
    private Instant readAt;
    /**
     * 业务类型。
     */
    @TableField("business_type")
    private String businessType;
    /**
     * 业务 ID。
     */
    @TableField("business_id")
    private String businessId;
    /**
     * 来源模块。
     */
    @TableField("source_module")
    private String sourceModule;
    /**
     * 站内跳转链接。
     */
    @TableField("link")
    private String link;
}
