/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.core.upload.javabean.from.FileTypePolicySaveFrom;
import com.devops00.spectra.core.upload.javabean.vo.FileTypePolicyVO;
import com.devops00.spectra.core.upload.service.FileTypeManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 文件类型策略管理接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026-08-31
 */
@RestController
@RequestMapping("/file/types")
@RequiredArgsConstructor
@Validated
public class FileTypeController {

    private final FileTypeManagementService managementService;

    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:read')")
    public IPage<FileTypePolicyVO> page(PageFrom page) {
        return managementService.page(page.toPage());
    }

    @GetMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:read')")
    public FileTypePolicyVO get(@PathVariable UUID id) {
        return managementService.get(id);
    }

    @Audit("'创建文件类型策略'")
    @PostMapping(version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:manage')")
    public FileTypePolicyVO create(@Valid @RequestBody FileTypePolicySaveFrom from) {
        return managementService.create(from);
    }

    @Audit("'修改文件类型策略'")
    @PutMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:manage')")
    public FileTypePolicyVO modify(@PathVariable UUID id, @Valid @RequestBody FileTypePolicySaveFrom from) {
        return managementService.modify(id, from);
    }

    @Audit("'启用文件类型策略'")
    @PostMapping(value = "/{id}/enable", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:manage')")
    public FileTypePolicyVO enable(@PathVariable UUID id) {
        return managementService.enable(id);
    }

    @Audit("'停用文件类型策略'")
    @PostMapping(value = "/{id}/disable", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:manage')")
    public FileTypePolicyVO disable(@PathVariable UUID id) {
        return managementService.disable(id);
    }
}
