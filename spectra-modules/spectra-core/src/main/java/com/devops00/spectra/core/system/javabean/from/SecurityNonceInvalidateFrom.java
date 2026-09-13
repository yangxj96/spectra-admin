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

package com.devops00.spectra.core.system.javabean.from;

/**
 * 承载安全随机数相关的请求参数。
 *
 * @param nonce     待全局失效的防重放随机数
 * @param reason    本次操作或审计事件对应的原因
 * @param confirmed 已确认状态
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record SecurityNonceInvalidateFrom(String nonce, String reason, boolean confirmed) {
}
