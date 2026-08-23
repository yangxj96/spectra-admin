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

package com.devops00.spectra.notification.javabean.vo;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Provider 测试发送结果；不返回测试收件地址或供应商原始响应。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Value
@Builder
public class NotificationProviderTestVO {

    /**
     * 测试渠道。
     */
    String channel;

    /**
     * Provider 编码。
     */
    String providerCode;

    /**
     * 标准化发送状态。
     */
    String status;

    /**
     * 供应商消息 ID；未提供时为空。
     */
    String providerMessageId;

    /**
     * 脱敏结果摘要。
     */
    String summary;

    /**
     * 测试时间。
     */
    Instant testedAt;
}
