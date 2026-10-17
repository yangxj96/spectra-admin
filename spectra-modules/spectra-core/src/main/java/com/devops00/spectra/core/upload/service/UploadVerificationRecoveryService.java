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

package com.devops00.spectra.core.upload.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * 重新投递心跳过期的上传校验任务，覆盖进程重启和线程池任务丢失。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@Slf4j
@Service
public class UploadVerificationRecoveryService {

    /**
     * 校验心跳租约；超过该时长未更新的 VERIFYING 会话视为可重领。
     */
    public static final Duration LEASE = Duration.ofMinutes(10);

    private static final int BATCH_SIZE = 100;

    private final FileUploadCleanupTransactionService transactionService;
    private final UploadVerificationWorker verificationWorker;
    private final TaskExecutor taskExecutor;

    public UploadVerificationRecoveryService(FileUploadCleanupTransactionService transactionService,
                                             UploadVerificationWorker verificationWorker,
                                             @Qualifier("fileUploadTaskExecutor") TaskExecutor taskExecutor) {
        this.transactionService = transactionService;
        this.verificationWorker = verificationWorker;
        this.taskExecutor = taskExecutor;
    }

    /**
     * 领取并重新投递一批过期校验会话。
     *
     * @param now 当前时间。
     * @return 成功投递的数量。
     */
    public int recoverStale(Instant now) {
        Instant staleBefore = now.minus(LEASE);
        int dispatched = 0;
        for (UUID uploadId : transactionService.claimStaleVerifications(staleBefore, now, BATCH_SIZE)) {
            try {
                taskExecutor.execute(() -> verificationWorker.verify(uploadId));
                dispatched++;
            } catch (RuntimeException exception) {
                log.warn("上传校验重投失败，等待下一轮: uploadId={}", uploadId, exception);
            }
        }
        return dispatched;
    }
}
