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

/** 离散调度执行状态。 */
public enum SchedulerExecutionStatus {
    /** 等待执行器领取。 */
    QUEUED,
    /** 已被实例领取并持有租约。 */
    RUNNING,
    /** 等待安全重试时间。 */
    RETRY_WAIT,
    /** 执行成功。 */
    SUCCEEDED,
    /** 执行明确失败。 */
    FAILED,
    /** 外部副作用或结果无法确认。 */
    UNKNOWN,
    /** 因策略或调度窗口跳过。 */
    SKIPPED,
    /** 被安全取消。 */
    CANCELLED
}
