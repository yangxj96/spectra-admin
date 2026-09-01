package com.devops00.spectra.core.scheduler.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataExistException;
import com.devops00.spectra.common.scheduler.ScheduledEffectType;
import com.devops00.spectra.common.scheduler.ScheduledJobDescriptor;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerControlCommandEntity;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerJobEntity;
import com.devops00.spectra.core.scheduler.javabean.domain.SchedulerOperationHistoryRow;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerJobSaveFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerJobPageFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerOperationFrom;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerExecutionEntity;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerOperationAuditEntity;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerConcurrencyPolicy;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerCommandStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerCommandType;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDefinitionStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDesiredState;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerMisfirePolicy;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerOperationSource;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerOperationStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerOperationType;
import com.devops00.spectra.core.scheduler.mapper.SchedulerControlCommandMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerExecutionMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerJobMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerLoopErrorMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerLoopRuntimeMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerOperationAuditMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerOperationHistoryMapper;
import com.devops00.spectra.core.scheduler.service.impl.SchedulerAdminServiceImpl;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 管理用例的任务类型边界和数据库 fail-closed 契约。 */
class SchedulerAdminServiceTest {

    @Test
    void systemDescriptorCannotBeConfiguredFromAdmin() {
        var jobMapper = mock(SchedulerJobMapper.class);
        var registry = mock(ScheduledJobRegistry.class);
        when(registry.find("system.task")).thenReturn(java.util.Optional.of(descriptor(ScheduledJobType.SYSTEM)));
        var service = service(jobMapper, registry);

        assertThrows(IllegalStateException.class, () -> service.create(save("system.task")));
    }

    @Test
    void loopDescriptorCannotBeConfiguredFromAdmin() {
        var jobMapper = mock(SchedulerJobMapper.class);
        var registry = mock(ScheduledJobRegistry.class);
        when(registry.find("loop.task")).thenReturn(java.util.Optional.of(descriptor(ScheduledJobType.LOOP)));
        var service = service(jobMapper, registry);

        assertThrows(IllegalStateException.class, () -> service.create(save("loop.task")));
    }

    @Test
    void databaseFailureIsConvertedToSchedulerUnavailable() {
        var jobMapper = mock(SchedulerJobMapper.class);
        when(jobMapper.selectPage(any(), any())).thenThrow(new DataAccessResourceFailureException("database down"));
        var service = service(jobMapper, mock(ScheduledJobRegistry.class));

        assertThrows(com.devops00.spectra.common.exception.SchedulerDatabaseUnavailableException.class,
                () -> service.jobs(null, new SchedulerJobPageFrom()));
    }

    @Test
    void catalogRequiresDatabaseAvailability() {
        var jobMapper = mock(SchedulerJobMapper.class);
        when(jobMapper.selectCount(any())).thenThrow(new DataAccessResourceFailureException("database down"));
        var service = service(jobMapper, mock(ScheduledJobRegistry.class));

        assertThrows(com.devops00.spectra.common.exception.SchedulerDatabaseUnavailableException.class,
                service::catalog);
    }

    @Test
    void controlCommandHistoryReturnsReasonAndResultForLoopJob() {
        var jobMapper = mock(SchedulerJobMapper.class);
        var commandMapper = mock(SchedulerControlCommandMapper.class);
        var jobId = UUID.randomUUID();
        var command = new SchedulerControlCommandEntity();
        command.setId(UUID.randomUUID());
        command.setJobId(jobId);
        command.setCommandType(SchedulerCommandType.DRAIN_STOP);
        command.setStatus(SchedulerCommandStatus.APPLIED);
        command.setReason("发布窗口前排空 Worker");
        command.setResultCode("DRAINING");
        command.setResultMessage("循环已进入排空状态");
        command.setRequestedAt(Instant.parse("2026-08-26T02:00:00Z"));
        var page = new Page<SchedulerControlCommandEntity>(1, 15);
        page.setRecords(List.of(command));
        when(commandMapper.selectPage(any(), any())).thenReturn(page);
        var service = service(jobMapper, mock(ScheduledJobRegistry.class), mock(SchedulerTimeZoneResolver.class), commandMapper);

        var result = service.commands(jobId, new PageFrom());

        assertEquals(1, result.getRecords().size());
        assertEquals("发布窗口前排空 Worker", result.getRecords().getFirst().reason());
        assertEquals("循环已进入排空状态", result.getRecords().getFirst().resultMessage());
        var wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(commandMapper).selectPage(any(), wrapperCaptor.capture());
        assertTrue(wrapperCaptor.getValue().getExpression().getNormal().size() > 0);
        assertEquals(2, wrapperCaptor.getValue().getExpression().getOrderBy().size());
    }

    @Test
    void operationHistoryCombinesTaskAuditAndLoopCommands() {
        var jobMapper = mock(SchedulerJobMapper.class);
        var historyMapper = mock(SchedulerOperationHistoryMapper.class);
        var jobId = UUID.randomUUID();
        var taskOperation = new SchedulerOperationHistoryRow(
                UUID.randomUUID(), jobId, UUID.randomUUID(), SchedulerOperationType.TRIGGER,
                SchedulerOperationStatus.SUCCEEDED, "manual-1", "发布后验证", UUID.randomUUID(),
                Instant.parse("2026-08-26T02:00:00Z"), Instant.parse("2026-08-26T02:00:01Z"),
                "TRIGGERED", "手工执行已入队", SchedulerOperationSource.TASK);
        var loopCommand = new SchedulerOperationHistoryRow(
                UUID.randomUUID(), jobId, null, SchedulerOperationType.DRAIN_STOP,
                SchedulerOperationStatus.APPLIED, "drain-1", "发布窗口前排空 Worker", UUID.randomUUID(),
                Instant.parse("2026-08-26T02:01:00Z"), Instant.parse("2026-08-26T02:01:02Z"),
                "DRAINING", "循环已进入排空状态", SchedulerOperationSource.LOOP_COMMAND);
        var page = new Page<SchedulerOperationHistoryRow>(1, 15);
        page.setRecords(List.of(taskOperation, loopCommand));
        when(jobMapper.selectById(jobId)).thenReturn(job(jobId, SchedulerDefinitionStatus.REGISTERED,
                SchedulerDesiredState.ENABLED, 1L, null));
        when(historyMapper.selectPage(any(), eq(jobId))).thenReturn(page);
        var service = service(jobMapper, mock(ScheduledJobRegistry.class), mock(SchedulerTimeZoneResolver.class),
                mock(SchedulerControlCommandMapper.class), historyMapper);

        var result = service.operations(jobId, new PageFrom());

        assertEquals(2, result.getRecords().size());
        assertEquals(SchedulerOperationType.TRIGGER, result.getRecords().getFirst().operationType());
        assertEquals("发布后验证", result.getRecords().getFirst().reason());
        assertEquals(SchedulerOperationSource.LOOP_COMMAND, result.getRecords().get(1).source());
        assertEquals("循环已进入排空状态", result.getRecords().get(1).resultMessage());
        verify(historyMapper).selectPage(any(), eq(jobId));
    }

    @Test
    void manualTriggerWritesTaskOperationAuditWithReason() {
        var jobMapper = mock(SchedulerJobMapper.class);
        var executionService = mock(SchedulerExecutionService.class);
        var auditMapper = mock(SchedulerOperationAuditMapper.class);
        var registry = mock(ScheduledJobRegistry.class);
        var jobId = UUID.randomUUID();
        var executionId = UUID.randomUUID();
        var execution = new SchedulerExecutionEntity();
        execution.setId(executionId);
        execution.setJobId(jobId);
        when(jobMapper.selectById(jobId)).thenReturn(
                job(jobId, SchedulerDefinitionStatus.REGISTERED, SchedulerDesiredState.ENABLED, 1L, null));
        when(registry.find("ops.task")).thenReturn(java.util.Optional.of(descriptor(ScheduledJobType.OPS,
                Set.of("VIEW", "TRIGGER"))));
        when(executionService.triggerManual(eq(jobId), eq(Map.of()), eq("manual-trigger"), any(Instant.class)))
                .thenReturn(execution);
        var service = service(jobMapper, registry, mock(SchedulerTimeZoneResolver.class),
                mock(SchedulerControlCommandMapper.class), mock(SchedulerOperationHistoryMapper.class), auditMapper,
                executionService);

        service.trigger(jobId, new com.devops00.spectra.core.scheduler.javabean.from.SchedulerTriggerFrom(
                Map.of(), "manual-trigger", "发布后验证"));

        var captor = ArgumentCaptor.forClass(SchedulerOperationAuditEntity.class);
        verify(auditMapper).insert(captor.capture());
        assertEquals(SchedulerOperationType.TRIGGER, captor.getValue().getOperationType());
        assertEquals(executionId, captor.getValue().getExecutionId());
        assertEquals("发布后验证", captor.getValue().getReason());
        assertEquals(SchedulerOperationStatus.SUCCEEDED, captor.getValue().getStatus());
    }

    @Test
    void archivedOpsJobCanBeReregistered() {
        var jobMapper = mock(SchedulerJobMapper.class);
        var jobId = UUID.randomUUID();
        var archived = job(jobId, SchedulerDefinitionStatus.ARCHIVED, SchedulerDesiredState.DISABLED, 3L, null);
        var registered = job(jobId, SchedulerDefinitionStatus.REGISTERED, SchedulerDesiredState.ENABLED, 4L, null);
        var scheduled = job(jobId, SchedulerDefinitionStatus.REGISTERED, SchedulerDesiredState.ENABLED, 5L,
                Instant.parse("2026-08-26T01:00:01Z"));
        var timeZoneResolver = mock(SchedulerTimeZoneResolver.class);
        when(timeZoneResolver.resolve()).thenReturn(ZoneId.of("UTC"));
        when(jobMapper.selectById(jobId)).thenReturn(archived, registered, scheduled);
        when(jobMapper.updateDefinitionState(jobId, 3L, "REGISTERED", "ENABLED")).thenReturn(1);
        when(jobMapper.advanceNextFire(eq(jobId), eq(4L), any(Instant.class))).thenReturn(1);
        var service = service(jobMapper, mock(ScheduledJobRegistry.class), timeZoneResolver);

        var result = service.enable(jobId, new SchedulerOperationFrom(3L, "register-test", "恢复任务"));

        assertEquals(SchedulerDefinitionStatus.REGISTERED, result.definitionStatus());
        assertEquals(SchedulerDesiredState.ENABLED, result.desiredState());
        verify(jobMapper).updateDefinitionState(jobId, 3L, "REGISTERED", "ENABLED");
        verify(jobMapper).advanceNextFire(eq(jobId), eq(4L), any(Instant.class));
    }

    @Test
    void archivedOpsJobCannotBeEditedBeforeReregistration() {
        var jobMapper = mock(SchedulerJobMapper.class);
        var registry = mock(ScheduledJobRegistry.class);
        var jobId = UUID.randomUUID();
        when(jobMapper.selectById(jobId)).thenReturn(
                job(jobId, SchedulerDefinitionStatus.ARCHIVED, SchedulerDesiredState.DISABLED, 3L, null));
        when(registry.find("ops.task")).thenReturn(java.util.Optional.of(descriptor(ScheduledJobType.OPS)));
        var service = service(jobMapper, registry);

        var exception = assertThrows(IllegalStateException.class, () -> service.update(jobId,
                new SchedulerJobSaveFrom("ops.task", "task", null, ScheduledScheduleKind.FIXED_DELAY,
                        null, 1000L, 0L, SchedulerMisfirePolicy.SKIP, SchedulerConcurrencyPolicy.FORBID,
                        Map.of(), Map.of(), 3L, "update-test", "编辑任务")));

        assertEquals("已归档任务不能编辑，请先重新注册", exception.getMessage());
    }

    @Test
    void registeredDisabledOpsJobCanBeEdited() {
        var jobMapper = mock(SchedulerJobMapper.class);
        var registry = mock(ScheduledJobRegistry.class);
        var jobId = UUID.randomUUID();
        var disabled = job(jobId, SchedulerDefinitionStatus.REGISTERED, SchedulerDesiredState.DISABLED, 3L, null);
        var saved = job(jobId, SchedulerDefinitionStatus.REGISTERED, SchedulerDesiredState.DISABLED, 4L, null);
        when(jobMapper.selectById(jobId)).thenReturn(disabled, saved);
        when(registry.find("ops.task")).thenReturn(java.util.Optional.of(descriptor(ScheduledJobType.OPS)));
        when(jobMapper.updateDefinition(any(), eq(3L))).thenReturn(1);
        var service = service(jobMapper, registry);

        var result = service.update(jobId,
                new SchedulerJobSaveFrom("ops.task", "task", null, ScheduledScheduleKind.FIXED_DELAY,
                        null, 1000L, 0L, SchedulerMisfirePolicy.SKIP, SchedulerConcurrencyPolicy.FORBID,
                        Map.of(), Map.of(), 3L, "update-test", "编辑任务"));

        assertEquals(SchedulerDefinitionStatus.REGISTERED, result.definitionStatus());
        assertEquals(SchedulerDesiredState.DISABLED, result.desiredState());
        verify(jobMapper).updateDefinition(any(), eq(3L));
    }

    @Test
    void creatingArchivedExistingOpsJobExplainsReregistration() {
        var jobMapper = mock(SchedulerJobMapper.class);
        var registry = mock(ScheduledJobRegistry.class);
        when(registry.find("ops.task")).thenReturn(java.util.Optional.of(descriptor(ScheduledJobType.OPS)));
        when(jobMapper.selectOne(any())).thenReturn(
                job(UUID.randomUUID(), SchedulerDefinitionStatus.ARCHIVED, SchedulerDesiredState.DISABLED, 3L, null));
        var service = service(jobMapper, registry);

        var exception = assertThrows(DataExistException.class, () -> service.create(save("ops.task")));

        assertEquals("任务已归档，请在任务列表中点击“重新注册”", exception.getMessage());
    }

    private static SchedulerAdminServiceImpl service(SchedulerJobMapper jobMapper, ScheduledJobRegistry registry) {
        return service(jobMapper, registry, mock(SchedulerTimeZoneResolver.class));
    }

    private static SchedulerAdminServiceImpl service(SchedulerJobMapper jobMapper, ScheduledJobRegistry registry,
                                                     SchedulerTimeZoneResolver timeZoneResolver) {
        return service(jobMapper, registry, timeZoneResolver, mock(SchedulerControlCommandMapper.class));
    }

    private static SchedulerAdminServiceImpl service(SchedulerJobMapper jobMapper, ScheduledJobRegistry registry,
                                                     SchedulerTimeZoneResolver timeZoneResolver,
                                                     SchedulerControlCommandMapper commandMapper) {
        return service(jobMapper, registry, timeZoneResolver, commandMapper, mock(SchedulerOperationHistoryMapper.class));
    }

    private static SchedulerAdminServiceImpl service(SchedulerJobMapper jobMapper, ScheduledJobRegistry registry,
                                                     SchedulerTimeZoneResolver timeZoneResolver,
                                                     SchedulerControlCommandMapper commandMapper,
                                                     SchedulerOperationHistoryMapper historyMapper) {
        return service(jobMapper, registry, timeZoneResolver, commandMapper, historyMapper,
                mock(SchedulerOperationAuditMapper.class), mock(SchedulerExecutionService.class));
    }

    private static SchedulerAdminServiceImpl service(SchedulerJobMapper jobMapper, ScheduledJobRegistry registry,
                                                     SchedulerTimeZoneResolver timeZoneResolver,
                                                     SchedulerControlCommandMapper commandMapper,
                                                     SchedulerOperationHistoryMapper historyMapper,
                                                     SchedulerOperationAuditMapper auditMapper,
                                                     SchedulerExecutionService executionService) {
        when(auditMapper.insert(any(SchedulerOperationAuditEntity.class))).thenReturn(1);
        return new SchedulerAdminServiceImpl(jobMapper, mock(SchedulerExecutionMapper.class),
                mock(SchedulerLoopRuntimeMapper.class), commandMapper,
                mock(SchedulerLoopErrorMapper.class), registry, executionService,
                mock(SchedulerControlCommandService.class), mock(SecurityContextAccessor.class), timeZoneResolver,
                historyMapper, auditMapper);
    }

    private static SchedulerJobSaveFrom save(String key) {
        return new SchedulerJobSaveFrom(key, "task", null, ScheduledScheduleKind.FIXED_DELAY,
                null, 1000L, 0L, SchedulerMisfirePolicy.SKIP, SchedulerConcurrencyPolicy.FORBID,
                Map.of(), Map.of(), null, "test-operation", "test reason");
    }

    private static SchedulerJobEntity job(UUID id, SchedulerDefinitionStatus definitionStatus,
                                          SchedulerDesiredState desiredState, long version, Instant nextFireAt) {
        var job = new SchedulerJobEntity();
        job.setId(id);
        job.setJobKey("ops.task");
        job.setName("task");
        job.setModule("test");
        job.setHandlerKey("handler");
        job.setJobType(ScheduledJobType.OPS);
        job.setRunScope(ScheduledRunScope.SINGLETON);
        job.setDefinitionStatus(definitionStatus);
        job.setDesiredState(desiredState);
        job.setScheduleKind(ScheduledScheduleKind.FIXED_DELAY);
        job.setFixedDelayMs(1000L);
        job.setInitialDelayMs(0L);
        job.setNextFireAt(nextFireAt);
        job.setMisfirePolicy(SchedulerMisfirePolicy.SKIP);
        job.setConcurrencyPolicy(SchedulerConcurrencyPolicy.FORBID);
        job.setExecutionPolicy(Map.of());
        job.setParameters(Map.of());
        job.setRevision(1L);
        job.setVersion(version);
        return job;
    }

    private static ScheduledJobDescriptor descriptor(ScheduledJobType type) {
        return descriptor(type, Set.of("VIEW"));
    }

    private static ScheduledJobDescriptor descriptor(ScheduledJobType type, Set<String> supportedActions) {
        return ScheduledJobDescriptor.builder()
                .jobKey(type.name().toLowerCase() + ".task")
                .handlerKey("handler")
                .name("task")
                .module("test")
                .jobType(type)
                .runScope(type == ScheduledJobType.LOOP ? ScheduledRunScope.PER_INSTANCE : ScheduledRunScope.SINGLETON)
                .scheduleKind(ScheduledScheduleKind.FIXED_DELAY)
                .effectType(ScheduledEffectType.DB_ONLY)
                .parameterSchema(Map.of())
                .supportedActions(supportedActions)
                .executionPolicy(Map.of())
                .build();
    }
}
