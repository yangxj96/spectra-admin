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

import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.upload.api.FileErrorCode;
import com.devops00.spectra.core.upload.api.FileUploadException;
import com.devops00.spectra.core.upload.javabean.from.ConfirmPartRequest;
import com.devops00.spectra.core.upload.javabean.from.CreateUploadRequest;
import com.devops00.spectra.core.upload.javabean.from.PartTargetRequest;
import com.devops00.spectra.core.upload.javabean.vo.PartTargetVO;
import com.devops00.spectra.core.upload.javabean.vo.UploadSessionVO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.InputStream;
import java.util.UUID;

/**
 * 文件上传应用层用例编排器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Service
public class UploadApplicationService {

    private final SecurityContextAccessor securityContextAccessor;
    private final TaskExecutor taskExecutor;
    private final UploadSessionService sessionService;
    private final UploadPartService partService;
    private final UploadVerificationWorker verificationWorker;

    public UploadApplicationService(SecurityContextAccessor securityContextAccessor,
                                    @Qualifier("fileUploadTaskExecutor") TaskExecutor taskExecutor,
                                    UploadSessionService sessionService,
                                    UploadPartService partService,
                                    UploadVerificationWorker verificationWorker) {
        this.securityContextAccessor = securityContextAccessor;
        this.taskExecutor = taskExecutor;
        this.sessionService = sessionService;
        this.partService = partService;
        this.verificationWorker = verificationWorker;
    }

    /** 创建或恢复当前用户的上传会话。 */
    @Transactional
    public UploadSessionVO create(CreateUploadRequest request) {
        return sessionService.create(request, requireUser());
    }

    /** 查询当前用户的上传会话状态。 */
    @Transactional(readOnly = true)
    public UploadSessionVO status(UUID uploadId) {
        return sessionService.status(uploadId, requireUser());
    }

    /** 为当前用户的分片生成上传目标。 */
    @Transactional
    public PartTargetVO target(UUID uploadId, int partNumber, PartTargetRequest request) {
        UUID userId = requireUser();
        var session = sessionService.requireOwned(uploadId, true, userId);
        sessionService.ensureUploadable(session);
        return partService.target(session, partNumber, request);
    }

    /** 写入当前用户的本地代理分片。 */
    @Transactional
    public void putPart(UUID uploadId, int partNumber, InputStream body, long contentLength) {
        UUID userId = requireUser();
        var session = sessionService.requireOwned(uploadId, true, userId);
        sessionService.ensureUploadable(session);
        partService.putPart(session, partNumber, body, contentLength);
    }

    /** 确认当前用户的分片。 */
    @Transactional
    public void confirm(UUID uploadId, int partNumber, ConfirmPartRequest request) {
        UUID userId = requireUser();
        var session = sessionService.requireOwned(uploadId, true, userId);
        sessionService.ensureUploadable(session);
        partService.confirm(session, partNumber, request);
        sessionService.touchActivity(uploadId);
    }

    /** 完成分片收集并安排独立校验 Worker。 */
    @Transactional
    public UploadSessionVO complete(UUID uploadId) {
        UploadSessionService.Completion completion = sessionService.completeForApplication(uploadId, requireUser());
        UploadSessionVO response = completion.response();
        if (completion.verificationClaimed()) {
            scheduleVerification(uploadId);
        }
        return response;
    }

    /** 取消当前用户尚未完成的上传会话。 */
    @Transactional
    public void cancel(UUID uploadId) {
        sessionService.cancel(uploadId, requireUser());
    }

    /**
     * 处理调度相关数据。
     */
    private void scheduleVerification(UUID uploadId) {
        Runnable verification = () -> taskExecutor.execute(() -> verificationWorker.verify(uploadId));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    verification.run();
                }
            });
        } else {
            verification.run();
        }
    }

    /**
     * 校验用户。
     */
    private UUID requireUser() {
        UUID userId = securityContextAccessor.currentUserId();
        if (userId == null) {
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_PERMISSION_DENIED, "请先完成身份认证");
        }
        return userId;
    }
}
