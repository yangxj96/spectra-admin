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
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerLoopErrorEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;
import java.time.Duration;

/** 高频循环错误聚合 Mapper。 */
@Mapper
public interface SchedulerLoopErrorMapper extends BaseMapper<SchedulerLoopErrorEntity> {

    /** 按任务、实例和指纹查询错误聚合。 */
    SchedulerLoopErrorEntity selectOpenByFingerprint(
                                                     @Param("jobId") UUID jobId,
                                                     @Param("instanceId") String instanceId,
                                                     @Param("errorFingerprint") String errorFingerprint);

    /** 原子写入或更新错误聚合，并返回更新后的聚合记录。 */
    SchedulerLoopErrorEntity upsertOccurrence(
                                              @Param("error") SchedulerLoopErrorEntity error,
                                              @Param("logIntervalMs") long logIntervalMs);
}
