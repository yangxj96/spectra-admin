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

/** 高频循环运行会话状态。 */
public enum SchedulerRuntimeStatus {
    /** 正在建立运行会话。 */
    STARTING,
    /** 正常运行。 */
    RUNNING,
    /** 错误增加但仍在运行。 */
    DEGRADED,
    /** 不再领取新业务项，等待已领取项完成。 */
    DRAINING,
    /** 已正常停止。 */
    STOPPED,
    /** 循环异常退出。 */
    CRASHED,
    /** 运行结果无法确认。 */
    UNKNOWN
}
