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
 * 受控发送使用的受众范围；只描述用户、部门和角色，不携带地址等敏感信息。
 *
 * @param userIds       明确用户
 * @param departmentIds 部门及其下级部门
 * @param roleIds       当前有效角色
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
public record NotificationAudience(List<UUID> userIds, List<UUID> departmentIds, List<UUID> roleIds) {

    /**
     * 将可空集合归一化为不可变集合。
     */
    public NotificationAudience {
        userIds = userIds == null ? List.of() : List.copyOf(userIds);
        departmentIds = departmentIds == null ? List.of() : List.copyOf(departmentIds);
        roleIds = roleIds == null ? List.of() : List.copyOf(roleIds);
    }
}
