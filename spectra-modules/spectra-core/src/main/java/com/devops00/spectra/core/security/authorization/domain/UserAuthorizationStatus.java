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

package com.devops00.spectra.core.security.authorization.domain;

/**
 * 用户当前授权状态。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
public enum UserAuthorizationStatus {

    /** 没有任何 RoleAssignment。 */
    UNCONFIGURED,

    /** 存在有效 RoleAssignment，但缺少完整的 Permission Boundary。 */
    INCOMPLETE,

    /** 至少一个有效 RoleAssignment 已完整生效，且没有失效授权。 */
    ACTIVE,

    /** 存在已撤销、已过期、停用 Role 或部分失效的授权。 */
    PARTIAL
}
