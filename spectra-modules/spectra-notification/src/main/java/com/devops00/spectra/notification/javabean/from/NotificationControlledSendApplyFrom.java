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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

/**
 * 受控发送 Apply 请求；所有发送内容都来自已保存的 Preview 快照。
 */
@Data
public class NotificationControlledSendApplyFrom {

    /**
     * Preview 记录 ID。
     */
    @NotNull(message = "Preview ID 不能为空")
    private UUID previewId;

    /**
     * Preview 只返回一次的明文令牌。
     */
    @NotBlank(message = "Preview token 不能为空")
    @Size(max = 128, message = "Preview token 长度不合法")
    private String previewToken;

    /**
     * 前端确认时携带的请求摘要。
     */
    @NotBlank(message = "Preview 请求摘要不能为空")
    @Size(max = 64, message = "Preview 请求摘要长度不合法")
    private String requestHash;
}
