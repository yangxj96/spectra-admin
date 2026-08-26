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

/** 离散任务错过计划时间后的处理策略。 */
public enum SchedulerMisfirePolicy {
    /** 丢弃错过的计划。 */
    SKIP,
    /** 只补发一次。 */
    FIRE_ONCE,
    /** 按注册策略限制补发数量。 */
    CATCH_UP_LIMITED
}
