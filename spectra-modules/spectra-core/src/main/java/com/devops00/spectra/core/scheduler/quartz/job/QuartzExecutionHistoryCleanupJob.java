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

package com.devops00.spectra.core.scheduler.quartz.job;

import com.devops00.spectra.core.scheduler.history.service.QuartzJobExecutionHistoryService;
import jakarta.annotation.Resource;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

/** 由 Quartz 集群调度执行历史超时记录收敛。 */
@DisallowConcurrentExecution
public class QuartzExecutionHistoryCleanupJob implements Job {

    @Resource
    private QuartzJobExecutionHistoryService historyService;

    /**
     * 将长期未收到完成回调的 RUNNING 记录标记为 ABANDONED。
     *
     * @param context Quartz 执行上下文；任务不读取外部 JobDataMap 参数
     */
    @Override
    public void execute(JobExecutionContext context) {
        historyService.cleanup();
    }
}
