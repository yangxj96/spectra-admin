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

package com.devops00.spectra.notification.javabean.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 消息中心响应对象，保留旧消息中心字段兼容性。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Data
public class NotificationInboxVO {

    /**
     * 消息 ID。
     */
    private UUID id;
    /**
     * 消息标题。
     */
    private String title;
    /**
     * 消息正文。
     */
    private String content;
    /**
     * 兼容旧消息中心的消息分类。
     */
    private String type;
    /**
     * 发件用户 ID。
     */
    private UUID senderId;
    /**
     * 发件人名称。
     */
    private String senderName;
    /**
     * 客户端跳转链接。
     */
    private String link;
    /**
     * 是否已读。
     */
    private Boolean isRead;
    /**
     * 阅读时间。
     */
    private LocalDateTime readAt;
    /**
     * 收件用户 ID。
     */
    private UUID receiverId;
    /**
     * 非敏感扩展参数。
     */
    private Map<String, Object> extra;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
}
