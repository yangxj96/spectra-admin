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
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Provider 测试发送入参；测试目标只在本次请求内存中使用，不落库。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Data
public class NotificationProviderTestFrom {

    /**
     * 明确的测试收件地址；不会从用户或通知任务中推导。
     */
    @NotBlank(message = "测试收件地址不能为空")
    @Size(max = 500, message = "测试收件地址长度不能超过 500 个字符")
    private String recipientAddress;

    /**
     * 测试标题。
     */
    @NotBlank(message = "测试标题不能为空")
    @Size(max = 200, message = "测试标题长度不能超过 200 个字符")
    private String title;

    /**
     * 测试正文。
     */
    @NotBlank(message = "测试正文不能为空")
    @Size(max = 2_000, message = "测试正文长度不能超过 2000 个字符")
    private String content;

    /**
     * 明确操作确认词，避免误触发外部渠道。
     */
    @NotBlank(message = "测试发送确认词不能为空")
    @Pattern(regexp = "SEND_TEST", message = "测试发送必须输入确认词 SEND_TEST")
    private String confirmation;
}
