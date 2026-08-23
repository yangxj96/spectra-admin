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

import com.devops00.spectra.common.notification.NotificationChannel;

import java.time.Instant;

/**
 * Provider 运行时配置；其中 Secret 只在内存中短暂存在，不得转换为管理端 VO。
 *
 * @param channel             通知渠道
 * @param providerType        Provider 类型
 * @param enabled             是否启用
 * @param endpoint            Provider 端点
 * @param timeoutMs           请求超时毫秒数
 * @param rateLimitPerSecond 每秒发送上限
 * @param maxAttempts         最大投递尝试次数
 * @param templateCode        外部渠道模板编码
 * @param secret              已解密的 Secret，仅供 Provider 发送时使用
 * @param secretKeyId         Secret 标识
 * @param updatedAt           配置更新时间
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
public record NotificationProviderConfiguration(
        NotificationChannel channel,
        String providerType,
        boolean enabled,
        String endpoint,
        int timeoutMs,
        int rateLimitPerSecond,
        int maxAttempts,
        String templateCode,
        String secret,
        String secretKeyId,
        Instant updatedAt) {
}
