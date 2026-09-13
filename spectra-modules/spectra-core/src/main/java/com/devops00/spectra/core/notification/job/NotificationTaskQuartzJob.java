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

package com.devops00.spectra.core.notification.job;

import com.devops00.spectra.core.notification.dispatch.NotificationTaskWorker;
import jakarta.annotation.Resource;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.Map;

/**
 * 由 Quartz 集群调度通知投递任务批次。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@DisallowConcurrentExecution
public class NotificationTaskQuartzJob implements Job {

    @Resource
    private NotificationTaskWorker worker;

    /**
     * 领取并处理一批到期通知任务；通知任务自身仍使用数据库条件更新保证幂等领取。
     *
     * @param context Quartz 执行上下文；任务不读取外部 JobDataMap 参数
     */
    @Override
    public void execute(JobExecutionContext context) {
        context.setResult(Map.of("processed", worker.processPending(50)));
    }
}
