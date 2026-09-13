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

package com.devops00.spectra.core.notification.javabean.vo;

import java.util.UUID;

/**
 * 封装通知发送应用相关的响应数据。
 *
 * @param requestId        请求标识
 * @param status           业务状态
 * @param taskCount        待处理的任务数量
 * @param idempotentReplay 请求是否命中幂等重放
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record NotificationControlledSendApplyVO(UUID requestId, String status, int taskCount,
                                                boolean idempotentReplay) {
}
