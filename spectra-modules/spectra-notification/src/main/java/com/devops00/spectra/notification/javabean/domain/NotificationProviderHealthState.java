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

package com.devops00.spectra.notification.javabean.domain;

/**
 * Provider 健康检查的有限状态集合。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
public enum NotificationProviderHealthState {

    /** Provider 已通过健康检查。 */
    HEALTHY,

    /** Provider 已配置但健康检查未通过。 */
    UNHEALTHY,

    /** Provider 尚未配置。 */
    NOT_CONFIGURED,

    /** Provider 被配置显式停用。 */
    DISABLED,

    /** 模块或安全门禁阻止使用 Provider。 */
    BLOCKED
}
