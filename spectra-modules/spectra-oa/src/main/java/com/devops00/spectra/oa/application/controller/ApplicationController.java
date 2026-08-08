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

package com.devops00.spectra.oa.application.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.validation.annotation.Validated;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.application.javabean.from.ApplicationPageFrom;
import com.devops00.spectra.oa.application.javabean.from.ApplicationTypeSaveFrom;
import com.devops00.spectra.oa.application.javabean.vo.ApplicationTypeVO;
import com.devops00.spectra.oa.application.javabean.vo.ApplicationVO;
import com.devops00.spectra.oa.application.service.ApplicationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/// OA 通用申请接口。
@Slf4j
@RestController
@RequestMapping("/oa/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @ULog("'分页查询 OA 申请'")
    @GetMapping(value = "/page", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_APPLICATION:QUERY')")
    public IPage<ApplicationVO> page(PageFrom page, ApplicationPageFrom params) {
        return applicationService.page(page, params);
    }

    @ULog("'查询 OA 申请详情'")
    @GetMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_APPLICATION:QUERY')")
    public ApplicationVO get(@PathVariable UUID id) {
        return applicationService.get(id);
    }

    @ULog("'查询 OA 申请类型'")
    @GetMapping(value = "/types", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_APPLICATION:QUERY')")
    public List<ApplicationTypeVO> listTypes() {
        return applicationService.listTypes();
    }

    @ULog("'查询全部 OA 申请类型配置'")
    @GetMapping(value = "/types/all", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_APPLICATION_TYPE:QUERY')")
    public List<ApplicationTypeVO> listAllTypes() {
        return applicationService.listAllTypes();
    }

    @ULog("'创建 OA 申请类型配置'")
    @PostMapping(value = "/types", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_APPLICATION_TYPE:INSERT')")
    public UUID createdType(@Validated @RequestBody ApplicationTypeSaveFrom from) {
        return applicationService.createdType(from);
    }

    @ULog("'修改 OA 申请类型配置'")
    @org.springframework.web.bind.annotation.PutMapping(value = "/types/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_APPLICATION_TYPE:UPDATE')")
    public void modifyType(@PathVariable UUID id, @Validated @RequestBody ApplicationTypeSaveFrom from) {
        applicationService.modifyType(id, from);
    }

    @ULog("'删除 OA 申请类型配置'")
    @DeleteMapping(value = "/types/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_APPLICATION_TYPE:DELETE')")
    public void deleteType(@PathVariable UUID id) {
        applicationService.deleteType(id);
    }

    @ULog("'撤回 OA 申请'")
    @PostMapping(value = "/{id}/withdraw", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_APPLICATION:UPDATE')")
    public void withdraw(@PathVariable UUID id) {
        applicationService.withdraw(id);
    }

    @ULog("'取消 OA 申请'")
    @PostMapping(value = "/{id}/cancel", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_APPLICATION:UPDATE')")
    public void cancel(@PathVariable UUID id) {
        applicationService.cancel(id);
    }
}
