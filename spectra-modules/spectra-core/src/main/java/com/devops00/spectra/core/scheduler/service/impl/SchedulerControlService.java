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

package com.devops00.spectra.core.scheduler.service.impl;

import com.devops00.spectra.core.scheduler.javabean.from.SchedulerLoopCommandFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerOperationFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerTriggerFrom;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerControlCommandVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerExecutionVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerJobVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** 调度定义状态、手工触发和 LOOP 控制命令写入用例。 */
@Service
@RequiredArgsConstructor
public class SchedulerControlService {

    private final SchedulerAdminServiceSupport support;

    @Transactional
    public SchedulerJobVO enable(UUID id, SchedulerOperationFrom from) {
        return support.enable(id, from);
    }

    @Transactional
    public SchedulerJobVO disable(UUID id, SchedulerOperationFrom from) {
        return support.disable(id, from);
    }

    @Transactional
    public SchedulerJobVO archive(UUID id, SchedulerOperationFrom from) {
        return support.archive(id, from);
    }

    @Transactional
    public SchedulerExecutionVO trigger(UUID jobId, SchedulerTriggerFrom from) {
        return support.trigger(jobId, from);
    }

    @Transactional
    public SchedulerControlCommandVO command(UUID jobId, SchedulerLoopCommandFrom from) {
        return support.command(jobId, from);
    }
}
