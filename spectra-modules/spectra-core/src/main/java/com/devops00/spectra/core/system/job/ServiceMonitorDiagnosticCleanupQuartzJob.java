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

package com.devops00.spectra.core.system.job;

import com.devops00.spectra.core.system.service.impl.ServiceMonitorDiagnosticServiceImpl;
import jakarta.annotation.Resource;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.Map;

/** 由 Quartz 集群调度服务监控诊断文件清理。 */
@DisallowConcurrentExecution
public class ServiceMonitorDiagnosticCleanupQuartzJob implements Job {

    @Resource
    private ServiceMonitorDiagnosticServiceImpl diagnosticService;

    /**
     * 删除已过期的服务监控诊断任务和文件。
     *
     * @param context Quartz 执行上下文；任务不读取外部 JobDataMap 参数
     */
    @Override
    public void execute(JobExecutionContext context) {
        diagnosticService.cleanupExpiredTasks();
        context.setResult(Map.of("cleanup", "completed"));
    }
}
