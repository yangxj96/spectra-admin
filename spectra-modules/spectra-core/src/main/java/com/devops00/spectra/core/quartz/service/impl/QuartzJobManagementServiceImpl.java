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

package com.devops00.spectra.core.quartz.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.exception.BuiltinDataException;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.exception.SchedulerDatabaseUnavailableException;
import com.devops00.spectra.core.quartz.javabean.entity.QuartzJobExecutionHistoryEntity;
import com.devops00.spectra.core.quartz.service.QuartzJobExecutionHistoryService;
import com.devops00.spectra.core.quartz.catalog.QuartzJobCatalog;
import com.devops00.spectra.common.port.quartz.QuartzJobDefinition;
import com.devops00.spectra.common.port.quartz.QuartzTriggerTemplate;
import com.devops00.spectra.core.quartz.configuration.QuartzSchedulerLifecycle;
import com.devops00.spectra.core.quartz.javabean.from.QuartzHistoryQueryFrom;
import com.devops00.spectra.core.quartz.javabean.from.QuartzJobCreateFrom;
import com.devops00.spectra.core.quartz.javabean.from.QuartzJobUpdateFrom;
import com.devops00.spectra.core.quartz.javabean.from.QuartzTriggerFrom;
import com.devops00.spectra.core.quartz.javabean.vo.QuartzExecutionHistoryVO;
import com.devops00.spectra.core.quartz.javabean.vo.QuartzJobTypeVO;
import com.devops00.spectra.core.quartz.javabean.vo.QuartzJobVO;
import com.devops00.spectra.core.quartz.javabean.vo.QuartzTriggerVO;
import com.devops00.spectra.core.quartz.parameter.QuartzJobParameterValidator;
import com.devops00.spectra.core.quartz.parameter.VersionedJsonJobParameters;
import com.devops00.spectra.core.quartz.service.QuartzJobManagementService;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Quartz 管理应用服务；所有 Job 变更都经过代码白名单和唯一 Trigger 约束。 */
@Service
public class QuartzJobManagementServiceImpl implements QuartzJobManagementService {

    /** 内置 Job 使用的 Quartz 分组。 */
    public static final String BUILTIN_GROUP = "SPECTRA_BUILTIN";

    /** 管理端创建的普通 Job 使用的 Quartz 分组。 */
    public static final String ADMIN_GROUP = "SPECTRA_ADMIN";

    private static final String JOB_TYPE = "spectra.job.type";
    private static final String JOB_BUILT_IN = "spectra.job.built-in";
    private static final String PARAMETER_VERSION = "spectra.parameter.version";
    private static final String PARAMETER_JSON = "spectra.parameter.json";
    private static final String PARAMETER_SHA256 = "spectra.parameter.sha256";

    private final QuartzSchedulerLifecycle lifecycle;
    private final QuartzJobCatalog catalog;
    private final QuartzJobParameterValidator parameterValidator;
    private final QuartzJobExecutionHistoryService historyService;
    private final TimeMapper timeMapper;

    /**
     * 创建 Quartz 管理服务。
     *
     * @param lifecycle          Quartz 生命周期门面
     * @param catalog            代码白名单 Job 目录
     * @param parameterValidator 版本化 JSON 参数校验器
     * @param historyService     执行历史查询服务
     * @param timeMapper         按当前用户时区转换 API 时间字段
     */
    public QuartzJobManagementServiceImpl(QuartzSchedulerLifecycle lifecycle,
                                          QuartzJobCatalog catalog,
                                          QuartzJobParameterValidator parameterValidator,
                                          QuartzJobExecutionHistoryService historyService,
                                          TimeMapper timeMapper) {
        this.lifecycle = lifecycle;
        this.catalog = catalog;
        this.parameterValidator = parameterValidator;
        this.historyService = historyService;
        this.timeMapper = timeMapper;
    }

    /** {@inheritDoc} */
    @Override
    public List<QuartzJobTypeVO> jobTypes() {
        return catalog.definitions()
                .stream()
                .map(definition -> new QuartzJobTypeVO(definition.typeKey(), definition.displayName(),
                        definition.builtIn(), definition.jobClass().getName(), definition.parameterSchema().version(),
                        definition.parameterSchema().fields(), definition.parameterSchema().allowUnknownFields(),
                        List.of(QuartzTriggerTemplate.TriggerType.CRON, QuartzTriggerTemplate.TriggerType.SIMPLE)))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public IPage<QuartzJobVO> jobs(PageFrom page) {
        return withScheduler(scheduler -> {
            long current = page == null || page.getPageNum() == null
                    ? 1L
                    : Math.max(1L, page.getPageNum());
            long size = page == null || page.getPageSize() == null
                    ? 15L
                    : Math.min(200L, Math.max(1L, page.getPageSize()));
            List<JobKey> keys = scheduler.getJobKeys(GroupMatcher.anyJobGroup())
                    .stream()
                    .filter(this::managedGroup)
                    .sorted((left, right) -> left.toString().compareTo(right.toString()))
                    .toList();
            int from = (int) Math.min(keys.size(), Math.max(0L, (current - 1L) * size));
            int to = (int) Math.min(keys.size(), from + size);
            List<QuartzJobVO> records = keys.subList(from, to)
                    .stream()
                    .map(key -> toJob(scheduler, key))
                    .toList();
            return new Page<QuartzJobVO>(current, size, keys.size()).setRecords(records);
        });
    }

    /** {@inheritDoc} */
    @Override
    public QuartzJobVO job(String jobKey) {
        return withScheduler(scheduler -> toJob(scheduler, resolveJobKey(scheduler, jobKey)));
    }

    /** {@inheritDoc} */
    @Override
    public QuartzJobVO create(QuartzJobCreateFrom from) {
        Objects.requireNonNull(from, "from");
        return withScheduler(scheduler -> {
            QuartzJobDefinition definition = definition(from.getTypeKey());
            if (definition.builtIn()) {
                throw new BuiltinDataException("内置 Quartz Job 只能由系统注册");
            }
            VersionedJsonJobParameters parameters = validateParameters(definition, from.getParametersJson());
            JobKey jobKey = new JobKey(UUID.randomUUID().toString(), ADMIN_GROUP);
            JobDetail detail = jobDetail(definition, jobKey, from.getDisplayName(), parameters, false);
            TriggerKey triggerKey = triggerKey(jobKey);
            Trigger trigger = trigger(from.getTrigger(), triggerKey, jobKey);
            try {
                scheduler.addJob(detail, false);
                scheduler.scheduleJob(trigger);
                return toJob(scheduler, jobKey);
            } catch (SchedulerException exception) {
                throw schedulerUnavailable(exception);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public QuartzJobVO update(String jobKey, QuartzJobUpdateFrom from) {
        Objects.requireNonNull(from, "from");
        return withScheduler(scheduler -> {
            JobKey existingKey = resolveJobKey(scheduler, jobKey);
            JobDetail existing = getJobDetail(scheduler, existingKey);
            QuartzJobDefinition definition = definition(existing.getJobDataMap().getString(JOB_TYPE));
            boolean expectedBuiltIn = isBuiltIn(existingKey, existing);
            if (expectedBuiltIn
                    && (!definition.builtIn()
                            || !definition.jobClass().equals(existing.getJobClass()))) {
                throw new BuiltinDataException("内置 Quartz Job 的实现类和类型不可修改");
            }
            VersionedJsonJobParameters parameters = validateParameters(definition, from.getParametersJson());
            List<? extends Trigger> triggers = getTriggers(scheduler, existingKey);
            if (triggers.size() != 1) {
                throw new DataException("Quartz Job 必须绑定且只能绑定一个 Trigger");
            }
            JobDetail replacement = jobDetail(definition, existingKey, from.getDisplayName(), parameters,
                    expectedBuiltIn);
            Trigger replacementTrigger = trigger(from.getTrigger(), triggers.getFirst().getKey(), existingKey);
            try {
                scheduler.addJob(replacement, true);
                scheduler.rescheduleJob(triggers.getFirst().getKey(), replacementTrigger);
                return toJob(scheduler, existingKey);
            } catch (SchedulerException exception) {
                throw schedulerUnavailable(exception);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void delete(String jobKey) {
        withScheduler(scheduler -> {
            JobKey key = resolveJobKey(scheduler, jobKey);
            if (isBuiltIn(key, getJobDetail(scheduler, key))) {
                throw new BuiltinDataException("内置 Quartz Job 不允许删除");
            }
            try {
                for (Trigger trigger : getTriggers(scheduler, key)) {
                    scheduler.unscheduleJob(trigger.getKey());
                }
                if (!scheduler.deleteJob(key)) {
                    throw new DataSaveException("删除 Quartz Job 失败");
                }
                return null;
            } catch (SchedulerException exception) {
                throw schedulerUnavailable(exception);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void pause(String jobKey) {
        mutateJob(jobKey, Scheduler::pauseJob);
    }

    /** {@inheritDoc} */
    @Override
    public void resume(String jobKey) {
        mutateJob(jobKey, Scheduler::resumeJob);
    }

    /** {@inheritDoc} */
    @Override
    public void triggerNow(String jobKey) {
        withScheduler(scheduler -> {
            JobKey key = resolveJobKey(scheduler, jobKey);
            try {
                scheduler.triggerJob(key);
                return null;
            } catch (SchedulerException exception) {
                throw schedulerUnavailable(exception);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public QuartzTriggerVO triggerDetail(String triggerKey) {
        return withScheduler(scheduler -> {
            TriggerKey resolved = resolveTriggerKey(scheduler, triggerKey);
            try {
                Trigger trigger = scheduler.getTrigger(resolved);
                if (trigger == null) {
                    throw new DataNotExistException("Quartz Trigger 不存在");
                }
                return toTrigger(scheduler, trigger);
            } catch (SchedulerException exception) {
                throw schedulerUnavailable(exception);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public IPage<QuartzExecutionHistoryVO> executionHistory(PageFrom page, QuartzHistoryQueryFrom from) {
        QuartzHistoryQueryFrom query = from == null ? new QuartzHistoryQueryFrom() : from;
        long current = page == null || page.getPageNum() == null ? 1L : page.getPageNum();
        long size = page == null || page.getPageSize() == null ? 15L : page.getPageSize();
        IPage<QuartzJobExecutionHistoryEntity> source = historyService.page(current, size,
                normalize(query.getJobKey()), normalize(query.getTriggerKey()), query.getStatus(),
                parseTime(query.getFrom(), "历史开始时间"), parseTime(query.getTo(), "历史结束时间"));
        Page<QuartzExecutionHistoryVO> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        return result.setRecords(source.getRecords().stream().map(this::toHistory).toList());
    }

    /** {@inheritDoc} */
    @Override
    public QuartzExecutionHistoryVO executionHistory(UUID id) {
        return historyService.find(id)
                .map(this::toHistory)
                .orElseThrow(() -> new DataNotExistException("Quartz 执行历史不存在"));
    }

    private <T> T withScheduler(SchedulerCallback<T> callback) {
        if (!lifecycle.isReady()) {
            throw new SchedulerDatabaseUnavailableException(new IllegalStateException("Quartz Scheduler 未就绪"));
        }
        try {
            return callback.call(lifecycle.scheduler());
        } catch (DataException exception) {
            throw exception;
        } catch (SchedulerException exception) {
            throw schedulerUnavailable(exception);
        }
    }

    private void mutateJob(String jobKey, SchedulerMutation mutation) {
        withScheduler(scheduler -> {
            mutation.apply(scheduler, resolveJobKey(scheduler, jobKey));
            return null;
        });
    }

    private QuartzJobDefinition definition(String typeKey) {
        return catalog.find(normalize(typeKey))
                .orElseThrow(() -> new DataException("Quartz Job 类型未注册"));
    }

    private JobDetail jobDetail(QuartzJobDefinition definition, JobKey jobKey, String displayName,
                                VersionedJsonJobParameters parameters, boolean builtIn) {
        JobDataMap data = new JobDataMap();
        data.put(JOB_TYPE, definition.typeKey());
        data.put(JOB_BUILT_IN, builtIn);
        data.put(PARAMETER_VERSION, parameters.version());
        data.put(PARAMETER_JSON, parameters.json());
        data.put(PARAMETER_SHA256, sha256(parameters.json()));
        return JobBuilder.newJob(definition.jobClass())
                .withIdentity(jobKey)
                .withDescription(displayName.trim())
                .usingJobData(data)
                .storeDurably()
                .requestRecovery(false)
                .build();
    }

    private Trigger trigger(QuartzTriggerFrom from, TriggerKey triggerKey, JobKey jobKey) {
        if (from == null || from.getTriggerType() == null) {
            throw new DataSaveException("Quartz Trigger 类型不能为空");
        }
        QuartzTriggerTemplate.MisfirePolicy misfire = from.getMisfireInstruction();
        if (from.getTriggerType() == QuartzTriggerTemplate.TriggerType.CRON) {
            if (normalize(from.getCronExpression()) == null || normalize(from.getTimeZone()) == null) {
                throw new DataSaveException("Cron Trigger 必须提供表达式和 IANA 时区");
            }
            try {
                return QuartzTriggerTemplate.cron(from.getCronExpression().trim(), ZoneId.of(from.getTimeZone().trim()),
                        misfire).build(triggerKey, jobKey, from.getStartAt());
            } catch (DateTimeException exception) {
                throw new DataSaveException("Cron Trigger 时区无效", exception);
            } catch (IllegalArgumentException exception) {
                throw new DataSaveException("Cron Trigger 表达式无效", exception);
            }
        }
        if (from.isOneShot()) {
            return QuartzTriggerTemplate.oneShot(misfire).build(triggerKey, jobKey, from.getStartAt());
        }
        if (from.getIntervalMs() == null || from.getIntervalMs() <= 0L) {
            throw new DataSaveException("Simple Trigger 间隔必须大于 0");
        }
        return QuartzTriggerTemplate.fixedInterval(Duration.ofMillis(from.getIntervalMs()), misfire)
                .build(triggerKey, jobKey, from.getStartAt());
    }

    private VersionedJsonJobParameters validateParameters(QuartzJobDefinition definition, String json) {
        try {
            return parameterValidator.validate(definition.parameterSchema(), json);
        } catch (IllegalArgumentException exception) {
            throw new DataSaveException("Quartz Job 参数无效", exception);
        }
    }

    private List<? extends Trigger> getTriggers(Scheduler scheduler, JobKey jobKey) throws SchedulerException {
        return scheduler.getTriggersOfJob(jobKey);
    }

    private JobKey resolveJobKey(Scheduler scheduler, String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new DataNotExistException("Quartz JobKey 不能为空");
        }
        Set<JobKey> keys = callScheduler(() -> scheduler.getJobKeys(GroupMatcher.anyJobGroup()));
        return keys.stream()
                .filter(this::managedGroup)
                .filter(key -> key.toString().equals(normalized) || key.getName().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new DataNotExistException("Quartz Job 不存在"));
    }

    private TriggerKey resolveTriggerKey(Scheduler scheduler, String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new DataNotExistException("Quartz TriggerKey 不能为空");
        }
        try {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
                if (!managedGroup(jobKey)) {
                    continue;
                }
                for (Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
                    if (trigger.getKey().toString().equals(normalized)
                            || trigger.getKey().getName().equals(normalized)) {
                        return trigger.getKey();
                    }
                }
            }
            throw new DataNotExistException("Quartz Trigger 不存在");
        } catch (SchedulerException exception) {
            throw schedulerUnavailable(exception);
        }
    }

    private JobDetail getJobDetail(Scheduler scheduler, JobKey jobKey) {
        try {
            JobDetail detail = scheduler.getJobDetail(jobKey);
            if (detail == null) {
                throw new DataNotExistException("Quartz Job 不存在");
            }
            return detail;
        } catch (SchedulerException exception) {
            throw schedulerUnavailable(exception);
        }
    }

    private QuartzJobVO toJob(Scheduler scheduler, JobKey jobKey) {
        JobDetail detail = getJobDetail(scheduler, jobKey);
        List<? extends Trigger> triggers = callScheduler(() -> getTriggers(scheduler, jobKey));
        if (triggers.size() > 1) {
            throw new DataException("Quartz Job 存在多个 Trigger，无法按一对一契约展示");
        }
        String type = normalize(detail.getJobDataMap().getString(JOB_TYPE));
        QuartzTriggerVO trigger = triggers.isEmpty() ? null : toTrigger(scheduler, triggers.getFirst());
        return new QuartzJobVO(jobKey.toString(), jobKey.getGroup(), detail.getDescription(), type,
                isBuiltIn(jobKey, detail), detail.getJobClass().getSimpleName(),
                detail.getJobDataMap().getString(PARAMETER_VERSION),
                detail.getJobDataMap().getString(PARAMETER_JSON), trigger);
    }

    private QuartzTriggerVO toTrigger(Scheduler scheduler, Trigger trigger) {
        String state = callScheduler(() -> scheduler.getTriggerState(trigger.getKey()).name());
        if (trigger instanceof CronTrigger cron) {
            return new QuartzTriggerVO(trigger.getKey().toString(), QuartzTriggerTemplate.TriggerType.CRON, state,
                    cron.getCronExpression(), null, false, cron.getTimeZone().getID(),
                    cronMisfire(cron.getMisfireInstruction()), local(trigger.getStartTime()),
                    local(trigger.getPreviousFireTime()), local(trigger.getNextFireTime()));
        }
        if (trigger instanceof SimpleTrigger simple) {
            return new QuartzTriggerVO(trigger.getKey().toString(), QuartzTriggerTemplate.TriggerType.SIMPLE, state,
                    null, simple.getRepeatInterval(), simple.getRepeatCount() == 0, null,
                    simpleMisfire(simple.getMisfireInstruction()), local(trigger.getStartTime()),
                    local(trigger.getPreviousFireTime()), local(trigger.getNextFireTime()));
        }
        throw new DataException("Quartz Trigger 类型不受支持");
    }

    private QuartzExecutionHistoryVO toHistory(QuartzJobExecutionHistoryEntity source) {
        return new QuartzExecutionHistoryVO(source.getId(), source.getFireInstanceId(), source.getJobKey(),
                source.getTriggerKey(), source.getJobType(), source.getJobClassName(), source.getTriggerType(),
                source.getStatus(), local(source.getScheduledFireAt()), local(source.getActualFireAt()),
                local(source.getStartedAt()), local(source.getFinishedAt()), source.getDurationMs(),
                source.getSchedulerInstance(), source.getCorrelationId(), source.getParameterVersion(),
                source.getParameterSha256(), source.getResultSummary(), source.getErrorCode(),
                source.getErrorMessage());
    }

    private boolean isBuiltIn(JobKey key, JobDetail detail) {
        return BUILTIN_GROUP.equals(key.getGroup()) || detail.getJobDataMap().getBoolean(JOB_BUILT_IN);
    }

    private boolean managedGroup(JobKey key) {
        return BUILTIN_GROUP.equals(key.getGroup()) || ADMIN_GROUP.equals(key.getGroup());
    }

    private TriggerKey triggerKey(JobKey jobKey) {
        return new TriggerKey(jobKey.getName() + ".trigger", jobKey.getGroup());
    }

    private static QuartzTriggerTemplate.MisfirePolicy cronMisfire(int instruction) {
        return instruction == 2
                ? QuartzTriggerTemplate.MisfirePolicy.DO_NOTHING
                : instruction == -1
                        ? QuartzTriggerTemplate.MisfirePolicy.NEXT_WITH_REMAINING_COUNT
                        : QuartzTriggerTemplate.MisfirePolicy.FIRE_ONCE_NOW;
    }

    private static QuartzTriggerTemplate.MisfirePolicy simpleMisfire(int instruction) {
        return instruction == 1
                ? QuartzTriggerTemplate.MisfirePolicy.FIRE_ONCE_NOW
                : QuartzTriggerTemplate.MisfirePolicy.NEXT_WITH_REMAINING_COUNT;
    }

    private LocalDateTime local(Date value) {
        return timeMapper.toLocalDateTime(value);
    }

    private LocalDateTime local(Instant value) {
        return timeMapper.toLocalDateTime(value);
    }

    private static Instant parseTime(String value, String label) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        try {
            return Instant.parse(normalized);
        } catch (RuntimeException exception) {
            throw new DataSaveException(label + "必须是合法 ISO-8601 时间", exception);
        }
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 不可用", exception);
        }
    }

    private static SchedulerDatabaseUnavailableException schedulerUnavailable(SchedulerException exception) {
        return new SchedulerDatabaseUnavailableException(exception);
    }

    private static <T> T callScheduler(CheckedSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (SchedulerException exception) {
            throw schedulerUnavailable(exception);
        }
    }

    @FunctionalInterface
    private interface SchedulerCallback<T> {

        T call(Scheduler scheduler) throws SchedulerException;
    }

    @FunctionalInterface
    private interface SchedulerMutation {

        void apply(Scheduler scheduler, JobKey jobKey) throws SchedulerException;
    }

    @FunctionalInterface
    private interface CheckedSupplier<T> {

        T get() throws SchedulerException;
    }
}
