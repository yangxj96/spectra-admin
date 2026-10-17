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

package com.devops00.spectra.core.upload.controller;

import com.devops00.spectra.common.port.file.FileDownload;
import com.devops00.spectra.common.port.file.FileRangeNotSatisfiableException;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.upload.service.FileAssetApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.InputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证文件流响应的 {@code Content-Range} 反映实际返回字节范围而非请求范围。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class FileAssetStreamControllerTest {

    private static FileAssetStreamController controller(UUID userId, FileDownload download) {
        var service = mock(FileAssetApplicationService.class);
        var accessor = mock(SecurityContextAccessor.class);
        when(accessor.currentUserId()).thenReturn(userId);
        when(service.openForAuthorizedUser(any(), any())).thenReturn(download);
        return new FileAssetStreamController(service, accessor);
    }

    @Test
    void rangedResponseReportsActualReturnedRange() {
        UUID userId = UUID.randomUUID();
        var download = new FileDownload(InputStream.nullInputStream(), "a.bin", "application/octet-stream",
                10L, 0L, 99L);
        var request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.RANGE, "bytes=0-99");

        var response = controller(userId, download).preview(UUID.randomUUID(), null, null, request);

        assertEquals(HttpStatus.PARTIAL_CONTENT, response.getStatusCode());
        assertEquals("bytes 0-9/*", response.getHeaders().getFirst(HttpHeaders.CONTENT_RANGE),
                "响应范围应按实际返回字节裁剪，而非原样回显请求范围");
        assertEquals(10L, response.getHeaders().getContentLength());
    }

    @Test
    void fullResponseHasNoContentRange() {
        UUID userId = UUID.randomUUID();
        var download = new FileDownload(InputStream.nullInputStream(), "a.bin", "application/octet-stream",
                10L, null, null);
        var request = new MockHttpServletRequest();

        var response = controller(userId, download).preview(UUID.randomUUID(), null, null, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getHeaders().getFirst(HttpHeaders.CONTENT_RANGE));
        assertEquals(10L, response.getHeaders().getContentLength());
    }

    @Test
    void invalidRangeHeaderIsRejectedAsRangeNotSatisfiable() {
        var download = new FileDownload(InputStream.nullInputStream(), "a.bin", "application/octet-stream",
                10L, null, null);
        var request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.RANGE, "bytes=abc-5");

        var controller = controller(UUID.randomUUID(), download);
        assertThrows(FileRangeNotSatisfiableException.class,
                () -> controller.preview(UUID.randomUUID(), null, null, request));
    }
}
