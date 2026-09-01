/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.upload.javabean.from.FileAdminOperationFrom;
import com.devops00.spectra.upload.javabean.from.FileUploadAdminPageRequest;
import com.devops00.spectra.upload.javabean.vo.FileUploadAdminDetailVO;
import com.devops00.spectra.upload.javabean.vo.FileUploadAdminVO;
import com.devops00.spectra.upload.service.FileUploadAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 文件上传任务管理接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026-08-31
 */
@RestController
@RequestMapping("/file/uploads")
@RequiredArgsConstructor
@Validated
public class FileUploadAdminController {

    private final FileUploadAdminService adminService;

    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:read')")
    public IPage<FileUploadAdminVO> page(PageFrom page, FileUploadAdminPageRequest request) {
        return adminService.page(page.toPage(), request);
    }

    @GetMapping(value = "/{uploadId}/admin-detail", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:read')")
    public FileUploadAdminDetailVO detail(@PathVariable UUID uploadId) {
        return adminService.detail(uploadId);
    }

    @Audit("'管理员取消文件上传任务'")
    @PostMapping(value = "/{uploadId}/admin-cancel", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:manage')")
    public void cancel(@PathVariable UUID uploadId, @Valid @RequestBody FileAdminOperationFrom operation) {
        adminService.cancel(uploadId, operation);
    }
}
