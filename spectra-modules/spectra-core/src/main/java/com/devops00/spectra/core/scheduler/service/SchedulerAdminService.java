package com.devops00.spectra.core.scheduler.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerControlCommandEntity;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerExecutionActionFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerExecutionPageFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerJobPageFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerJobSaveFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerLoopCommandFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerLoopErrorPageFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerLoopPageFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerOperationFrom;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerCatalogVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerControlCommandVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerExecutionVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerJobVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerLoopErrorVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerLoopRuntimeVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerOperationVO;

import java.util.List;
import java.util.UUID;

/** 调度管理用例；所有输入在这里再次执行任务类型和状态校验。 */
public interface SchedulerAdminService {

    List<SchedulerCatalogVO> catalog();

    IPage<SchedulerJobVO> jobs(com.devops00.spectra.common.base.javabean.from.PageFrom page,
                               SchedulerJobPageFrom from);

    SchedulerJobVO create(SchedulerJobSaveFrom from);

    SchedulerJobVO update(UUID id, SchedulerJobSaveFrom from);

    SchedulerJobVO enable(UUID id, SchedulerOperationFrom from);

    SchedulerJobVO disable(UUID id, SchedulerOperationFrom from);

    SchedulerJobVO archive(UUID id, SchedulerOperationFrom from);

    SchedulerExecutionVO trigger(UUID jobId, com.devops00.spectra.core.scheduler.javabean.from.SchedulerTriggerFrom from);

    IPage<SchedulerExecutionVO> executions(com.devops00.spectra.common.base.javabean.from.PageFrom page,
                                           SchedulerExecutionPageFrom from);

    SchedulerExecutionVO execution(UUID id);

    SchedulerExecutionVO retry(UUID id, SchedulerExecutionActionFrom from);

    SchedulerExecutionVO cancel(UUID id, SchedulerExecutionActionFrom from);

    SchedulerExecutionVO resolve(UUID id, SchedulerExecutionActionFrom from);

    IPage<SchedulerLoopRuntimeVO> loops(com.devops00.spectra.common.base.javabean.from.PageFrom page,
                                        SchedulerLoopPageFrom from);

    SchedulerControlCommandVO command(UUID jobId, SchedulerLoopCommandFrom from);

    IPage<SchedulerControlCommandVO> commands(UUID jobId,
                                              com.devops00.spectra.common.base.javabean.from.PageFrom page);

    IPage<SchedulerOperationVO> operations(UUID jobId,
                                           com.devops00.spectra.common.base.javabean.from.PageFrom page);

    IPage<SchedulerLoopErrorVO> errors(UUID jobId, com.devops00.spectra.common.base.javabean.from.PageFrom page,
                                       SchedulerLoopErrorPageFrom from);
}
