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

package com.devops00.spectra.core.notification.sender;

import com.devops00.spectra.common.notification.NotificationChannel;

import java.util.UUID;

/**
 * 单个通知发送目标。
 *
 * @param channel 发送渠道
 * @param userId  收件用户 ID；直接地址场景为空
 * @param address 收件地址；站内信场景为空
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
public record NotificationSendTarget(NotificationChannel channel, UUID userId, String address) {
}
