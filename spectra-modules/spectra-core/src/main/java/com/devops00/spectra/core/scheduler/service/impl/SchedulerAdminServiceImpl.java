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

import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerExecutionActionFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerExecutionPageFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerJobPageFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerJobSaveFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerLoopCommandFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerLoopErrorPageFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerLoopPageFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerOperationFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerTriggerFrom;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerCatalogVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerControlCommandVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerExecutionVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerJobVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerLoopErrorVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerLoopRuntimeVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerOperationVO;
import com.devops00.spectra.core.scheduler.service.SchedulerAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** 调度管理公开入口，只负责把目录、控制和执行查询请求路由到对应的用例服务。 */
@Service
@Primary
public class SchedulerAdminServiceImpl implements SchedulerAdminService {

    private final SchedulerCatalogService catalogService;

    private final SchedulerControlService controlService;

    private final SchedulerExecutionQueryService executionQueryService;

    @Autowired
    public SchedulerAdminServiceImpl(SchedulerCatalogService catalogService, SchedulerControlService controlService,
                                     SchedulerExecutionQueryService executionQueryService) {
        this.catalogService = catalogService;
        this.controlService = controlService;
        this.executionQueryService = executionQueryService;
    }

    @Override
    public List<SchedulerCatalogVO> catalog() {
        return catalogService.catalog();
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<SchedulerJobVO> jobs(PageFrom page, SchedulerJobPageFrom from) {
        return catalogService.jobs(page, from);
    }

    @Override
    public SchedulerJobVO create(SchedulerJobSaveFrom from) {
        return catalogService.create(from);
    }

    @Override
    public SchedulerJobVO update(UUID id, SchedulerJobSaveFrom from) {
        return catalogService.update(id, from);
    }

    @Override
    public SchedulerJobVO enable(UUID id, SchedulerOperationFrom from) {
        return controlService.enable(id, from);
    }

    @Override
    public SchedulerJobVO disable(UUID id, SchedulerOperationFrom from) {
        return controlService.disable(id, from);
    }

    @Override
    public SchedulerJobVO archive(UUID id, SchedulerOperationFrom from) {
        return controlService.archive(id, from);
    }

    @Override
    public SchedulerExecutionVO trigger(UUID jobId, SchedulerTriggerFrom from) {
        return controlService.trigger(jobId, from);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<SchedulerExecutionVO> executions(PageFrom page,
                                                                                         SchedulerExecutionPageFrom from) {
        return executionQueryService.executions(page, from);
    }

    @Override
    public SchedulerExecutionVO execution(UUID id) {
        return executionQueryService.execution(id);
    }

    @Override
    public SchedulerExecutionVO retry(UUID id, SchedulerExecutionActionFrom from) {
        return executionQueryService.retry(id, from);
    }

    @Override
    public SchedulerExecutionVO cancel(UUID id, SchedulerExecutionActionFrom from) {
        return executionQueryService.cancel(id, from);
    }

    @Override
    public SchedulerExecutionVO resolve(UUID id, SchedulerExecutionActionFrom from) {
        return executionQueryService.resolve(id, from);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<SchedulerLoopRuntimeVO> loops(PageFrom page,
                                                                                      SchedulerLoopPageFrom from) {
        return executionQueryService.loops(page, from);
    }

    @Override
    public SchedulerControlCommandVO command(UUID jobId, SchedulerLoopCommandFrom from) {
        return controlService.command(jobId, from);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<SchedulerControlCommandVO> commands(UUID jobId, PageFrom page) {
        return executionQueryService.commands(jobId, page);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<SchedulerOperationVO> operations(UUID jobId, PageFrom page) {
        return executionQueryService.operations(jobId, page);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<SchedulerLoopErrorVO> errors(UUID jobId, PageFrom page,
                                                                                     SchedulerLoopErrorPageFrom from) {
        return executionQueryService.errors(jobId, page, from);
    }
}
