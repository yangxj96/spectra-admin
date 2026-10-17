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

import com.devops00.spectra.core.upload.service.UploadVerificationRecoveryService;
import jakarta.annotation.Resource;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.time.Instant;

/**
 * 周期扫描心跳过期的上传校验会话并重新投递。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@DisallowConcurrentExecution
public class FileUploadVerificationRecoveryQuartzJob implements Job {

    @Resource
    private UploadVerificationRecoveryService recoveryService;

    /**
     * 执行一次校验恢复扫描。
     *
     * @param context Quartz 执行上下文。
     */
    @Override
    public void execute(JobExecutionContext context) {
        context.setResult(recoveryService.recoverStale(Instant.now()));
    }
}
