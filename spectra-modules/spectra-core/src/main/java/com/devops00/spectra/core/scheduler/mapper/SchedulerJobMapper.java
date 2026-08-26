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
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerJobEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** 调度任务定义 Mapper。 */
@Mapper
public interface SchedulerJobMapper extends BaseMapper<SchedulerJobEntity> {

    /** 查询到期且可派发的离散任务。 */
    List<SchedulerJobEntity> selectDueJobs(@Param("now") Instant now, @Param("limit") int limit);

    /** 使用任务版本推进下一次计划时间。 */
    int advanceNextFire(
                        @Param("id") UUID id,
                        @Param("expectedVersion") long expectedVersion,
                        @Param("nextFireAt") Instant nextFireAt);

    /** 锁定任务定义行，串行化同一任务的循环会话创建和期望状态变更。 */
    SchedulerJobEntity selectByIdForUpdate(@Param("id") UUID id);

    /** 使用任务版本更新循环任务的期望状态。 */
    int updateDesiredState(
                           @Param("id") UUID id,
                           @Param("expectedVersion") long expectedVersion,
                           @Param("desiredState") String desiredState);

    /** 使用版本更新任务定义状态和期望状态。 */
    int updateDefinitionState(
                              @Param("id") UUID id,
                              @Param("expectedVersion") long expectedVersion,
                              @Param("definitionStatus") String definitionStatus,
                              @Param("desiredState") String desiredState);

    /** 使用版本更新 OPS 任务的可配置字段。 */
    int updateDefinition(
                         @Param("job") SchedulerJobEntity job,
                         @Param("expectedVersion") long expectedVersion);
}
