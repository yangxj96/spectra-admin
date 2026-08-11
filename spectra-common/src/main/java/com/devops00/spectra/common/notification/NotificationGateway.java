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
 * 业务模块使用的统一通知入口，隔离具体渠道、模板、重试和持久化实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
public interface NotificationGateway {

    /**
     * 查询指定渠道是否已配置并可接受投递。
     *
     * @param channel
     *            待检查的通知渠道
     * @return 渠道标识、可用状态与脱敏原因
     */
    NotificationChannelAvailability availability(NotificationChannel channel);

    /**
     * 提交不可变通知请求，完成幂等校验和收件人任务展开后返回入队回执。
     *
     * @param request
     *            业务模块构造的通知请求
     * @return 逻辑请求 ID、状态、任务数与幂等重放标记
     */
    NotificationReceipt enqueue(NotificationRequest request);
}
