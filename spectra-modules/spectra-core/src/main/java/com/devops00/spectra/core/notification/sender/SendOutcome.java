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

import java.util.UUID;

/**
 * 单次渠道发送结果。
 *
 * @param success      是否成功
 * @param provider     实际供应商编码
 * @param configId     实际配置行 ID
 * @param errorCode    失败码
 * @param errorMessage 失败信息
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
public record SendOutcome(boolean success, String provider, UUID configId, String errorCode, String errorMessage) {

    /**
     * 创建成功结果。
     *
     * @param provider 供应商编码
     * @param configId 配置行 ID
     * @return 成功结果
     */
    public static SendOutcome success(String provider, UUID configId) {
        return new SendOutcome(true, provider, configId, null, null);
    }

    /**
     * 创建失败结果。
     *
     * @param provider     供应商编码
     * @param configId     配置行 ID
     * @param errorCode    失败码
     * @param errorMessage 失败信息
     * @return 失败结果
     */
    public static SendOutcome failure(String provider, UUID configId, String errorCode, String errorMessage) {
        return new SendOutcome(false, provider, configId, errorCode, errorMessage);
    }
}
