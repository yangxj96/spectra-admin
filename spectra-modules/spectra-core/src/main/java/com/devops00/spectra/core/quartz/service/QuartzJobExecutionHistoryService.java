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

package com.devops00.spectra.core.quartz.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.core.quartz.javabean.entity.QuartzJobExecutionHistoryEntity;
import com.devops00.spectra.core.quartz.javabean.enums.QuartzExecutionHistoryStatus;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Quartz 执行历史写入、收敛和查询服务。 */
public interface QuartzJobExecutionHistoryService {

    /**
     * 插入一条 RUNNING 执行历史，并把 Quartz 的任务、Trigger、节点和参数摘要固定到本次 fire。
     *
     * @param context Quartz 提供的本次执行上下文；必须包含 JobDetail、Trigger 和 fireInstanceId
     */
    void started(JobExecutionContext context);

    /**
     * 按 fireInstanceId 将执行历史更新为成功或失败，并记录完成时间、耗时和安全错误摘要。
     *
     * @param context   与 started 使用同一 fireInstanceId 的 Quartz 执行上下文
     * @param exception 任务未抛出异常时传 null，抛出异常时用于决定 FAILED 状态；原始消息不会直接暴露给客户端
     */
    void completed(JobExecutionContext context, JobExecutionException exception);

    /**
     * 为被 Quartz veto、尚未进入 Job.execute 的 fire 创建 VETOED 终态记录。
     *
     * @param context 被 Quartz 拒绝执行的本次执行上下文
     */
    void vetoed(JobExecutionContext context);

    /**
     * 将超时 RUNNING 记录标记为 ABANDONED，并分批物理删除超过保留期的终态记录。
     *
     * @return 本轮标记或删除的历史记录总行数；没有需要处理的记录时返回 0
     */
    int cleanup();

    /**
     * 按任务、Trigger、状态和开始时间过滤执行历史，并返回稳定排序的分页结果。
     *
     * @param current       从 1 开始的页码
     * @param size          单页最大记录数
     * @param jobKey        可选 Quartz JobKey 过滤条件
     * @param triggerKey    可选 Quartz TriggerKey 过滤条件
     * @param status        可选执行状态过滤条件
     * @param startedAtFrom 可选开始时间下界，包含该时刻
     * @param startedAtTo   可选开始时间上界，包含该时刻
     * @return 执行历史分页结果；没有匹配记录时返回空记录列表而不是 null
     */
    IPage<QuartzJobExecutionHistoryEntity> page(long current, long size, String jobKey,
                                                String triggerKey, QuartzExecutionHistoryStatus status,
                                                Instant startedAtFrom, Instant startedAtTo);

    /**
     * 按历史记录 ID 查询单次执行详情。
     *
     * @param id 执行历史主键
     * @return 找到时返回对应历史记录，否则返回空 Optional
     */
    Optional<QuartzJobExecutionHistoryEntity> find(UUID id);
}
