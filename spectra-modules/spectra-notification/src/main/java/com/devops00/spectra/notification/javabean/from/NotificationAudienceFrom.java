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

package com.devops00.spectra.notification.javabean.from;

import com.devops00.spectra.common.notification.NotificationAudience;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * 受控发送受众范围。
 */
@Data
public class NotificationAudienceFrom {

    /**
     * 明确用户，最多五千个。
     */
    @Size(max = 5_000, message = "明确用户数量不能超过 5000")
    private List<UUID> userIds;

    /**
     * 部门范围，包含下级部门，最多一百个根部门。
     */
    @Size(max = 100, message = "部门范围不能超过 100 个")
    private List<UUID> departmentIds;

    /**
     * 角色范围，最多一百个角色。
     */
    @Size(max = 100, message = "角色范围不能超过 100 个")
    private List<UUID> roleIds;

    /**
     * 转为跨模块受众契约。
     */
    public NotificationAudience toAudience() {
        return new NotificationAudience(userIds, departmentIds, roleIds);
    }
}
