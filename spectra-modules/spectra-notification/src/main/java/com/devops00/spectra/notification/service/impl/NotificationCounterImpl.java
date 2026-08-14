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

package com.devops00.spectra.notification.service.impl;

import com.devops00.spectra.common.notification.NotificationCounter;
import com.devops00.spectra.notification.service.NotificationInboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 消息中心未读数公共端口实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Service
@RequiredArgsConstructor
public class NotificationCounterImpl implements NotificationCounter {

    /**
     * 消息中心服务。
     */
    private final NotificationInboxService inboxService;

    /**
     * 查询指定用户未读消息数；匿名用户返回零。
     */
    @Override
    public long unreadCount(UUID userId) {
        return userId == null ? 0 : inboxService.unreadCount(userId);
    }
}
