/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.controller;

import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.port.file.FileAccessContext;
import com.devops00.spectra.common.port.file.FileDownload;
import com.devops00.spectra.core.upload.service.FileAssetApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/file/assets")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FileAssetStreamController {

    private final FileAssetApplicationService assetService;
    private final SecurityContextAccessor securityContextAccessor;

    @GetMapping(value = "/{fileAssetId}/preview", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:read') or hasPermission(null, 'file:admin:read')")
    public ResponseEntity<InputStreamResource> preview(@PathVariable UUID fileAssetId,
                                                       @RequestParam(required = false) String referenceType,
                                                       @RequestParam(required = false) UUID referenceId,
                                                       HttpServletRequest request) {
        return stream(fileAssetId, referenceType, referenceId, request, false);
    }

    @GetMapping(value = "/{fileAssetId}/download", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:read') or hasPermission(null, 'file:admin:read')")
    public ResponseEntity<InputStreamResource> download(@PathVariable UUID fileAssetId,
                                                        @RequestParam(required = false) String referenceType,
                                                        @RequestParam(required = false) UUID referenceId,
                                                        HttpServletRequest request) {
        return stream(fileAssetId, referenceType, referenceId, request, true);
    }

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

    private record Range(Long start, Long end) {
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
