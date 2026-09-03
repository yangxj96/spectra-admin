package com.devops00.spectra.core.scheduler.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataExistException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.exception.SchedulerDatabaseUnavailableException;
import com.devops00.spectra.common.scheduler.ScheduledJobDescriptor;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.core.scheduler.javabean.converter.SchedulerAdminConverter;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerControlCommandEntity;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerExecutionEntity;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerJobEntity;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerLoopErrorEntity;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerLoopRuntimeEntity;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerOperationAuditEntity;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerCommandType;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDefinitionStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDesiredState;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerExecutionStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerOperationStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerOperationType;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerResolutionStatus;
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
import com.devops00.spectra.core.scheduler.mapper.SchedulerControlCommandMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerExecutionMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerJobMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerLoopErrorMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerLoopRuntimeMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerOperationAuditMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerOperationHistoryMapper;
import com.devops00.spectra.core.scheduler.service.ScheduledJobRegistry;
import com.devops00.spectra.core.scheduler.service.SchedulerAdminService;
import com.devops00.spectra.core.scheduler.service.SchedulerExecutionService;
import com.devops00.spectra.core.scheduler.service.SchedulerTimeZoneResolver;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/** 调度管理用例实现；数据库异常统一转换为 fail-closed 的调度错误。 */
@Service
@RequiredArgsConstructor
public class SchedulerAdminServiceImpl implements SchedulerAdminService {

    private static final Set<String> CONFIGURABLE_POLICY_KEYS = Set.of(
            "timeoutMs", "leaseDurationMs", "maxAttempts", "heartbeatIntervalMs", "errorLogIntervalMs");

    private final SchedulerJobMapper jobMapper;
    private final SchedulerExecutionMapper executionMapper;
    private final SchedulerLoopRuntimeMapper runtimeMapper;
    private final SchedulerControlCommandMapper commandMapper;
    private final SchedulerLoopErrorMapper errorMapper;
    private final ScheduledJobRegistry registry;
    private final SchedulerExecutionService executionService;
    private final com.devops00.spectra.core.scheduler.service.SchedulerControlCommandService commandService;
    private final SecurityContextAccessor securityContextAccessor;
    private final SchedulerTimeZoneResolver timeZoneResolver;
    private final SchedulerOperationHistoryMapper operationHistoryMapper;
    private final SchedulerOperationAuditMapper operationAuditMapper;

    private final Clock clock = Clock.systemUTC();

    @Override
    public List<SchedulerCatalogVO> catalog() {
        return database(() -> {
            jobMapper.selectCount(null);
            return registry.descriptors()
                    .stream()
                    .sorted(java.util.Comparator.comparing(ScheduledJobDescriptor::jobKey))
                    .map(SchedulerAdminConverter::toCatalog)
                    .toList();
        });
    }

    @Override
    public IPage<SchedulerJobVO> jobs(PageFrom page, SchedulerJobPageFrom from) {
        var safePage = page == null ? new PageFrom() : page;
        var safeFrom = from == null ? new SchedulerJobPageFrom() : from;
        return database(() -> jobMapper.selectPage(safePage.toPage(), new LambdaQueryWrapper<SchedulerJobEntity>()
                .like(safeFrom.getJobKey() != null && !safeFrom.getJobKey().isBlank(),
                        SchedulerJobEntity::getJobKey, safeFrom.getJobKey())
                .eq(safeFrom.getJobType() != null, SchedulerJobEntity::getJobType, safeFrom.getJobType())
                .eq(safeFrom.getDefinitionStatus() != null, SchedulerJobEntity::getDefinitionStatus,
                        safeFrom.getDefinitionStatus())
                .eq(safeFrom.getDesiredState() != null, SchedulerJobEntity::getDesiredState,
                        safeFrom.getDesiredState())
                .orderByAsc(SchedulerJobEntity::getJobKey)))
                .convert(SchedulerAdminConverter::toJob);
    }

    @Override
    @Transactional
    public SchedulerJobVO create(@Valid SchedulerJobSaveFrom from) {
        return database(() -> {
            var descriptor = requireOpsDescriptor(from.getJobKey());
            validateOperationContext(from.getIdempotencyKey(), from.getReason());
            validateDefinition(from, descriptor);
            var duplicate = jobMapper.selectOne(new LambdaQueryWrapper<SchedulerJobEntity>()
                    .eq(SchedulerJobEntity::getJobKey, from.getJobKey()));
            if (duplicate != null) {
                if (duplicate.getDefinitionStatus() == SchedulerDefinitionStatus.ARCHIVED) {
                    throw new DataExistException("任务已归档，请在任务列表中点击“重新注册”");
                }
                throw new DataExistException("任务已存在，不能重复注册");
            }
            var now = clock.instant();
            var job = toEntity(from, descriptor, now);
            jobMapper.insert(job);
            var saved = initializeNextFireIfNeeded(job);
            recordTaskOperation(saved.getId(), null, SchedulerOperationType.CREATE, from.getIdempotencyKey(),
                    from.getReason(), "CREATED", "调度任务已创建");
            return SchedulerAdminConverter.toJob(saved);
        });
    }

    @Override
    @Transactional
    public SchedulerJobVO update(UUID id, @Valid SchedulerJobSaveFrom from) {
        return database(() -> {
            var current = requireJob(id);
            requireOps(current);
            if (current.getDefinitionStatus() == SchedulerDefinitionStatus.ARCHIVED) {
                throw new IllegalStateException("已归档任务不能编辑，请先重新注册");
            }
            if (from.getVersion() == null || !from.getVersion().equals(current.getVersion())) {
                throw new IllegalStateException("任务定义版本已变化，请刷新后重试");
            }
            if (!current.getJobKey().equals(from.getJobKey())) {
                throw new IllegalArgumentException("任务键不可修改");
            }
            var descriptor = requireOpsDescriptor(current.getJobKey());
            validateOperationContext(from.getIdempotencyKey(), from.getReason());
            validateDefinition(from, descriptor);
            var updated = toEntity(from, descriptor, current.getUpdatedAt() == null ? clock.instant() : current.getUpdatedAt());
            updated.setId(id);
            if (jobMapper.updateDefinition(updated, from.getVersion()) != 1) {
                throw new IllegalStateException("任务定义并发更新失败，请刷新后重试");
            }
            var saved = initializeNextFireIfNeeded(requireJob(id));
            recordTaskOperation(id, null, SchedulerOperationType.UPDATE, from.getIdempotencyKey(), from.getReason(),
                    "UPDATED", "调度任务已更新");
            return SchedulerAdminConverter.toJob(saved);
        });
    }

    @Override
    @Transactional
    public SchedulerJobVO enable(UUID id, SchedulerOperationFrom from) {
        return changeDefinitionState(id, from, SchedulerDefinitionStatus.REGISTERED, SchedulerDesiredState.ENABLED,
                SchedulerOperationType.ENABLE);
    }

    @Override
    @Transactional
    public SchedulerJobVO disable(UUID id, SchedulerOperationFrom from) {
        return changeDefinitionState(id, from, SchedulerDefinitionStatus.REGISTERED, SchedulerDesiredState.DISABLED,
                SchedulerOperationType.DISABLE);
    }

    @Override
    @Transactional
    public SchedulerJobVO archive(UUID id, SchedulerOperationFrom from) {
        return changeDefinitionState(id, from, SchedulerDefinitionStatus.ARCHIVED, SchedulerDesiredState.DISABLED,
                SchedulerOperationType.ARCHIVE);
    }

    @Override
    @Transactional
    public SchedulerExecutionVO trigger(UUID jobId, SchedulerTriggerFrom from) {
        var result = database(() -> {
            validateOperationContext(from.getIdempotencyKey(), from.getReason());
            var job = requireJob(jobId);
            var descriptor = requireDiscreteDescriptor(job);
            if (job.getJobType() == ScheduledJobType.SYSTEM && !isDevOps()) {
                throw new AccessDeniedException("SYSTEM 任务只能由 DEV_OPS 受控触发");
            }
            if (!descriptor.supportedActions().contains("TRIGGER")) {
                throw new IllegalStateException("任务未开放手工触发能力");
            }
            var execution = executionService.triggerManual(jobId, from.getParameters(), from.getIdempotencyKey(),
                    clock.instant());
            recordTaskOperation(jobId, execution.getId(), SchedulerOperationType.TRIGGER, from.getIdempotencyKey(),
                    from.getReason(), "TRIGGERED", "手工执行已入队");
            return execution;
        });
        return SchedulerAdminConverter.toExecution(result);
    }

    @Override
    public IPage<SchedulerExecutionVO> executions(PageFrom page, SchedulerExecutionPageFrom from) {
        var safePage = page == null ? new PageFrom() : page;
        var safeFrom = from == null ? new SchedulerExecutionPageFrom() : from;
        return database(() -> executionMapper.selectPage(safePage.toPage(), new LambdaQueryWrapper<SchedulerExecutionEntity>()
                .eq(safeFrom.getJobId() != null, SchedulerExecutionEntity::getJobId, safeFrom.getJobId())
                .eq(safeFrom.getStatus() != null, SchedulerExecutionEntity::getStatus, safeFrom.getStatus())
                .like(safeFrom.getFireKey() != null && !safeFrom.getFireKey().isBlank(),
                        SchedulerExecutionEntity::getFireKey, safeFrom.getFireKey())
                .orderByDesc(SchedulerExecutionEntity::getScheduledAt)
                .orderByDesc(SchedulerExecutionEntity::getId)))
                .convert(SchedulerAdminConverter::toExecution);
    }

    @Override
    public SchedulerExecutionVO execution(UUID id) {
        return SchedulerAdminConverter.toExecution(database(() -> requireExecution(id)));
    }

    @Override
    @Transactional
    public SchedulerExecutionVO retry(UUID id, SchedulerExecutionActionFrom from) {
        var result = database(() -> {
            validateOperationContext(from.getIdempotencyKey(), from.getReason());
            var execution = requireExecution(id);
            var job = requireJob(execution.getJobId());
            if (job.getJobType() == ScheduledJobType.LOOP || job.getJobType() == ScheduledJobType.SYSTEM) {
                throw new IllegalStateException("SYSTEM/LOOP 任务不支持普通人工重试");
            }
            if (execution.getStatus() == SchedulerExecutionStatus.UNKNOWN && !isDevOps()) {
                throw new AccessDeniedException("UNKNOWN 结果重试属于 DEV_OPS 高风险操作");
            }
            if (execution.getVersion() == null || !execution.getVersion().equals(from.getVersion())) {
                throw new IllegalStateException("执行版本已变化，请刷新后重试");
            }
            var retry = executionService.retryExecution(id, currentUserId(), from.getReason(), from.getIdempotencyKey());
            recordTaskOperation(job.getId(), id, SchedulerOperationType.RETRY, from.getIdempotencyKey(),
                    from.getReason(), "RETRY_CREATED", "已创建新的重试执行");
            return retry;
        });
        return SchedulerAdminConverter.toExecution(result);
    }

    @Override
    @Transactional
    public SchedulerExecutionVO cancel(UUID id, SchedulerExecutionActionFrom from) {
        var result = database(() -> {
            validateOperationContext(from.getIdempotencyKey(), from.getReason());
            var execution = requireExecution(id);
            var job = requireJob(execution.getJobId());
            if (job.getJobType() == ScheduledJobType.LOOP) {
                throw new IllegalStateException("LOOP 任务必须使用控制命令");
            }
            if (job.getJobType() == ScheduledJobType.SYSTEM && !isDevOps()) {
                throw new AccessDeniedException("SYSTEM 执行取消属于 DEV_OPS 受控操作");
            }
            if (execution.getVersion() == null || !execution.getVersion().equals(from.getVersion())) {
                throw new IllegalStateException("执行版本已变化，请刷新后重试");
            }
            if (execution.getStatus() == SchedulerExecutionStatus.CANCELLED) {
                recordTaskOperation(job.getId(), id, SchedulerOperationType.CANCEL, from.getIdempotencyKey(),
                        from.getReason(), "ALREADY_CANCELLED", "执行已经是取消状态");
                return execution;
            }
            if (!executionService.cancel(id, from.getVersion(), from.getReason(), clock.instant())) {
                throw new IllegalStateException("执行已开始或版本已变化，不能取消");
            }
            var cancelled = requireExecution(id);
            recordTaskOperation(job.getId(), id, SchedulerOperationType.CANCEL, from.getIdempotencyKey(),
                    from.getReason(), "CANCELLED", "执行已取消");
            return cancelled;
        });
        return SchedulerAdminConverter.toExecution(result);
    }

    @Override
    @Transactional
    public SchedulerExecutionVO resolve(UUID id, SchedulerExecutionActionFrom from) {
        var result = database(() -> {
            validateOperationContext(from.getIdempotencyKey(), from.getReason());
            var execution = requireExecution(id);
            if (!isDevOps()) {
                throw new AccessDeniedException("UNKNOWN 结果解决属于 DEV_OPS 高风险操作");
            }
            if (execution.getStatus() != SchedulerExecutionStatus.UNKNOWN
                    || from.getResolutionStatus() == null
                    || (from.getResolutionStatus() != SchedulerResolutionStatus.CONFIRMED_SUCCESS
                            && from.getResolutionStatus() != SchedulerResolutionStatus.CONFIRMED_FAILED)) {
                throw new IllegalStateException("只能确认 UNKNOWN 执行为成功或失败");
            }
            if (!executionService.resolveUnknown(id, from.getVersion(),
                    new com.devops00.spectra.core.scheduler.javabean.domain.ExecutionResolution(
                            from.getResolutionStatus(), from.getReason(), currentUserId()))) {
                throw new IllegalStateException("UNKNOWN 执行版本已变化，请刷新后重试");
            }
            var resolved = requireExecution(id);
            recordTaskOperation(resolved.getJobId(), id, SchedulerOperationType.RESOLVE,
                    from.getIdempotencyKey(), from.getReason(), from.getResolutionStatus().name(),
                    "UNKNOWN 执行结果已人工登记");
            return resolved;
        });
        return SchedulerAdminConverter.toExecution(result);
    }

    @Override
    public IPage<SchedulerLoopRuntimeVO> loops(PageFrom page, SchedulerLoopPageFrom from) {
        var safePage = page == null ? new PageFrom() : page;
        var safeFrom = from == null ? new SchedulerLoopPageFrom() : from;
        return database(() -> runtimeMapper.selectPage(safePage.toPage(), new LambdaQueryWrapper<SchedulerLoopRuntimeEntity>()
                .eq(safeFrom.getJobId() != null, SchedulerLoopRuntimeEntity::getJobId, safeFrom.getJobId())
                .eq(safeFrom.getInstanceId() != null && !safeFrom.getInstanceId().isBlank(),
                        SchedulerLoopRuntimeEntity::getInstanceId, safeFrom.getInstanceId())
                .eq(safeFrom.getStatus() != null, SchedulerLoopRuntimeEntity::getStatus, safeFrom.getStatus())
                .orderByDesc(SchedulerLoopRuntimeEntity::getStartedAt)))
                .convert(SchedulerAdminConverter::toRuntime);
    }

    @Override
    public SchedulerControlCommandVO command(UUID jobId, SchedulerLoopCommandFrom from) {
        var command = database(() -> {
            validateOperationContext(from.getIdempotencyKey(), from.getReason());
            var job = requireJob(jobId);
            if (job.getJobType() != ScheduledJobType.LOOP) {
                throw new IllegalStateException("控制命令目标必须是 LOOP 任务");
            }
            if (isHighRisk(from.getCommandType()) && !isDevOps()) {
                throw new AccessDeniedException("高风险 LOOP 控制命令只能由 DEV_OPS 执行");
            }
            var requestedAt = clock.instant();
            return commandService.request(jobId, from.getCommandType(), from.getTargetRuntimeId(),
                    from.getTargetSessionKey(), from.getExpectedRuntimeVersion(), from.getIdempotencyKey(),
                    from.getReason(), currentUserId(), requestedAt, from.getDeadlineAt());
        });
        return SchedulerAdminConverter.toCommand(command);
    }

    @Override
    public IPage<SchedulerControlCommandVO> commands(UUID jobId, PageFrom page) {
        var safePage = page == null ? new PageFrom() : page;
        return database(() -> commandMapper.selectPage(safePage.toPage(),
                new LambdaQueryWrapper<SchedulerControlCommandEntity>()
                        .eq(SchedulerControlCommandEntity::getJobId, jobId)
                        .orderByDesc(SchedulerControlCommandEntity::getRequestedAt)
                        .orderByDesc(SchedulerControlCommandEntity::getId)))
                .convert(SchedulerAdminConverter::toCommand);
    }

    @Override
    public IPage<SchedulerOperationVO> operations(UUID jobId, PageFrom page) {
        var safePage = page == null ? new PageFrom() : page;
        return database(() -> {
            requireJob(jobId);
            return operationHistoryMapper.selectPage(safePage.toPage(), jobId);
        }).convert(SchedulerAdminConverter::toOperation);
    }

    @Override
    public IPage<SchedulerLoopErrorVO> errors(UUID jobId, PageFrom page, SchedulerLoopErrorPageFrom from) {
        var safePage = page == null ? new PageFrom() : page;
        var safeFrom = from == null ? new SchedulerLoopErrorPageFrom() : from;
        return database(() -> errorMapper.selectPage(safePage.toPage(), new LambdaQueryWrapper<SchedulerLoopErrorEntity>()
                .eq(SchedulerLoopErrorEntity::getJobId, jobId)
                .eq(safeFrom.getInstanceId() != null && !safeFrom.getInstanceId().isBlank(),
                        SchedulerLoopErrorEntity::getInstanceId, safeFrom.getInstanceId())
                .eq(safeFrom.getStatus() != null, SchedulerLoopErrorEntity::getStatus, safeFrom.getStatus())
                .orderByDesc(SchedulerLoopErrorEntity::getLastSeenAt)))
                .convert(SchedulerAdminConverter::toError);
    }

    private SchedulerJobVO changeDefinitionState(UUID id, SchedulerOperationFrom from,
                                                 SchedulerDefinitionStatus status,
                                                 SchedulerDesiredState desiredState,
                                                 SchedulerOperationType operationType) {
        return database(() -> {
            validateOperationContext(from.getIdempotencyKey(), from.getReason());
            var job = requireJob(id);
            requireOps(job);
            if (from.getVersion() == null || !from.getVersion().equals(job.getVersion())) {
                throw new IllegalStateException("任务定义版本已变化，请刷新后重试");
            }
            if (status == SchedulerDefinitionStatus.REGISTERED
                    && job.getDefinitionStatus() != SchedulerDefinitionStatus.REGISTERED
                    && job.getDefinitionStatus() != SchedulerDefinitionStatus.ARCHIVED) {
                throw new IllegalStateException("当前任务定义不可启用或停用");
            }
            if (jobMapper.updateDefinitionState(id, from.getVersion(), status.name(), desiredState.name()) != 1) {
                throw new IllegalStateException("任务定义状态更新失败，请刷新后重试");
            }
            var saved = initializeNextFireIfNeeded(requireJob(id));
            var actualOperation = operationType == SchedulerOperationType.ENABLE
                    && job.getDefinitionStatus() == SchedulerDefinitionStatus.ARCHIVED
                            ? SchedulerOperationType.REREGISTER
                            : operationType;
            recordTaskOperation(id, null, actualOperation, from.getIdempotencyKey(), from.getReason(),
                    actualOperation.name(), "任务定义状态已更新");
            return SchedulerAdminConverter.toJob(saved);
        });
    }

    private void recordTaskOperation(UUID jobId, UUID executionId, SchedulerOperationType operationType,
                                     String idempotencyKey, String reason, String resultCode,
                                     String resultMessage) {
        var now = clock.instant();
        var audit = new SchedulerOperationAuditEntity();
        audit.setJobId(jobId);
        audit.setExecutionId(executionId);
        audit.setOperationType(operationType);
        audit.setStatus(SchedulerOperationStatus.SUCCEEDED);
        audit.setIdempotencyKey(idempotencyKey);
        audit.setReason(reason);
        audit.setRequestedBy(currentUserId());
        audit.setRequestedAt(now);
        audit.setFinishedAt(now);
        audit.setResultCode(resultCode);
        audit.setResultMessage(resultMessage);
        if (operationAuditMapper.insert(audit) != 1) {
            throw new DataSaveException("调度操作审计记录保存失败");
        }
    }

    /** 注册并启用周期任务后立即补齐 next_fire_at，避免必须重启应用才能恢复调度。 */
    private SchedulerJobEntity initializeNextFireIfNeeded(SchedulerJobEntity job) {
        if (job.getDefinitionStatus() != SchedulerDefinitionStatus.REGISTERED
                || job.getDesiredState() != SchedulerDesiredState.ENABLED
                || job.getNextFireAt() != null
                || job.getScheduleKind() == ScheduledScheduleKind.MANUAL) {
            return job;
        }
        var nextFireAt = nextFireAt(job, clock.instant(), timeZoneResolver.resolve());
        if (nextFireAt == null) {
            return job;
        }
        if (jobMapper.advanceNextFire(job.getId(), job.getVersion(), nextFireAt) != 1) {
            throw new IllegalStateException("初始化调度任务下一次计划失败，请刷新后重试");
        }
        return requireJob(job.getId());
    }

    private static Instant nextFireAt(SchedulerJobEntity job, Instant now, ZoneId zone) {
        return switch (job.getScheduleKind()) {
            case CRON -> {
                var cron = CronExpression.parse(job.getCronExpression());
                var next = cron.next(ZonedDateTime.ofInstant(now, zone));
                yield next == null ? null : next.toInstant();
            }
            case FIXED_DELAY -> now.plusMillis(job.getInitialDelayMs() == null ? 0L : job.getInitialDelayMs());
            case MANUAL -> null;
        };
    }

    private SchedulerJobEntity toEntity(SchedulerJobSaveFrom from, ScheduledJobDescriptor descriptor, Instant now) {
        var job = new SchedulerJobEntity();
        job.setId(UUID.randomUUID());
        job.setJobKey(from.getJobKey().trim());
        job.setName(from.getName().trim());
        job.setModule(descriptor.module());
        job.setDescription(cleanText(from.getDescription()));
        job.setHandlerKey(descriptor.handlerKey());
        job.setJobType(descriptor.jobType());
        job.setRunScope(descriptor.runScope());
        job.setDefinitionStatus(SchedulerDefinitionStatus.REGISTERED);
        job.setDesiredState(SchedulerDesiredState.ENABLED);
        job.setScheduleKind(from.getScheduleKind());
        job.setCronExpression(cleanText(from.getCronExpression()));
        job.setFixedDelayMs(from.getFixedDelayMs());
        job.setInitialDelayMs(Objects.requireNonNullElse(from.getInitialDelayMs(), 0L));
        job.setNextFireAt(null);
        job.setMisfirePolicy(from.getMisfirePolicy());
        job.setConcurrencyPolicy(from.getConcurrencyPolicy());
        job.setExecutionPolicy(mergePolicy(descriptor.executionPolicy(), from.getExecutionPolicy()));
        job.setParameters(safeParameters(from.getParameters()));
        job.setRevision(1L);
        job.setCreatedAt(now);
        job.setUpdatedAt(now);
        job.setVersion(0L);
        return job;
    }

    private void validateDefinition(SchedulerJobSaveFrom from, ScheduledJobDescriptor descriptor) {
        if (from.getScheduleKind() != descriptor.scheduleKind()) {
            throw new IllegalArgumentException("任务只能使用注册描述中的调度类型");
        }
        switch (from.getScheduleKind()) {
            case CRON -> {
                if (from.getCronExpression() == null || from.getCronExpression().isBlank()) {
                    throw new IllegalArgumentException("Cron 表达式不能为空");
                }
                org.springframework.scheduling.support.CronExpression.parse(from.getCronExpression().trim());
                if (from.getFixedDelayMs() != null) {
                    throw new IllegalArgumentException("Cron 任务不能设置固定延迟");
                }
            }
            case FIXED_DELAY -> {
                if (from.getFixedDelayMs() == null || from.getFixedDelayMs() <= 0 || from.getCronExpression() != null) {
                    throw new IllegalArgumentException("固定延迟任务参数不合法");
                }
            }
            case MANUAL -> {
                if (from.getCronExpression() != null || from.getFixedDelayMs() != null) {
                    throw new IllegalArgumentException("手工任务不能设置周期表达式");
                }
            }
        }
        if (from.getInitialDelayMs() != null && from.getInitialDelayMs() < 0) {
            throw new IllegalArgumentException("初始延迟不能小于 0");
        }
        validateParameters(descriptor, from.getParameters());
        mergePolicy(descriptor.executionPolicy(), from.getExecutionPolicy());
    }

    private static Map<String, Object> mergePolicy(Map<String, Object> defaults, Map<String, Object> overrides) {
        var result = new java.util.LinkedHashMap<String, Object>();
        if (defaults != null) {
            result.putAll(defaults);
        }
        if (overrides != null) {
            for (var entry : overrides.entrySet()) {
                if (!CONFIGURABLE_POLICY_KEYS.contains(entry.getKey())) {
                    throw new IllegalArgumentException("不允许配置调度策略: " + entry.getKey());
                }
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return Map.copyOf(result);
    }

    private static void validateParameters(ScheduledJobDescriptor descriptor, Map<String, Object> parameters) {
        var actual = parameters == null ? Map.<String, Object>of() : parameters;
        if (actual.keySet().stream().anyMatch(key -> key == null || !descriptor.parameterSchema().containsKey(key))) {
            throw new IllegalArgumentException("任务参数包含未注册字段");
        }
        descriptor.parameterSchema().forEach((key, definition) -> {
            if (definition instanceof Map<?, ?> map
                    && Boolean.TRUE.equals(map.get("required"))
                    && !actual.containsKey(key)) {
                throw new IllegalArgumentException("缺少必填任务参数: " + key);
            }
        });
    }

    private static Map<String, Object> safeParameters(Map<String, Object> parameters) {
        var actual = parameters == null ? Map.<String, Object>of() : parameters;
        var result = new java.util.LinkedHashMap<String, Object>();
        actual.forEach((key, value) -> {
            if (key == null || key.isBlank() || key.length() > 100) {
                throw new IllegalArgumentException("任务参数名称不合法");
            }
            if (!(value == null
                    || value instanceof String
                    || value instanceof Number
                    || value instanceof Boolean
                    || value instanceof Map<?, ?>
                    || value instanceof List<?>)) {
                throw new IllegalArgumentException("任务参数值类型不受支持: " + key);
            }
            result.put(key, value);
        });
        return java.util.Collections.unmodifiableMap(result);
    }

    private static void validateOperationContext(String idempotencyKey, String reason) {
        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || idempotencyKey.length() > 300
                || reason == null
                || reason.isBlank()) {
            throw new IllegalArgumentException("管理操作必须提供有效幂等键和原因");
        }
    }

    private ScheduledJobDescriptor requireOpsDescriptor(String jobKey) {
        var descriptor = registry.find(jobKey).orElseThrow(() -> new IllegalStateException("任务处理器未注册: " + jobKey));
        if (descriptor.jobType() != ScheduledJobType.OPS) {
            throw new IllegalStateException("只有 OPS 任务允许在管理端配置");
        }
        return descriptor;
    }

    private ScheduledJobDescriptor requireDiscreteDescriptor(SchedulerJobEntity job) {
        if (job.getJobType() == ScheduledJobType.LOOP) {
            throw new IllegalStateException("LOOP 任务不支持离散触发");
        }
        var descriptor = registry.find(job.getJobKey()).orElseThrow(() -> new IllegalStateException("任务处理器未注册"));
        if (descriptor.jobType() != job.getJobType()) {
            throw new IllegalStateException("任务定义和代码注册类型不一致");
        }
        return descriptor;
    }

    private static void requireOps(SchedulerJobEntity job) {
        if (job.getJobType() != ScheduledJobType.OPS) {
            throw new IllegalStateException("只有 OPS 任务允许该管理操作");
        }
    }

    private SchedulerJobEntity requireJob(UUID id) {
        if (id == null) {
            throw new DataNotExistException("任务不存在");
        }
        var job = jobMapper.selectById(id);
        if (job == null) {
            throw new DataNotExistException("任务不存在: " + id);
        }
        return job;
    }

    private SchedulerExecutionEntity requireExecution(UUID id) {
        var execution = executionMapper.selectById(id);
        if (execution == null) {
            throw new DataNotExistException("执行不存在: " + id);
        }
        return execution;
    }

    private UUID currentUserId() {
        return securityContextAccessor == null ? null : securityContextAccessor.currentUserId();
    }

    private static boolean isHighRisk(SchedulerCommandType commandType) {
        return commandType == SchedulerCommandType.RESTART
                || commandType == SchedulerCommandType.FORCE_STOP
                || commandType == SchedulerCommandType.FORCE_RECLAIM;
    }

    private static boolean isDevOps() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority -> "ROLE_DEV_OPS".equals(authority.getAuthority()));
    }

    private static String cleanText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        var result = value.replaceAll("[\\r\\n\\t]+", " ").trim();
        return result.length() <= 2000 ? result : result.substring(0, 2000);
    }

    private <T> T database(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (SchedulerDatabaseUnavailableException exception) {
            throw exception;
        } catch (DataAccessException exception) {
            throw new SchedulerDatabaseUnavailableException(exception);
        }
    }
}
