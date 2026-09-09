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

package com.devops00.spectra.core.scheduler.history.javabean.enums;

/** Quartz 执行历史的生命周期状态。 */
public enum QuartzExecutionHistoryStatus {
    /** 正在执行。 */
    RUNNING,
    /** 已成功完成。 */
    SUCCEEDED,
    /** 执行抛出异常。 */
    FAILED,
    /** Quartz 在执行前否决了任务。 */
    VETOED,
    /** 超过运行租约仍未收到完成回调。 */
    ABANDONED
}
