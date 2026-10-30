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
import com.devops00.spectra.core.notification.javabean.from.NotificationQueryFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationInboxVO;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;

import java.util.List;
import java.util.UUID;

/**
 * 当前用户消息中心服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
public interface NotificationInboxService {

    /**
     * 分页查询当前用户消息。
     *
     * @param page   分页参数
     * @param userId 当前用户 ID
     * @param query  查询条件
     * @return 消息分页
     */
    IPage<NotificationInboxVO> page(PageFrom page, UUID userId, NotificationQueryFrom query);

    /**
     * 统计当前用户未读消息数量。
     *
     * @param userId 当前用户 ID
     * @return 未读数量
     */
    long unreadCount(UUID userId);

    /**
     * 查询当前用户消息详情。
     *
     * @param id     消息 ID
     * @param userId 当前用户 ID
     * @return 消息详情
     */
    NotificationInboxVO detail(UUID id, UUID userId);

    /**
     * 标记单条消息已读。
     *
     * @param id     消息 ID
     * @param userId 当前用户 ID
     */
    void markAsRead(UUID id, UUID userId);

    /**
     * 标记当前用户全部消息已读。
     *
     * @param userId 当前用户 ID
     */
    void markAllAsRead(UUID userId);

    /**
     * 删除当前用户一条消息。
     *
     * @param id     消息 ID
     * @param userId 当前用户 ID
     */
    void deleteById(UUID id, UUID userId);

    /**
     * 批量删除当前用户消息。
     *
     * @param ids    消息 ID 列表
     * @param userId 当前用户 ID
     */
    void batchDelete(List<UUID> ids, UUID userId);
}
