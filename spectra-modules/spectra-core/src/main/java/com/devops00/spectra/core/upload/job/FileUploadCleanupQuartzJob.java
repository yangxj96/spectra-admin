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

package com.devops00.spectra.core.upload.job;

import com.devops00.spectra.core.upload.service.FileUploadCleanupService;
import jakarta.annotation.Resource;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.time.Instant;

/**
 * 由 Quartz 集群调度文件上传临时资源清理。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@DisallowConcurrentExecution
public class FileUploadCleanupQuartzJob implements Job {

    @Resource
    private FileUploadCleanupService cleanupService;

    /**
     * 清理到期上传会话、临时对象及无引用文件资产。
     *
     * @param context Quartz 执行上下文；任务不读取外部 JobDataMap 参数
     */
    @Override
    public void execute(JobExecutionContext context) {
        context.setResult(cleanupService.cleanupBatch(Instant.now()));
    }
}
