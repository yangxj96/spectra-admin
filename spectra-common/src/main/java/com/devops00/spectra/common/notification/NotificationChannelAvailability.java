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
 * 通知渠道的运行时可用性快照，供业务模块在入队前判断渠道是否可用。
 *
 * @param channel   被检查的通知渠道
 * @param available 当前配置和运行状态是否允许接收投递
 * @param reason    不可用时的脱敏原因；可用时可为空
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
public record NotificationChannelAvailability(NotificationChannel channel, boolean available, String reason) {
}
