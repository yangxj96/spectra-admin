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

import java.util.UUID;

/**
 * 通知请求完成持久化和任务展开后的入队回执。
 *
 * @param requestId        通知模块内部的逻辑请求 ID
 * @param status           逻辑请求当前状态
 * @param taskCount        本次请求已展开的接收人×渠道任务数
 * @param idempotentReplay 是否因命中幂等键而返回已存在的请求结果
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
public record NotificationReceipt(UUID requestId, String status, int taskCount, boolean idempotentReplay) {
}
