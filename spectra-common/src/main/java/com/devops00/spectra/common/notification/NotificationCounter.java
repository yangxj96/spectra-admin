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

package com.devops00.spectra.common.notification;

import java.util.UUID;

/**
 * 消息中心只读计数端口，供工作台等业务模块查询通知摘要，不暴露收件箱持久化实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
public interface NotificationCounter {

    /**
     * 查询指定用户当前可见的未读站内信数量。
     *
     * @param userId 待查询的用户 ID
     * @return 未读且未删除的站内信数量
     */
    long unreadCount(UUID userId);
}
