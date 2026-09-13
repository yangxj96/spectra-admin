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
import com.devops00.spectra.core.upload.javabean.constant.UploadSessionStatus;
import com.devops00.spectra.core.upload.javabean.vo.UploadSessionVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 上传应用服务和校验 Worker 的依赖边界测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@ExtendWith(MockitoExtension.class)
class UploadApplicationServiceTest {

    @Mock
    private SecurityContextAccessor securityContextAccessor;

    @Mock
    private TaskExecutor taskExecutor;

    @Mock
    private UploadSessionService sessionService;

    @Mock
    private UploadPartService partService;

    @Mock
    private UploadVerificationWorker verificationWorker;

    @Test
    void completeRunsVerificationThroughIndependentWorkerAfterScheduling() {
        UUID userId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        var response = new UploadSessionVO();
        response.setStatus(UploadSessionStatus.VERIFYING);
        when(securityContextAccessor.currentUserId()).thenReturn(userId);
        when(sessionService.completeForApplication(uploadId, userId))
                .thenReturn(new UploadSessionService.Completion(response, true));
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(0).run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));

        var service = new UploadApplicationService(securityContextAccessor, taskExecutor, sessionService, partService,
                verificationWorker);

        service.complete(uploadId);

        verify(verificationWorker).verify(uploadId);
    }

    @Test
    void repeatedCompleteDoesNotScheduleAnotherVerification() {
        UUID userId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        var response = new UploadSessionVO();
        response.setStatus(UploadSessionStatus.VERIFYING);
        when(securityContextAccessor.currentUserId()).thenReturn(userId);
        when(sessionService.completeForApplication(uploadId, userId))
                .thenReturn(new UploadSessionService.Completion(response, false));

        var service = new UploadApplicationService(securityContextAccessor, taskExecutor, sessionService, partService,
                verificationWorker);

        service.complete(uploadId);

        verifyNoInteractions(taskExecutor, verificationWorker);
    }
}
