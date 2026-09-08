package com.devops00.spectra.core.scheduler.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
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

    /**
     * 返回调度器支持的任务类型和能力清单。
     *
     * @return 返回符合查询条件的调度器能力项列表；无匹配时返回空列表，不返回 null。
     */
    List<SchedulerCatalogVO> catalog();

    /**
     * 分页查询已登记的调度任务。
     *
     * @param page 调度任务列表的页码、页大小及排序字段。
     * @param from 任务键、任务类型、定义状态和期望运行状态等调度任务筛选条件。
     * @return 返回按分页条件查询的调度任务分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<SchedulerJobVO> jobs(com.devops00.spectra.common.base.javabean.from.PageFrom page,
                               SchedulerJobPageFrom from);

    /**
     * 创建或保存调度数据，并返回接口约定的对象或回执。
     *
     * @param from 任务键、名称、调度类型、Cron/延迟参数、错过策略、并发策略及幂等信息。
     * @return 返回已登记调度任务的标识、调度规则和当前状态；校验或写入失败时抛出业务异常，不返回 null。
     */
    SchedulerJobVO create(SchedulerJobSaveFrom from);

    /**
     * 更新调度数据或状态；调用方必须满足接口声明的权限前置条件。
     *
     * @param id   待更新调度任务的唯一标识。
     * @param from 任务键、名称、调度类型、Cron/延迟参数、错过策略、并发策略及版本信息。
     * @return 返回更新后的调度任务，包含最新调度规则和版本；版本冲突或写入失败时抛出业务异常，不返回 null。
     */
    SchedulerJobVO update(UUID id, SchedulerJobSaveFrom from);

    /**
     * 变更调度状态，并校验允许的状态迁移。
     *
     * @param id   待启用调度任务的唯一标识。
     * @param from 操作版本、幂等键和状态变更原因，用于校验并记录本次启用操作。
     * @return 返回已切换为启用状态的调度任务；状态迁移或持久化失败时抛出业务异常，不返回 null。
     */
    SchedulerJobVO enable(UUID id, SchedulerOperationFrom from);

    /**
     * 变更调度状态，并校验允许的状态迁移。
     *
     * @param id   待禁用调度任务的唯一标识。
     * @param from 操作版本、幂等键和状态变更原因，用于校验并记录本次禁用操作。
     * @return 返回已切换为禁用状态的调度任务；状态迁移或持久化失败时抛出业务异常，不返回 null。
     */
    SchedulerJobVO disable(UUID id, SchedulerOperationFrom from);

    /**
     * 删除、撤销或归档调度数据；重复调用按接口约定保持幂等。
     *
     * @param id   待归档调度任务的唯一标识。
     * @param from 操作版本、幂等键和归档原因，用于校验并记录本次归档操作。
     * @return 返回已切换为归档状态的调度任务；状态迁移或持久化失败时抛出业务异常，不返回 null。
     */
    SchedulerJobVO archive(UUID id, SchedulerOperationFrom from);

    /**
     * 按任务标识触发一次调度执行。
     *
     * @param jobId 要查询控制命令的调度任务唯一标识。
     * @param from  本次执行覆盖用参数、幂等键和触发原因。
     * @return 返回新建调度执行记录，包含执行标识和初始状态；任务不可执行或写入失败时抛出业务异常，不返回 null。
     */
    SchedulerExecutionVO trigger(UUID jobId, com.devops00.spectra.core.scheduler.javabean.from.SchedulerTriggerFrom from);

    /**
     * 分页查询调度执行记录。
     *
     * @param page 执行记录列表的页码、页大小及排序字段。
     * @param from 任务 ID、执行状态和触发键等执行记录筛选条件。
     * @return 返回按分页条件查询的调度执行记录分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<SchedulerExecutionVO> executions(com.devops00.spectra.common.base.javabean.from.PageFrom page,
                                           SchedulerExecutionPageFrom from);

    /**
     * 查询单次调度执行详情。
     *
     * @param id 要读取详情的调度执行记录唯一标识。
     * @return 返回符合条件的调度执行记录详情；记录不存在或当前用户不可见时抛出业务异常，不返回 null。
     */
    SchedulerExecutionVO execution(UUID id);

    /**
     * 重试失败的调度执行。
     *
     * @param id   待重试调度执行记录的唯一标识。
     * @param from 执行记录版本、幂等键、重试原因和目标处理状态。
     * @return 返回重试后的调度执行记录，包含新的尝试状态和错误信息；不允许重试或写入失败时抛出业务异常，不返回 null。
     */
    SchedulerExecutionVO retry(UUID id, SchedulerExecutionActionFrom from);

    /**
     * 删除、撤销或归档调度数据；重复调用按接口约定保持幂等。
     *
     * @param id   待取消调度执行记录的唯一标识。
     * @param from 执行记录版本、幂等键、取消原因和目标处理状态。
     * @return 返回已切换为取消状态的调度执行记录；状态迁移或写入失败时抛出业务异常，不返回 null。
     */
    SchedulerExecutionVO cancel(UUID id, SchedulerExecutionActionFrom from);

    /**
     * 查询调度数据或详情，并按接口契约处理不存在和不可用情况。
     *
     * @param id   要解析的调度执行记录唯一标识。
     * @param from 执行记录版本、幂等键、处理原因和目标解析状态。
     * @return 返回包含最终处理状态和解析说明的调度执行记录；记录不存在或状态不允许解析时抛出业务异常，不返回 null。
     */
    SchedulerExecutionVO resolve(UUID id, SchedulerExecutionActionFrom from);

    /**
     * 查询任务执行循环或重入状态。
     *
     * @param page 循环运行记录列表的页码、页大小及排序字段。
     * @param from 任务 ID、实例 ID 和运行状态等循环运行记录筛选条件。
     * @return 返回按分页条件查询的调度执行循环状态分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<SchedulerLoopRuntimeVO> loops(com.devops00.spectra.common.base.javabean.from.PageFrom page,
                                        SchedulerLoopPageFrom from);

    /**
     * 生成单个调度控制命令。
     *
     * @param jobId 要生成控制命令的调度任务唯一标识。
     * @param from  命令类型、目标运行实例/会话、期望版本、幂等键、原因和截止时间。
     * @return 返回包含命令标识、目标实例和执行期限的调度控制命令；命令校验或写入失败时抛出业务异常，不返回 null。
     */
    SchedulerControlCommandVO command(UUID jobId, SchedulerLoopCommandFrom from);

    /**
     * 批量生成调度控制命令。
     *
     * @param jobId 调度任务的唯一标识。
     * @param page  分页条件，包含页码、页大小和排序字段。
     * @return 返回该任务的控制命令分页，包含命令类型、目标和状态；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<SchedulerControlCommandVO> commands(UUID jobId,
                                              com.devops00.spectra.common.base.javabean.from.PageFrom page);

    /**
     * 查询调度运维操作记录。
     *
     * @param jobId 要查询运维操作记录的调度任务唯一标识。
     * @param page  分页条件，包含页码、页大小和排序字段。
     * @return 返回该任务的启停、归档、触发等运维操作分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<SchedulerOperationVO> operations(UUID jobId,
                                           com.devops00.spectra.common.base.javabean.from.PageFrom page);

    /**
     * 查询调度失败记录。
     *
     * @param jobId 要查询失败记录的调度任务唯一标识。
     * @param page  失败记录列表的页码、页大小及排序字段。
     * @param from  执行实例、错误状态等失败记录筛选条件。
     * @return 返回该任务的循环失败记录分页，包含错误阶段、原因和处理状态；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<SchedulerLoopErrorVO> errors(UUID jobId, com.devops00.spectra.common.base.javabean.from.PageFrom page,
                                       SchedulerLoopErrorPageFrom from);
}
