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
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerExecutionEntity;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerResolutionStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 调度离散执行 Mapper。 */
@Mapper
public interface SchedulerExecutionMapper extends BaseMapper<SchedulerExecutionEntity> {

    /** 以 fire_key 幂等插入一条 QUEUED 执行。 */
    int insertIfAbsent(@Param("execution") SchedulerExecutionEntity execution);

    /** 按 fire_key 读取幂等执行记录。 */
    SchedulerExecutionEntity selectByFireKey(@Param("fireKey") String fireKey);

    /** 查询到期且尚未领取的执行。 */
    List<SchedulerExecutionEntity> selectClaimable(
                                                   @Param("now") Instant now,
                                                   @Param("limit") int limit);

    /** 将租约已过期且仍在运行的执行标记为 UNKNOWN，禁止旧实例继续覆盖结果。 */
    int markExpiredAsUnknown(
                             @Param("now") Instant now,
                             @Param("errorCode") String errorCode,
                             @Param("errorMessage") String errorMessage);

    /** 将到期重试记录重新放回队列。 */
    int requeueExecution(
                         @Param("id") UUID id,
                         @Param("expectedVersion") long expectedVersion,
                         @Param("now") Instant now);

    /** 在持有租约期间记录结果摘要并推进版本。 */
    int finishExecution(
                        @Param("id") UUID id,
                        @Param("expectedVersion") long expectedVersion,
                        @Param("instanceId") String instanceId,
                        @Param("status") String status,
                        @Param("errorCode") String errorCode,
                        @Param("errorMessage") String errorMessage,
                        @Param("resultSummary") Map<String, Object> resultSummary,
                        @Param("nextRetryAt") Instant nextRetryAt,
                        @Param("finishedAt") Instant finishedAt);

    /** 仅更新 UNKNOWN 执行的独立人工解决状态，不覆盖原始 status。 */
    int resolveUnknown(
                       @Param("id") UUID id,
                       @Param("expectedVersion") long expectedVersion,
                       @Param("resolutionStatus") SchedulerResolutionStatus resolutionStatus,
                       @Param("reason") String reason,
                       @Param("resolvedBy") UUID resolvedBy,
                       @Param("resolvedAt") Instant resolvedAt);

    /** 使用执行版本和租约领取队列记录。 */
    int claimExecution(
                       @Param("id") UUID id,
                       @Param("expectedVersion") long expectedVersion,
                       @Param("instanceId") String instanceId,
                       @Param("lockedAt") Instant lockedAt,
                       @Param("leaseExpiresAt") Instant leaseExpiresAt);

    /** 使用持有者、版本和有效租约续租。 */
    int heartbeatExecution(
                           @Param("id") UUID id,
                           @Param("expectedVersion") long expectedVersion,
                           @Param("instanceId") String instanceId,
                           @Param("heartbeatAt") Instant heartbeatAt,
                           @Param("leaseExpiresAt") Instant leaseExpiresAt);

    /** 使用持有者和版本回写最终状态。 */
    int completeExecution(
                          @Param("id") UUID id,
                          @Param("expectedVersion") long expectedVersion,
                          @Param("instanceId") String instanceId,
                          @Param("status") String status,
                          @Param("finishedAt") Instant finishedAt);

    /** 仅允许取消尚未开始的执行。 */
    int cancelExecution(
                        @Param("id") UUID id,
                        @Param("expectedVersion") long expectedVersion,
                        @Param("reason") String reason,
                        @Param("finishedAt") Instant finishedAt);
}
