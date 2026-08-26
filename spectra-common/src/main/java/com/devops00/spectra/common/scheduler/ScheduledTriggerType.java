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
 * 离散任务触发来源。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/26
 */
public enum ScheduledTriggerType {
    /** 自动调度。 */
    SCHEDULE,
    /** 运维人员手工触发。 */
    MANUAL,
    /** 基于已有执行记录创建的新重试。 */
    RETRY
}
