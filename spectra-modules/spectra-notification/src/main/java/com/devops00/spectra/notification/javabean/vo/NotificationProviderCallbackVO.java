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

package com.devops00.spectra.notification.javabean.vo;

/**
 * Provider 回执确认结果；不回显消息 ID、地址或供应商原始响应。
 *
 * @param status       回执处理结果，APPLIED 或 DUPLICATE
 * @param resultStatus Delivery 当前标准状态
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
public record NotificationProviderCallbackVO(String status, String resultStatus) {
}
