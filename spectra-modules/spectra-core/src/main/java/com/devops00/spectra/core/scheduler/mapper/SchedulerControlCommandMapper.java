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
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerControlCommandEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.time.Instant;
import java.util.UUID;

/** 高频循环控制命令 Mapper。 */
@Mapper
public interface SchedulerControlCommandMapper extends BaseMapper<SchedulerControlCommandEntity> {

    /** 查询待应用命令；命令先落库再由控制器应用。 */
    List<SchedulerControlCommandEntity> selectPendingCommands(@Param("limit") int limit);

    /** 按幂等键读取已存在命令。 */
    SchedulerControlCommandEntity selectByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    /** 幂等插入控制命令，重复键返回 0。 */
    int insertIfAbsent(@Param("command") SchedulerControlCommandEntity command);

    /** 使用命令版本领取 REQUESTED 命令。 */
    int claimCommand(@Param("id") UUID id, @Param("expectedVersion") long expectedVersion);

    /** 将 APPLYING 命令标记为 APPLIED。 */
    int markApplied(@Param("id") UUID id, @Param("expectedVersion") long expectedVersion,
                    @Param("appliedAt") Instant appliedAt, @Param("resultCode") String resultCode,
                    @Param("resultMessage") String resultMessage);

    /** 将 APPLYING 命令标记为 FAILED 或 TIMEOUT。 */
    int markFinished(@Param("id") UUID id, @Param("expectedVersion") long expectedVersion,
                     @Param("status") String status, @Param("finishedAt") Instant finishedAt,
                     @Param("resultCode") String resultCode, @Param("resultMessage") String resultMessage);
}
