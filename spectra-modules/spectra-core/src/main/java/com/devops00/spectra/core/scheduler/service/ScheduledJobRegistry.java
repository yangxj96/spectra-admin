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

package com.devops00.spectra.core.scheduler.service;

import com.devops00.spectra.common.scheduler.ScheduledJobDescriptor;
import com.devops00.spectra.common.scheduler.ScheduledJobHandler;
import com.devops00.spectra.common.scheduler.ScheduledLoopHandler;

import java.util.Collection;
import java.util.Optional;

/** 代码注册的调度处理器目录。 */
public interface ScheduledJobRegistry {

    /** 返回全部已校验的任务描述。 */
    Collection<ScheduledJobDescriptor> descriptors();

    /** 按任务键查找任务描述。 */
    Optional<ScheduledJobDescriptor> find(String jobKey);

    /** 查找离散任务处理器。 */
    Optional<ScheduledJobHandler> findJobHandler(String jobKey);

    /** 查找循环任务处理器。 */
    Optional<ScheduledLoopHandler> findLoopHandler(String jobKey);
}
