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

package com.devops00.spectra.common.audit;

/**
 * 审计事件的可靠性分类。
 *
 * <p>分类只描述事件语义，不决定具体存储。普通操作日志和安全审计仍由不同 sink
 * 负责其事务和失败语义。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
public enum AuditCategory {

    /** 普通业务操作，最终由操作日志 outbox 持久化。 */
    OPERATION,

    /** 安全边界、认证和授权事实，必须同步可靠记录。 */
    SECURITY
}
