/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.controller;

import com.devops00.spectra.common.port.file.FileReferenceCommand;
import com.devops00.spectra.common.port.file.FileReferenceService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.upload.javabean.from.FileReferenceRequest;
import com.devops00.spectra.upload.javabean.from.FileReferencePageRequest;
import com.devops00.spectra.upload.javabean.entity.FileReference;
import com.devops00.spectra.upload.javabean.vo.FileReferenceAdminVO;
import com.devops00.spectra.common.port.file.FileReferenceView;
import com.devops00.spectra.upload.service.FileReferenceAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/file/references")
@RequiredArgsConstructor
@Validated
public class FileReferenceController {

    private final FileReferenceService referenceService;

    private final FileReferenceAdminService adminService;

    @PostMapping(version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:reference')")
    public FileReferenceView register(@Validated @RequestBody FileReferenceRequest request) {
        return referenceService.register(new FileReferenceCommand(request.getFileAssetId(), request.getReferenceType(),
                request.getReferenceId(), request.getPurpose(), request.getDisplayName()));
    }

    @DeleteMapping(value = "/{referenceId}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:reference')")
    public void remove(@PathVariable UUID referenceId) {
        referenceService.removeById(referenceId);
    }

    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:read')")
    public IPage<FileReferenceAdminVO> page(PageFrom page, FileReferencePageRequest request) {
        return adminService.page(page.toPage(), request);
    }
}
