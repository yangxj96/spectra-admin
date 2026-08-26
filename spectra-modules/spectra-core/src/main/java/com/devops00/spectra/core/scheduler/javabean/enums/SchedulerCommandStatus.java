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

/** 高频循环控制命令状态。 */
public enum SchedulerCommandStatus {
    /** 已持久化，等待控制器应用。 */
    REQUESTED,
    /** 控制器正在应用。 */
    APPLYING,
    /** 已应用。 */
    APPLIED,
    /** 应用失败。 */
    FAILED,
    /** 超过截止时间。 */
    TIMEOUT
}
