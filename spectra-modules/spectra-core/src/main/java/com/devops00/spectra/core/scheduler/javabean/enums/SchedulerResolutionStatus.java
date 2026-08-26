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

/** UNKNOWN 执行的人工解决状态；不覆盖原始执行状态。 */
public enum SchedulerResolutionStatus {
    /** 尚未人工解决。 */
    UNRESOLVED,
    /** 人工确认业务已成功。 */
    CONFIRMED_SUCCESS,
    /** 人工确认业务已失败。 */
    CONFIRMED_FAILED,
    /** 已创建新的执行记录重试。 */
    RETRIED
}
