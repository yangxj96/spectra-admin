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

/**
 * 未登录场景使用的直接投递地址，仅允许策略明确授权的认证和安全用途使用。
 *
 * @param channel 目标外部渠道，通常为短信或邮件
 * @param address 未加密的原始地址，仅允许在入队边界短暂存活
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
public record NotificationDirectAddress(NotificationChannel channel, String address) {
}
