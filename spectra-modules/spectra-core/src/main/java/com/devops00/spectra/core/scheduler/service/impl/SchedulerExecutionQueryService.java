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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerExecutionActionFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerExecutionPageFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerLoopErrorPageFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerLoopPageFrom;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerControlCommandVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerExecutionVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerLoopErrorVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerLoopRuntimeVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerOperationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** 调度执行、LOOP 运行时、控制命令和操作历史查询/控制用例。 */
@Service
@RequiredArgsConstructor
public class SchedulerExecutionQueryService {

    private final SchedulerAdminServiceSupport support;

    public IPage<SchedulerExecutionVO> executions(PageFrom page, SchedulerExecutionPageFrom from) {
        return support.executions(page, from);
    }

    public SchedulerExecutionVO execution(UUID id) {
        return support.execution(id);
    }

    @Transactional
    public SchedulerExecutionVO retry(UUID id, SchedulerExecutionActionFrom from) {
        return support.retry(id, from);
    }

    @Transactional
    public SchedulerExecutionVO cancel(UUID id, SchedulerExecutionActionFrom from) {
        return support.cancel(id, from);
    }

    @Transactional
    public SchedulerExecutionVO resolve(UUID id, SchedulerExecutionActionFrom from) {
        return support.resolve(id, from);
    }

    public IPage<SchedulerLoopRuntimeVO> loops(PageFrom page, SchedulerLoopPageFrom from) {
        return support.loops(page, from);
    }

    public IPage<SchedulerControlCommandVO> commands(UUID jobId, PageFrom page) {
        return support.commands(jobId, page);
    }

    public IPage<SchedulerOperationVO> operations(UUID jobId, PageFrom page) {
        return support.operations(jobId, page);
    }

    public IPage<SchedulerLoopErrorVO> errors(UUID jobId, PageFrom page, SchedulerLoopErrorPageFrom from) {
        return support.errors(jobId, page, from);
    }
}
