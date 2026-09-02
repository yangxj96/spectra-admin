/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.initialization.controller;

import com.devops00.spectra.common.annotation.Encrypt;
import com.devops00.spectra.core.security.initialization.javabean.from.SystemInitializationCompleteFrom;
import com.devops00.spectra.core.security.initialization.javabean.from.SystemInitializationStartFrom;
import com.devops00.spectra.core.security.initialization.javabean.vo.SystemInitializationStartVO;
import com.devops00.spectra.core.security.initialization.javabean.vo.SystemInitializationStatusVO;
import com.devops00.spectra.core.security.initialization.service.SystemInitializationService;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.common.audit.AuditCategory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 首次系统初始化接口；前端初始化页面可在此 API 之上实现。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/initialization")
public class SystemInitializationController {

    private static final String INITIALIZATION_TOKEN_HEADER = "X-Spectra-Initialization-Token";

    private final SystemInitializationService initializationService;

    /**
     * 查询或获取目标数据（{@code status}）。
     */
    @GetMapping(value = "/status", version = "1.0.0")
    @PreAuthorize("permitAll()")
    public SystemInitializationStatusVO status() {
        return initializationService.status();
    }

    /**
     * 创建或构建目标数据（{@code start}）。
     */
    @Audit(value = "'开始系统初始化'", category = AuditCategory.SECURITY)
    @Encrypt(response = false)
    @PostMapping(value = "/start", version = "1.0.0")
    @PreAuthorize("permitAll()")
    public SystemInitializationStartVO start(
                                             @Valid @RequestBody SystemInitializationStartFrom from,
                                             @RequestHeader(value = INITIALIZATION_TOKEN_HEADER, required = false) String token) {
        return initializationService.start(from, token);
    }

    /**
     * 处理内部业务逻辑（{@code complete}）。
     */
    @Audit(value = "'完成系统初始化'", category = AuditCategory.SECURITY)
    @Encrypt(response = false)
    @PostMapping(value = "/complete", version = "1.0.0")
    @PreAuthorize("permitAll()")
    public void complete(@Valid @RequestBody SystemInitializationCompleteFrom from) {
        initializationService.complete(from);
    }
}
