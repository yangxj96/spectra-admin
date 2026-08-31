/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.controller;

import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.upload.javabean.from.ConfirmPartRequest;
import com.devops00.spectra.upload.javabean.from.CreateUploadRequest;
import com.devops00.spectra.upload.javabean.from.PartTargetRequest;
import com.devops00.spectra.upload.javabean.vo.PartTargetVO;
import com.devops00.spectra.upload.javabean.vo.UploadSessionVO;
import com.devops00.spectra.upload.service.UploadApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/file/uploads")
@RequiredArgsConstructor
@Validated
@PreAuthorize("isAuthenticated()")
public class FileUploadController {

    private final UploadApplicationService uploadService;

    @Audit("'创建或恢复文件上传任务'")
    @PostMapping(version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:create')")
    public UploadSessionVO create(@Valid @RequestBody CreateUploadRequest request) {
        return uploadService.create(request);
    }

    @GetMapping(value = "/{uploadId}", version = "1.0.0")
    public UploadSessionVO status(@PathVariable UUID uploadId) {
        return uploadService.status(uploadId);
    }

    @PostMapping(value = "/{uploadId}/parts/{partNumber}/target", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:create')")
    public PartTargetVO target(@PathVariable UUID uploadId, @PathVariable int partNumber,
                               @Valid @RequestBody PartTargetRequest request, HttpServletRequest servletRequest) {
        PartTargetVO target = uploadService.target(uploadId, partNumber, request);
        target.setUrl(withContextPath(servletRequest.getContextPath(), target.getUrl()));
        return target;
    }

    private String withContextPath(String contextPath, String url) {
        if (url == null
                || url.isBlank()
                || isAbsoluteUrl(url)
                || contextPath == null
                || contextPath.isBlank()
                || "/".equals(contextPath)
                || url.equals(contextPath)
                || url.startsWith(contextPath + "/")) {
            return url;
        }
        String normalizedContextPath = contextPath.endsWith("/")
                ? contextPath.substring(0, contextPath.length() - 1)
                : contextPath;
        return normalizedContextPath + (url.startsWith("/") ? url : "/" + url);
    }

    private boolean isAbsoluteUrl(String url) {
        return url.matches("^[A-Za-z][A-Za-z0-9+.-]*:.*");
    }

    @PutMapping(value = "/{uploadId}/parts/{partNumber}/content", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE, version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:create')")
    public ResponseEntity<Void> content(@PathVariable UUID uploadId, @PathVariable int partNumber,
                                        HttpServletRequest request)
            throws IOException {
        uploadService.putPart(uploadId, partNumber, request.getInputStream(), request.getContentLengthLong());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{uploadId}/parts/{partNumber}/confirm", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:create')")
    public ResponseEntity<Void> confirm(@PathVariable UUID uploadId, @PathVariable int partNumber,
                                        @Valid @RequestBody ConfirmPartRequest request) {
        uploadService.confirm(uploadId, partNumber, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{uploadId}/complete", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:create')")
    public UploadSessionVO complete(@PathVariable UUID uploadId) {
        return uploadService.complete(uploadId);
    }

    @DeleteMapping(value = "/{uploadId}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:create')")
    public ResponseEntity<Void> cancel(@PathVariable UUID uploadId) {
        uploadService.cancel(uploadId);
        return ResponseEntity.noContent().build();
    }
}
