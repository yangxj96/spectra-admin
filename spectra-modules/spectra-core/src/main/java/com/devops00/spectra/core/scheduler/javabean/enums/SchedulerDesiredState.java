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

/** 持久化的任务期望状态。 */
public enum SchedulerDesiredState {
    /** 离散任务启用。 */
    ENABLED,
    /** 离散任务禁用。 */
    DISABLED,
    /** 循环任务持续运行。 */
    RUNNING,
    /** 循环任务排空后停止。 */
    DRAINING,
    /** 循环任务停止。 */
    STOPPED
}
