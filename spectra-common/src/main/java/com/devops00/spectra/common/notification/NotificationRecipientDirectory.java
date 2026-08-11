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

import java.util.List;
import java.util.UUID;

/**
 * Core 向通知模块提供的收件人解析端口，只返回投递所需的最小快照。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
public interface NotificationRecipientDirectory {

    /**
     * 按用户 ID 解析通知所需的最小收件人快照。
     *
     * @param userIds
     *            用户 ID
     * @return 与输入 ID 一一对应的快照；未知用户返回 inactive 快照
     */
    List<NotificationRecipient> resolve(List<UUID> userIds);

    /**
     * 按登录名解析流程任务等内部调用方使用的收件人快照。
     *
     * @param loginNames
     *            用户登录名；空值和重复值由实现过滤
     * @return 可解析登录名对应的收件人快照
     */
    List<NotificationRecipient> resolveByLoginNames(List<String> loginNames);
}
