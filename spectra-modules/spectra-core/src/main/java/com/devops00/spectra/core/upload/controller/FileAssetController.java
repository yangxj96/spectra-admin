/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.core.upload.javabean.from.FileAssetPageRequest;
import com.devops00.spectra.core.upload.javabean.vo.FileAssetVO;
import com.devops00.spectra.core.upload.service.FileAssetApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/file/assets")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasPermission(null, 'file:admin:read')")
public class FileAssetController {

    private final FileAssetApplicationService assetService;

    @Audit("'分页查询文件资产'")
    @GetMapping(value = "/page", version = "1.0.0")
    public IPage<FileAssetVO> page(PageFrom page, FileAssetPageRequest request) {
        return assetService.page(page.toPage(), request);
    }

    @Audit("'删除文件资产'")
    @DeleteMapping(value = "/{fileAssetId}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:delete')")
    public void delete(@PathVariable UUID fileAssetId) {
        assetService.delete(fileAssetId);
    }
}
