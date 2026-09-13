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

import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.port.file.FileAccessContext;
import com.devops00.spectra.common.port.file.FileDownload;
import com.devops00.spectra.core.upload.service.FileAssetApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 提供文件资产数据流相关的 HTTP 接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@RestController
@RequestMapping("/file/assets")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class FileAssetStreamController {

    private final FileAssetApplicationService assetService;
    private final SecurityContextAccessor securityContextAccessor;

    /**
     * 处理文件资产数据流相关数据。
     *
     * @param fileAssetId   文件资产标识。
     * @param referenceType 引用类型参数。
     * @param referenceId   引用标识。
     * @param request       请求参数。
     * @return 包含处理结果的 HTTP 响应。
     */
    @Audit("'预览文件'")
    @GetMapping(value = "/{fileAssetId}/preview", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:read') or hasPermission(null, 'file:admin:read')")
    public ResponseEntity<InputStreamResource> preview(@PathVariable UUID fileAssetId,
                                                       @RequestParam(required = false) String referenceType,
                                                       @RequestParam(required = false) UUID referenceId,
                                                       HttpServletRequest request) {
        return stream(fileAssetId, referenceType, referenceId, request, false);
    }

    /**
     * 处理文件资产数据流相关数据。
     *
     * @param fileAssetId   文件资产标识。
     * @param referenceType 引用类型参数。
     * @param referenceId   引用标识。
     * @param request       请求参数。
     * @return 包含处理结果的 HTTP 响应。
     */
    @Audit("'下载文件'")
    @GetMapping(value = "/{fileAssetId}/download", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:read') or hasPermission(null, 'file:admin:read')")
    public ResponseEntity<InputStreamResource> download(@PathVariable UUID fileAssetId,
                                                        @RequestParam(required = false) String referenceType,
                                                        @RequestParam(required = false) UUID referenceId,
                                                        HttpServletRequest request) {
        return stream(fileAssetId, referenceType, referenceId, request, true);
    }

    /**
     * 处理数据流相关数据。
     */
    private ResponseEntity<InputStreamResource> stream(UUID fileAssetId, String referenceType, UUID referenceId,
                                                       HttpServletRequest request, boolean download) {
        Range range = Range.parse(request.getHeader(HttpHeaders.RANGE));
        FileAccessContext context = new FileAccessContext(securityContextAccessor.currentUserId(), referenceType, referenceId,
                range.start(), range.end());
        FileDownload file = assetService.openForAuthorizedUser(fileAssetId, context);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.contentType()));
        headers.setContentLength(file.size());
        headers.set("X-Content-Type-Options", "nosniff");
        headers.setContentDisposition((download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(file.displayName(), StandardCharsets.UTF_8)
                .build());
        if (range.start() != null) {
            long end = range.end() == null ? range.start() + file.size() - 1 : range.end();
            headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + range.start() + "-" + end + "/*");
            return new ResponseEntity<>(new InputStreamResource(file.stream()), headers, HttpStatus.PARTIAL_CONTENT);
        }
        return new ResponseEntity<>(new InputStreamResource(file.stream()), headers, HttpStatus.OK);
    }

    /**
     * 提供范围相关的 HTTP 接口。
     *
     * @param start 数据读取范围的起始字节偏移
     * @param end   数据读取范围的结束字节偏移
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    private record Range(Long start, Long end) {
        /**
         * 解析范围。
         */
        private static Range parse(String value) {
            if (value == null || value.isBlank())
                return new Range(null, null);
            if (!value.startsWith("bytes=") || value.indexOf(',') >= 0)
                throw new IllegalArgumentException("invalid range");
            String[] bounds = value.substring(6).split("-", -1);
            if (bounds.length != 2 || bounds[0].isBlank())
                throw new IllegalArgumentException("invalid range");
            long start = Long.parseLong(bounds[0]);
            Long end = bounds[1].isBlank() ? null : Long.valueOf(bounds[1]);
            if (start < 0 || (end != null && end < start))
                throw new IllegalArgumentException("invalid range");
            return new Range(start, end);
        }
    }
}
