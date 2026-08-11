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

package com.devops00.spectra.notification.inbox.service;

import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.notification.inbox.javabean.from.NotificationQueryFrom;
import com.devops00.spectra.notification.inbox.javabean.vo.NotificationInboxVO;

/** 当前用户消息中心服务。 */
public interface NotificationInboxService {

    /** 查询当前用户消息，查询条件始终附带收件人所有权。 */
    IPage<NotificationInboxVO> page(PageFrom page, UUID tenantId, UUID userId, NotificationQueryFrom params);

    /** 查询当前用户未读数。 */
    long unreadCount(UUID tenantId, UUID userId);

    /** 查询当前用户消息详情。 */
    NotificationInboxVO detail(UUID id, UUID tenantId, UUID userId);

    /** 标记单条已读。 */
    void markAsRead(UUID id, UUID tenantId, UUID userId);

    /** 标记当前用户全部已读。 */
    void markAllAsRead(UUID tenantId, UUID userId);

    /** 删除当前用户消息。 */
    void deleteById(UUID id, UUID tenantId, UUID userId);

    /** 批量删除当前用户消息，混合 ID 不影响其他用户记录。 */
    void batchDelete(List<UUID> ids, UUID tenantId, UUID userId);
}
