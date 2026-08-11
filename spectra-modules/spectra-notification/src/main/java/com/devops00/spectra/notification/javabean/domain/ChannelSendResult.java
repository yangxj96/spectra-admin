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

package com.devops00.spectra.notification.javabean.domain;

/**
 * 渠道发送结果；占位渠道不会伪造成功。
 *
 * @param status            标准化投递结果状态
 * @param providerCode      执行投递的渠道供应商编码
 * @param providerMessageId 供应商返回的消息 ID；未提供时为空
 * @param summary           可安全持久化的脱敏响应摘要
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
public record ChannelSendResult(String status, String providerCode, String providerMessageId, String summary) {
}
