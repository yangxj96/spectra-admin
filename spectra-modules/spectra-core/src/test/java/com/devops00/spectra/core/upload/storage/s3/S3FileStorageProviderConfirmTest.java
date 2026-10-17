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

package com.devops00.spectra.core.upload.storage.s3;

import com.devops00.spectra.core.upload.api.FileErrorCode;
import com.devops00.spectra.core.upload.api.FileUploadException;
import com.devops00.spectra.core.upload.properties.S3Properties;
import com.devops00.spectra.core.upload.storage.StorageMultipart;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListPartsRequest;
import software.amazon.awssdk.services.s3.model.ListPartsResponse;
import software.amazon.awssdk.services.s3.model.Part;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link S3FileStorageProvider#confirmExternalPart} 以存储侧 ListParts 结果校验分片的验证。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class S3FileStorageProviderConfirmTest {

    private static S3FileStorageProvider provider(S3Client client) {
        return new S3FileStorageProvider(client, mock(S3Presigner.class), mock(S3Properties.class));
    }

    private static void stubPart(S3Client client, int partNumber, long size, String etag) {
        when(client.listParts(any(ListPartsRequest.class))).thenReturn(ListPartsResponse.builder()
                .parts(Part.builder().partNumber(partNumber).size(size).eTag(etag).build())
                .build());
    }

    private static StorageMultipart multipart() {
        return new StorageMultipart("bucket", "key", "upload-id");
    }

    @Test
    void matchingSizeAndEtagIsAccepted() {
        var client = mock(S3Client.class);
        stubPart(client, 1, 10L, "\"etag-1\"");
        var stored = provider(client).confirmExternalPart(multipart(), 1, 10L, "sha", "etag-1");
        assertEquals(10L, stored.size());
        assertEquals("\"etag-1\"", stored.etag());
    }

    @Test
    void sizeMismatchIsRejected() {
        var client = mock(S3Client.class);
        stubPart(client, 1, 9L, "\"etag-1\"");
        var exception = assertThrows(FileUploadException.class,
                () -> provider(client).confirmExternalPart(multipart(), 1, 10L, "sha", "etag-1"));
        assertEquals(FileErrorCode.FILE_PART_HASH_MISMATCH, exception.getErrorCode());
    }

    @Test
    void etagMismatchIsRejected() {
        var client = mock(S3Client.class);
        stubPart(client, 1, 10L, "\"etag-2\"");
        var exception = assertThrows(FileUploadException.class,
                () -> provider(client).confirmExternalPart(multipart(), 1, 10L, "sha", "etag-1"));
        assertEquals(FileErrorCode.FILE_PART_HASH_MISMATCH, exception.getErrorCode());
    }

    @Test
    void missingPartIsRejected() {
        var client = mock(S3Client.class);
        stubPart(client, 2, 10L, "\"etag-1\"");
        var exception = assertThrows(FileUploadException.class,
                () -> provider(client).confirmExternalPart(multipart(), 1, 10L, "sha", "etag-1"));
        assertEquals(FileErrorCode.FILE_PART_INVALID, exception.getErrorCode());
    }
}
