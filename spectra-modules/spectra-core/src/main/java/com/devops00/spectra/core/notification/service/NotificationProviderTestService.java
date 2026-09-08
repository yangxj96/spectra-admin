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

package com.devops00.spectra.core.notification.service;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.core.notification.javabean.from.NotificationProviderTestFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationProviderTestVO;

/**
 * Provider 测试发送服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
public interface NotificationProviderTestService {

    /**
     * 向明确指定的测试地址发送一次测试消息。
     *
     * @param channel 通知投递渠道，用于选择站内信、短信或邮件等发送路径。
     * @param params  测试收件地址、标题、正文和确认字段；只用于向明确地址发送测试消息。
     * @return 返回 Provider 测试投递的请求标识和投递状态；渠道未配置、参数非法或发送失败时抛出异常，不返回 null。
     */
    NotificationProviderTestVO send(NotificationChannel channel, NotificationProviderTestFrom params);
}
