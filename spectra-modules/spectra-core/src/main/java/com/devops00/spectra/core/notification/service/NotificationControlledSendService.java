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

import com.devops00.spectra.core.notification.javabean.from.NotificationControlledSendApplyFrom;
import com.devops00.spectra.core.notification.javabean.from.NotificationControlledSendFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationControlledSendApplyVO;
import com.devops00.spectra.core.notification.javabean.vo.NotificationControlledSendPreviewVO;

/**
 * 通知中心受控发送服务。
 */
public interface NotificationControlledSendService {

    /**
     * 生成短时一次性 Preview。
     *
     * @param params 幂等键、通知用途、渠道、模板版本、收件人和模板参数等一次性发送内容。
     * @return 返回一次性发送预览，包含请求标识、待投递渠道和消费凭证；参数校验或签发失败时抛出异常，不返回 null。
     */
    NotificationControlledSendPreviewVO preview(NotificationControlledSendFrom params);

    /**
     * 消费 Preview 并通过统一 Gateway 入队。
     *
     * @param params 预览 ID、一次性预览凭证和请求摘要，用于校验预览未过期且未被重复消费。
     * @return 返回消费预览后生成的通知入队结果，包含请求标识和当前投递状态；预览失效、重复消费或入队失败时抛出异常，不返回 null。
     */
    NotificationControlledSendApplyVO apply(NotificationControlledSendApplyFrom params);
}
