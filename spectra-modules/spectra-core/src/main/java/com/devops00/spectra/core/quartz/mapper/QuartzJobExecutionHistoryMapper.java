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

package com.devops00.spectra.core.quartz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.quartz.javabean.entity.QuartzJobExecutionHistoryEntity;
import com.devops00.spectra.core.quartz.javabean.enums.QuartzExecutionHistoryStatus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.UUID;

/** Quartz 执行历史持久化 Mapper。 */
@Mapper
public interface QuartzJobExecutionHistoryMapper extends BaseMapper<QuartzJobExecutionHistoryEntity> {

    /** 按执行开始时间将超时运行记录标记为 ABANDONED。 */
    int abandonExpired(@Param("cutoff") Instant cutoff,
                       @Param("finishedAt") Instant finishedAt,
                       @Param("errorCode") String errorCode,
                       @Param("errorMessage") String errorMessage,
                       @Param("limit") int limit);

    /** 删除已超过保留期限的终态历史记录。 */
    int deleteExpired(@Param("cutoff") Instant cutoff, @Param("limit") int limit);

    /** 分页查询执行历史，不返回参数原文。 */
    IPage<QuartzJobExecutionHistoryEntity> selectHistoryPage(
                                                             IPage<QuartzJobExecutionHistoryEntity> page,
                                                             @Param("jobKey") String jobKey,
                                                             @Param("triggerKey") String triggerKey,
                                                             @Param("status") QuartzExecutionHistoryStatus status,
                                                             @Param("startedAtFrom") Instant startedAtFrom,
                                                             @Param("startedAtTo") Instant startedAtTo);

    /** 按 Quartz fire instance 查询已经存在的历史主键，支持重复回调幂等处理。 */
    UUID selectIdByFireInstanceId(@Param("fireInstanceId") String fireInstanceId);

    /** 使用受保护的 ID 更新一次执行的最终结果。 */
    int finish(@Param("id") UUID id,
               @Param("status") QuartzExecutionHistoryStatus status,
               @Param("finishedAt") Instant finishedAt,
               @Param("durationMs") Long durationMs,
               @Param("resultSummary") String resultSummary,
               @Param("errorCode") String errorCode,
               @Param("errorMessage") String errorMessage);
}
