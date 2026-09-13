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

package com.devops00.spectra.core.system.javabean.from;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 承载精确撤销单个安全会话的请求参数。
 *
 * @param sessionId 稳定且不透明的会话句柄
 * @param reason    本次操作或审计事件对应的原因
 * @param confirmed 已确认状态
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record SecuritySessionRevokeOneFrom(
                                           @NotBlank(message = "会话句柄不能为空") @Size(max = 128, message = "会话句柄长度不能超过 128") String sessionId,
                                           @NotBlank(message = "操作理由不能为空") @Size(max = 200, message = "操作理由长度不能超过 200") String reason,
                                           boolean confirmed) {
}
