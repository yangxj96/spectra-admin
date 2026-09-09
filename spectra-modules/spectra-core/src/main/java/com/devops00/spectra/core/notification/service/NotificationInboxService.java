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

package com.devops00.spectra.core.notification.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.core.notification.javabean.from.NotificationQueryFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationInboxVO;

import java.util.List;
import java.util.UUID;

/**
 * 当前用户消息中心服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
public interface NotificationInboxService {

    /**
     * 查询当前用户消息，查询条件始终附带收件人所有权。
     *
     * @param page   分页条件，包含页码、页大小和排序字段。
     * @param userId 目标用户的唯一标识，用于限定查询或变更范围。
     * @param params 消息状态、渠道、通知用途和时间范围等当前用户收件箱筛选条件。
     * @return 返回按分页条件查询的用户收件箱消息分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<NotificationInboxVO> page(PageFrom page, UUID userId, NotificationQueryFrom params);

    /**
     * 查询当前用户未读数。
     *
     * @param userId 目标用户的唯一标识，用于限定查询或变更范围。
     * @return 返回指定用户当前未读通知数量；没有未读通知时返回 0，不返回 null。
     */
    long unreadCount(UUID userId);

    /**
     * 查询当前用户消息详情。
     *
     * @param id     目标通知记录的唯一标识。
     * @param userId 目标用户的唯一标识，用于限定查询或变更范围。
     * @return 返回符合条件的用户收件箱消息详情；记录不存在或当前用户不可见时抛出业务异常，不返回 null。
     */
    NotificationInboxVO detail(UUID id, UUID userId);

    /**
     * 标记单条已读。
     *
     * @param id     目标通知记录的唯一标识。
     * @param userId 目标用户的唯一标识，用于限定查询或变更范围。
     */
    void markAsRead(UUID id, UUID userId);

    /**
     * 标记当前用户全部已读。
     *
     * @param userId 目标用户的唯一标识，用于限定查询或变更范围。
     */
    void markAllAsRead(UUID userId);

    /**
     * 删除当前用户消息。
     *
     * @param id     目标通知记录的唯一标识。
     * @param userId 目标用户的唯一标识，用于限定查询或变更范围。
     */
    void deleteById(UUID id, UUID userId);

    /**
     * 批量删除当前用户消息，混合 ID 不影响其他用户记录。
     *
     * @param ids    要从当前用户收件箱删除的通知记录 ID；空列表不删除任何记录，列表中的其他用户记录会被忽略。
     * @param userId 目标用户的唯一标识，用于限定查询或变更范围。
     */
    void batchDelete(List<UUID> ids, UUID userId);
}
