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
import com.devops00.spectra.core.notification.javabean.vo.NotificationProviderCallbackVO;

/**
 * 外部 Provider 回执处理服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
public interface NotificationProviderCallbackService {

    /**
     * 验签并幂等处理 Provider 回执。
     *
     * @param channel   回执所属渠道
     * @param signature HMAC 签名，格式为 {@code sha256=<hex>}
     * @param body      原始 JSON 请求体
     * @return 返回已验签且完成幂等落库的 Provider 回执结果，不包含原始签名和敏感内容；验签失败、报文非法或处理失败时抛出异常，不返回 null。
     */
    NotificationProviderCallbackVO handle(NotificationChannel channel, String signature, String body);
}
