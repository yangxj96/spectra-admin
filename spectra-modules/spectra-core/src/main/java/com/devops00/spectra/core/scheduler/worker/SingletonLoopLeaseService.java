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

package com.devops00.spectra.core.scheduler.worker;

import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerJobEntity;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerLoopRuntimeEntity;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerRuntimeStatus;
import com.devops00.spectra.core.scheduler.mapper.SchedulerJobMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerLoopRuntimeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** 循环运行会话创建和 PostgreSQL 租约持有。 */
@Service
public class SingletonLoopLeaseService {

    private static final Duration DEFAULT_LEASE = Duration.ofSeconds(30);
    private final SchedulerJobMapper jobMapper;
    private final SchedulerLoopRuntimeMapper runtimeMapper;
    private final Clock clock;

    @Autowired
    public SingletonLoopLeaseService(SchedulerJobMapper jobMapper, SchedulerLoopRuntimeMapper runtimeMapper) {
        this(jobMapper, runtimeMapper, Clock.systemUTC());
    }

    SingletonLoopLeaseService(SchedulerJobMapper jobMapper, SchedulerLoopRuntimeMapper runtimeMapper, Clock clock) {
        this.jobMapper = jobMapper;
        this.runtimeMapper = runtimeMapper;
        this.clock = clock;
    }

    /**
     * 在任务定义行锁内检查旧会话、回收过期租约并创建新会话。
     * <p>同一任务的两个实例不会同时通过这段数据库事务。</p>
     */
    @Transactional
    public Optional<SchedulerLoopRuntimeEntity> ensureSession(SchedulerJobEntity requestedJob,
                                                              String instanceId,
                                                              Instant requestedNow) {
        if (requestedJob == null || requestedJob.getId() == null || instanceId == null || instanceId.isBlank()) {
            throw new IllegalArgumentException("循环会话缺少任务或实例标识");
        }
        var now = requestedNow == null ? clock.instant() : requestedNow;
        var job = jobMapper.selectByIdForUpdate(requestedJob.getId());
        if (job == null) {
            throw new IllegalStateException("循环任务定义不存在: " + requestedJob.getId());
        }
        var sessions = runtimeMapper.selectByJobId(job.getId());
        for (var session : sessions) {
            if (!isActive(session)) {
                continue;
            }
            if (isLive(session, now)) {
                if (session.getInstanceId().equals(instanceId)
                        || job.getRunScope() == ScheduledRunScope.SINGLETON) {
                    return session.getInstanceId().equals(instanceId)
                            ? Optional.of(session)
                            : Optional.empty();
                }
                continue;
            }
            if (runtimeMapper.reclaimExpiredRuntime(session.getId(), session.getVersion(),
                    SchedulerRuntimeStatus.CRASHED.name(), "循环租约已过期", now) != 1) {
                return Optional.empty();
            }
        }

        var runtime = newRuntime(job, instanceId, now);
        if (runtimeMapper.insert(runtime) != 1) {
            throw new IllegalStateException("创建循环运行会话失败: " + job.getJobKey());
        }
        var leaseExpiresAt = now.plus(leaseDuration(job));
        if (runtimeMapper.claimStartingRuntime(runtime.getId(), 0L, instanceId, now, leaseExpiresAt) != 1) {
            throw new IllegalStateException("领取循环运行会话租约失败: " + job.getJobKey());
        }
        runtime.setStatus(SchedulerRuntimeStatus.RUNNING);
        runtime.setLastHeartbeatAt(now);
        runtime.setLeaseExpiresAt(leaseExpiresAt);
        runtime.setVersion(1L);
        return Optional.of(runtime);
    }

    /**
     * 应用正常停止时，停止当前实例持有的所有循环会话。
     * <p>使用会话版本和实例身份 CAS，避免误停其他实例或已经被并发回收的会话。</p>
     */
    @Transactional
    public void stopOwnedSessions(String instanceId, Instant requestedNow) {
        if (instanceId == null || instanceId.isBlank()) {
            throw new IllegalArgumentException("循环会话缺少实例标识");
        }
        var now = requestedNow == null ? clock.instant() : requestedNow;
        for (var job : jobMapper.selectList(null)) {
            if (job.getJobType() != ScheduledJobType.LOOP) {
                continue;
            }
            for (var runtime : runtimeMapper.selectByJobId(job.getId())) {
                if (runtime != null
                        && instanceId.equals(runtime.getInstanceId())
                        && isActive(runtime)) {
                    runtimeMapper.transitionRuntime(runtime.getId(), runtime.getVersion(), instanceId,
                            SchedulerRuntimeStatus.STOPPED.name(), "应用实例正常停止", null, now);
                }
            }
        }
    }

    /** 计算当前任务的循环租约时长。 */
    public Duration leaseDuration(SchedulerJobEntity job) {
        Object value = job == null || job.getExecutionPolicy() == null
                ? null
                : job.getExecutionPolicy().get("leaseDurationMs");
        if (value == null) {
            return DEFAULT_LEASE;
        }
        try {
            var millis = Long.parseLong(String.valueOf(value));
            if (millis <= 0) {
                throw new IllegalArgumentException("leaseDurationMs 必须大于 0");
            }
            return Duration.ofMillis(millis);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("leaseDurationMs 必须是正整数毫秒数", exception);
        }
    }

    private static SchedulerLoopRuntimeEntity newRuntime(SchedulerJobEntity job, String instanceId, Instant now) {
        var runtime = new SchedulerLoopRuntimeEntity();
        runtime.setId(UUID.randomUUID());
        runtime.setJobId(job.getId());
        runtime.setSessionKey(job.getJobKey() + ":" + instanceId + ":" + UUID.randomUUID());
        runtime.setInstanceId(instanceId.trim());
        runtime.setStatus(SchedulerRuntimeStatus.STARTING);
        runtime.setStartedAt(now);
        runtime.setTotalCycles(0L);
        runtime.setTotalProcessed(0L);
        runtime.setTotalFailed(0L);
        runtime.setConsecutiveErrorCount(0L);
        runtime.setCreatedAt(now);
        runtime.setUpdatedAt(now);
        runtime.setVersion(0L);
        return runtime;
    }

    private static boolean isActive(SchedulerLoopRuntimeEntity runtime) {
        return runtime != null
                && runtime.getStatus() != null
                && switch (runtime.getStatus()) {
                    case STARTING, RUNNING, DEGRADED, DRAINING -> true;
                    case STOPPED, CRASHED, UNKNOWN -> false;
                };
    }

    private static boolean isLive(SchedulerLoopRuntimeEntity runtime, Instant now) {
        return runtime.getLeaseExpiresAt() != null && runtime.getLeaseExpiresAt().isAfter(now);
    }
}
