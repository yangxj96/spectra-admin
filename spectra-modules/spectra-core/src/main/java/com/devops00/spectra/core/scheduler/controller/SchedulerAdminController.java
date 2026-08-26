package com.devops00.spectra.core.scheduler.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
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
import com.devops00.spectra.log.base.annotation.ULog;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** 单体调度管理接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/scheduler/admin")
public class SchedulerAdminController {

    private final SchedulerAdminService adminService;

    @ULog("'查询调度处理器目录'")
    @GetMapping(value = "/catalog", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:scheduler:query')")
    public List<SchedulerCatalogVO> catalog() {
        return adminService.catalog();
    }

    @ULog("'查询调度任务定义'")
    @GetMapping(value = "/jobs", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:scheduler:query')")
    public IPage<SchedulerJobVO> jobs(PageFrom page, SchedulerJobPageFrom from) {
        return adminService.jobs(page, from);
    }

    @ULog("'创建调度任务定义'")
    @PostMapping(value = "/jobs", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:scheduler:manage')")
    public SchedulerJobVO create(@Validated @RequestBody SchedulerJobSaveFrom from) {
        return adminService.create(from);
    }

    @ULog("'修改调度任务定义'")
    @PutMapping(value = "/jobs/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:scheduler:manage')")
    public SchedulerJobVO update(@PathVariable UUID id, @Validated @RequestBody SchedulerJobSaveFrom from) {
        return adminService.update(id, from);
    }

    @ULog("'启用调度任务'")
    @PostMapping(value = "/jobs/{id}/enable", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:scheduler:manage')")
    public SchedulerJobVO enable(@PathVariable UUID id, @Validated @RequestBody SchedulerOperationFrom from) {
        return adminService.enable(id, from);
    }

    @ULog("'停用调度任务'")
    @PostMapping(value = "/jobs/{id}/disable", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:scheduler:manage')")
    public SchedulerJobVO disable(@PathVariable UUID id, @Validated @RequestBody SchedulerOperationFrom from) {
        return adminService.disable(id, from);
    }

    @ULog("'归档调度任务'")
    @PostMapping(value = "/jobs/{id}/archive", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:scheduler:manage')")
    public SchedulerJobVO archive(@PathVariable UUID id, @Validated @RequestBody SchedulerOperationFrom from) {
        return adminService.archive(id, from);
    }

    @ULog("'手工触发调度任务'")
    @PostMapping(value = "/jobs/{id}/trigger", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:scheduler:execute')")
    public SchedulerExecutionVO trigger(@PathVariable UUID id, @Validated @RequestBody SchedulerTriggerFrom from) {
        return adminService.trigger(id, from);
    }

    @ULog("'查询调度执行记录'")
    @GetMapping(value = "/executions", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:scheduler:query')")
    public IPage<SchedulerExecutionVO> executions(PageFrom page, SchedulerExecutionPageFrom from) {
        return adminService.executions(page, from);
    }

    @ULog("'查询调度执行详情'")
    @GetMapping(value = "/executions/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:scheduler:query')")
    public SchedulerExecutionVO execution(@PathVariable UUID id) {
        return adminService.execution(id);
    }

    @ULog("'人工重试调度执行'")
    @PostMapping(value = "/executions/{id}/retry", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:scheduler:retry')")
    public SchedulerExecutionVO retry(@PathVariable UUID id,
                                      @Validated @RequestBody SchedulerExecutionActionFrom from) {
        return adminService.retry(id, from);
    }

    @ULog("'取消调度执行'")
    @PostMapping(value = "/executions/{id}/cancel", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:scheduler:execute')")
    public SchedulerExecutionVO cancel(@PathVariable UUID id,
                                       @Validated @RequestBody SchedulerExecutionActionFrom from) {
        return adminService.cancel(id, from);
    }

    @ULog("'解决未知调度执行'")
    @PostMapping(value = "/executions/{id}/resolve", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:scheduler:resolve') and hasRole('ROLE_DEV_OPS')")
    public SchedulerExecutionVO resolve(@PathVariable UUID id,
                                        @Validated @RequestBody SchedulerExecutionActionFrom from) {
        return adminService.resolve(id, from);
    }

    @ULog("'查询循环运行会话'")
    @GetMapping(value = "/loops", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:scheduler:query')")
    public IPage<SchedulerLoopRuntimeVO> loops(PageFrom page, SchedulerLoopPageFrom from) {
        return adminService.loops(page, from);
    }

    @ULog("'提交循环控制命令'")
    @PostMapping(value = "/loops/{jobId}/commands", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:scheduler:control')")
    public SchedulerControlCommandVO command(@PathVariable UUID jobId,
                                             @Validated @RequestBody SchedulerLoopCommandFrom from) {
        return adminService.command(jobId, from);
    }

    @ULog("'查询循环控制命令'")
    @GetMapping(value = "/loops/{jobId}/commands", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:scheduler:query')")
    public IPage<SchedulerControlCommandVO> commands(@PathVariable UUID jobId, PageFrom page) {
        return adminService.commands(jobId, page);
    }

    @ULog("'查询调度操作记录'")
    @GetMapping(value = "/jobs/{jobId}/operations", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:scheduler:query')")
    public IPage<SchedulerOperationVO> operations(@PathVariable UUID jobId, PageFrom page) {
        return adminService.operations(jobId, page);
    }

    @ULog("'查询循环错误聚合'")
    @GetMapping(value = "/loops/{jobId}/errors", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:scheduler:query')")
    public IPage<SchedulerLoopErrorVO> errors(@PathVariable UUID jobId, PageFrom page,
                                              SchedulerLoopErrorPageFrom from) {
        return adminService.errors(jobId, page, from);
    }
}
