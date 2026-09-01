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

package com.devops00.spectra.core.scheduler.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerLoopRuntimeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** 高频循环运行会话 Mapper。 */
@Mapper
public interface SchedulerLoopRuntimeMapper extends BaseMapper<SchedulerLoopRuntimeEntity> {

    /** 查询任务当前仍可观测的运行会话。 */
    List<SchedulerLoopRuntimeEntity> selectByJobId(@Param("jobId") UUID jobId);

    /** 将 STARTING 会话以 CAS 方式切换为 RUNNING 并取得租约。 */
    int claimStartingRuntime(
                             @Param("id") UUID id,
                             @Param("expectedVersion") long expectedVersion,
                             @Param("instanceId") String instanceId,
                             @Param("heartbeatAt") Instant heartbeatAt,
                             @Param("leaseExpiresAt") Instant leaseExpiresAt);

    /** 使用版本和实例身份更新循环心跳与租约。 */
    int heartbeatRuntime(
                         @Param("id") UUID id,
                         @Param("expectedVersion") long expectedVersion,
                         @Param("instanceId") String instanceId,
                         @Param("heartbeatAt") Instant heartbeatAt,
                         @Param("leaseExpiresAt") Instant leaseExpiresAt);

    /** 以会话版本回写周期计数和运行状态。 */
    @SuppressWarnings("PMD.ExcessiveParameterList")
    int recordCycle(
                    @Param("id") UUID id,
                    @Param("expectedVersion") long expectedVersion,
                    @Param("instanceId") String instanceId,
                    @Param("status") String status,
                    @Param("cycleAt") Instant cycleAt,
                    @Param("progressAt") Instant progressAt,
                    @Param("processed") long processed,
                    @Param("failed") long failed,
                    @Param("consecutiveErrorCount") long consecutiveErrorCount,
                    @Param("errorCode") String errorCode,
                    @Param("errorMessage") String errorMessage);

    /** 用目标会话版本应用排空、停止或崩溃状态。 */
    int transitionRuntime(
                          @Param("id") UUID id,
                          @Param("expectedVersion") long expectedVersion,
                          @Param("instanceId") String instanceId,
                          @Param("status") String status,
                          @Param("reason") String reason,
                          @Param("drainDeadlineAt") Instant drainDeadlineAt,
                          @Param("stoppedAt") Instant stoppedAt);

    /** 仅在租约已过期且版本匹配时回收旧会话。 */
    int reclaimExpiredRuntime(
                              @Param("id") UUID id,
                              @Param("expectedVersion") long expectedVersion,
                              @Param("status") String status,
                              @Param("reason") String reason,
                              @Param("now") Instant now);
}
