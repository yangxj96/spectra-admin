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
import com.devops00.spectra.core.quartz.javabean.entity.QuartzJobExecutionHistoryEntity;
import com.devops00.spectra.core.quartz.service.QuartzJobExecutionHistoryService;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Quartz 管理服务的持久化编辑版本、API 时间和执行历史查询协作者。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/28
 */
@Component
public class QuartzJobManagementSupport {

    private final QuartzJobExecutionHistoryService historyService;
    private final TimeMapper timeMapper;
    private final JdbcTemplate jdbcTemplate;

    public QuartzJobManagementSupport(QuartzJobExecutionHistoryService historyService,
                                      TimeMapper timeMapper,
                                      JdbcTemplate jdbcTemplate) {
        this.historyService = historyService;
        this.timeMapper = timeMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 读取 Job 的管理端编辑版本。 */
    public Long editVersion(String schedulerName, String jobName, String jobGroup) {
        return jdbcTemplate.queryForObject("""
                SELECT EDIT_VERSION FROM spectra_quartz.QRTZ_JOB_DETAILS
                WHERE SCHED_NAME = ? AND JOB_NAME = ? AND JOB_GROUP = ?
                """, Long.class, schedulerName, jobName, jobGroup);
    }

    /** 按读取版本原子递增 Job 编辑版本；返回 0 表示版本已变化。 */
    public int incrementEditVersion(String schedulerName, String jobName, String jobGroup, long expectedVersion) {
        return jdbcTemplate.update("""
                UPDATE spectra_quartz.QRTZ_JOB_DETAILS
                SET EDIT_VERSION = EDIT_VERSION + 1
                WHERE SCHED_NAME = ? AND JOB_NAME = ? AND JOB_GROUP = ? AND EDIT_VERSION = ?
                """, schedulerName, jobName, jobGroup, expectedVersion);
    }

    /** 按当前用户时区转换 API 时间字段。 */
    public OffsetDateTime toOffsetDateTime(Date value) {
        return timeMapper.toOffsetDateTime(value);
    }

    /** 按当前用户时区转换 API 时间字段。 */
    public OffsetDateTime toOffsetDateTime(Instant value) {
        return timeMapper.toOffsetDateTime(value);
    }

    /** 查询执行历史分页。 */
    public IPage<QuartzJobExecutionHistoryEntity> pageHistory(
                                                              QuartzJobExecutionHistoryService.HistoryPageRequest request) {
        return historyService.page(request);
    }

    /** 查询单条执行历史。 */
    public Optional<QuartzJobExecutionHistoryEntity> findHistory(UUID id) {
        return historyService.find(id);
    }
}
