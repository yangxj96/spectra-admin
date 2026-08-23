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

package com.devops00.spectra.ai.configuration.rag.listener;

import com.devops00.spectra.ai.properties.AiRAGProperties;
import com.devops00.spectra.common.event.FileUploadFinishEvent;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationSendRequest;
import com.devops00.spectra.common.notification.NotificationService;
import com.devops00.spectra.common.notification.NotificationTemplateCode;
import com.devops00.spectra.upload.javabean.entity.FileInfo;
import com.devops00.spectra.upload.service.FileInfoService;
import com.devops00.spectra.upload.service.impl.FileUploadFacade;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * AI RAG 失败结果通知和幂等键回归测试。
 */
class FileUploadFinishListenerNotificationTest {

    @Test
    void shouldNotifyCreatorWhenRagIndexFails() {
        var embeddingStore = mock(EmbeddingStore.class);
        var embeddingModel = mock(EmbeddingModel.class);
        var fileInfoService = mock(FileInfoService.class);
        var fileUploadFacade = mock(FileUploadFacade.class);
        var properties = new AiRAGProperties();
        var notificationService = mock(NotificationService.class);
        var fileId = UUID.randomUUID();
        var creatorId = UUID.randomUUID();
        var fileInfo = new FileInfo();
        fileInfo.setId(fileId);
        fileInfo.setCreatedBy(creatorId);
        fileInfo.setOriginalName("知识库文档.pdf");
        when(fileInfoService.getById(fileId)).thenReturn(fileInfo);
        when(fileUploadFacade.openStream(fileInfo)).thenThrow(new IllegalStateException("mock storage failure"));

        var listener = new FileUploadFinishListener(embeddingStore, embeddingModel, fileInfoService, fileUploadFacade,
                properties, notificationService);

        listener.handleFileUploaded(new FileUploadFinishEvent(this, fileId));

        var requestCaptor = org.mockito.ArgumentCaptor.forClass(NotificationSendRequest.class);
        verify(notificationService).send(requestCaptor.capture());
        assertEquals(NotificationPurpose.SYSTEM_NOTICE, requestCaptor.getValue().purpose());
        assertEquals("ai:rag:index:" + fileId + ":failure", requestCaptor.getValue().idempotencyKey());
        assertEquals(creatorId, requestCaptor.getValue().recipientUserIds().getFirst());
        assertEquals(NotificationTemplateCode.AI_RAG_INDEX, requestCaptor.getValue().templateGroupCode());
        assertEquals("失败", requestCaptor.getValue().parameters().get("status"));
    }
}
