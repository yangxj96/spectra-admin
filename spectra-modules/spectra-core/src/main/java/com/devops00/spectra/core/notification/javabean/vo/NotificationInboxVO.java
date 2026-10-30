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

package com.devops00.spectra.core.notification.javabean.vo;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 当前用户消息中心响应对象。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
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
     * 模板编码。
     */
    private String templateCode;
    /**
     * 站内跳转链接。
     */
    private String link;
    /**
     * 是否已读。
     */
    private Boolean isRead;
    /**
     * 阅读时间。
     */
    private OffsetDateTime readAt;
    /**
     * 收件用户 ID。
     */
    private UUID receiverUserId;
    /**
     * 创建时间。
     */
    private OffsetDateTime createdAt;
}
