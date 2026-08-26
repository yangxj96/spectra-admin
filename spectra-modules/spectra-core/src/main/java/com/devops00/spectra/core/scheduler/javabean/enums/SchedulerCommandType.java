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

/** 高频循环控制命令类型。 */
public enum SchedulerCommandType {
    /** 启动循环。 */
    START,
    /** 排空后停止。 */
    DRAIN_STOP,
    /** 创建新会话并重新启动。 */
    RESTART,
    /** 立即停止循环，不伪造业务项结果。 */
    FORCE_STOP,
    /** 在确认租约过期且实例失联后强制回收。 */
    FORCE_RECLAIM
}
