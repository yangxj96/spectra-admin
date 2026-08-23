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
 * 受控发送受众解析端口；具体用户、组织和角色模型留在 Core 模块。
 */
public interface NotificationAudienceDirectory {

    /**
     * 将受众范围展开为去重后的用户 ID；调用方仍需通过 {@link NotificationRecipientDirectory}
     * 解析当前有效用户、数据范围和渠道地址。
     *
     * @param audience 受众范围
     * @return 候选用户 ID
     */
    List<UUID> resolve(NotificationAudience audience);
}
