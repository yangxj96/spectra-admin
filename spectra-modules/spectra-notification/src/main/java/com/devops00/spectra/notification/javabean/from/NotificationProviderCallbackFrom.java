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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 外部 Provider 回执入参；只接受标准化回执字段，不保留供应商原始正文。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Data
public class NotificationProviderCallbackFrom {

    /**
     * Provider 返回的消息 ID。
     */
    @NotBlank(message = "Provider 消息 ID 不能为空")
    @Size(max = 200, message = "Provider 消息 ID 长度不能超过 200 个字符")
    private String messageId;

    /**
     * Provider 回执状态；支持 ACCEPTED、SENT、DELIVERED、FAILED、BOUNCED、REJECTED、UNKNOWN。
     */
    @NotBlank(message = "Provider 回执状态不能为空")
    @Size(max = 32, message = "Provider 回执状态长度不能超过 32 个字符")
    private String status;

    /**
     * 供应商错误码；只在安全白名单中持久化。
     */
    @Size(max = 100, message = "Provider 错误码长度不能超过 100 个字符")
    private String errorCode;
}
