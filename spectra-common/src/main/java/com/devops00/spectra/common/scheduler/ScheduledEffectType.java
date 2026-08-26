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

package com.devops00.spectra.common.scheduler;

/**
 * 任务副作用类型，用于决定租约过期后的安全处理方式。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/26
 */
public enum ScheduledEffectType {
    /** 仅数据库事务副作用。 */
    DB_ONLY,
    /** 通过 Outbox 交付副作用。 */
    OUTBOX,
    /** 外部系统提供幂等键。 */
    EXTERNAL_IDEMPOTENT,
    /** 外部结果无法可靠确认。 */
    EXTERNAL_UNKNOWN
}
