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

package com.devops00.spectra.core.scheduler.service;

import com.devops00.spectra.core.scheduler.mapper.SchedulerExecutionMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/** 离散执行记录的租约 CAS 操作。 */
@Service
public class ExecutionLeaseService {

    private final SchedulerExecutionMapper executionMapper;

    public ExecutionLeaseService(SchedulerExecutionMapper executionMapper) {
        this.executionMapper = executionMapper;
    }

    /** 领取一条 QUEUED 执行并返回新的执行版本。 */
    public boolean claim(SchedulerExecutionLease lease) {
        return executionMapper.claimExecution(lease.executionId(), lease.expectedVersion(), lease.instanceId(),
                lease.lockedAt(), lease.leaseExpiresAt()) == 1;
    }

    /** 只有持有相同实例和版本的执行器才能续租。 */
    public boolean heartbeat(SchedulerExecutionLease lease) {
        return executionMapper.heartbeatExecution(lease.executionId(), lease.expectedVersion(), lease.instanceId(),
                lease.lockedAt(), lease.leaseExpiresAt()) == 1;
    }

    /** 只有持有相同实例和版本且租约未过期的执行器才能完成。 */
    public boolean complete(UUID executionId, long expectedVersion, String instanceId, String status, Instant finishedAt) {
        return executionMapper.completeExecution(executionId, expectedVersion, instanceId, status, finishedAt) == 1;
    }

    /** 计算租约截止时间。 */
    public static Instant leaseExpiresAt(Instant now, Duration duration) {
        if (now == null || duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("租约时间必须大于 0");
        }
        return now.plus(duration);
    }

    /** 一次租约操作所需的不可变参数。 */
    public record SchedulerExecutionLease(UUID executionId,
                                          long expectedVersion,
                                          String instanceId,
                                          Instant lockedAt,
                                          Instant leaseExpiresAt) {

        public SchedulerExecutionLease {
            if (executionId == null
                    || instanceId == null
                    || instanceId.isBlank()
                    || lockedAt == null
                    || leaseExpiresAt == null
                    || !leaseExpiresAt.isAfter(lockedAt)) {
                throw new IllegalArgumentException("执行租约参数不完整");
            }
        }
    }
}
