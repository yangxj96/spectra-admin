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

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * 消息中心批量删除参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Data
public class NotificationBatchDeleteFrom {

    /**
     * 待删除消息 ID 列表。
     */
    @NotEmpty(message = "消息ID列表不能为空")
    private List<UUID> ids;
}
