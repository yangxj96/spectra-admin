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

package com.devops00.spectra.core.scheduler.javabean.enums;

/** 离散任务并发执行策略。 */
public enum SchedulerConcurrencyPolicy {
    /** 已有执行时不再领取新的执行。 */
    FORBID,
    /** 允许并行执行。 */
    ALLOW,
    /** 由调度策略取消尚未开始的旧执行后替换。 */
    REPLACE
}
