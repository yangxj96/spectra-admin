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

import com.devops00.spectra.core.upload.api.FileErrorCode;
import com.devops00.spectra.core.upload.api.FileUploadException;
import com.devops00.spectra.core.upload.javabean.constant.TransportMode;
import com.devops00.spectra.core.upload.javabean.constant.UploadPartStatus;
import com.devops00.spectra.core.upload.javabean.converter.FileUploadConverter;
import com.devops00.spectra.core.upload.javabean.entity.FileUploadPart;
import com.devops00.spectra.core.upload.javabean.entity.FileUploadSession;
import com.devops00.spectra.core.upload.javabean.from.ConfirmPartRequest;
import com.devops00.spectra.core.upload.mapper.FileUploadPartMapper;
import com.devops00.spectra.core.upload.properties.FileUploadProperties;
import com.devops00.spectra.core.upload.storage.FileStorageProviderRegistry;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link UploadPartService#confirm} 拒绝旧 attempt 回调的验证。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class UploadPartServiceAttemptTest {

    @Test
    void staleAttemptConfirmationIsRejected() {
        var mapper = mock(FileUploadPartMapper.class);
        var service = new UploadPartService(mapper, mock(FileStorageProviderRegistry.class),
                mock(FileUploadProperties.class), mock(FileUploadConverter.class));
        var session = new FileUploadSession();
        session.setId(UUID.randomUUID());
        session.setTransportMode(TransportMode.LOCAL_PROXY);
        var part = new FileUploadPart();
        part.setUploadAttempt(2);
        part.setExpectedSize(10L);
        part.setExpectedSha256("a".repeat(64));
        part.setStatus(UploadPartStatus.UPLOADED);
        when(mapper.selectForUpdate(session.getId(), 1)).thenReturn(part);
        var request = new ConfirmPartRequest();
        request.setAttempt(1);
        request.setPartSize(10L);
        request.setPartSha256("a".repeat(64));

        var exception = assertThrows(FileUploadException.class, () -> service.confirm(session, 1, request));

        assertEquals(FileErrorCode.FILE_UPLOAD_CONFLICT, exception.getErrorCode());
    }
}
