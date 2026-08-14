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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * OA 通用申请接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Slf4j
@RestController
@RequestMapping("/oa/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * 分页查询 OA 申请。
     */
    @ULog("'分页查询 OA 申请'")
    @GetMapping(value = "/page", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:application:read')")
    public IPage<ApplicationVO> page(PageFrom page, ApplicationPageFrom params) {
        return applicationService.page(page, params);
    }

    /**
     * 查询 OA 申请详情。
     */
    @ULog("'查询 OA 申请详情'")
    @GetMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:application:read')")
    public ApplicationVO get(@PathVariable UUID id) {
        return applicationService.get(id);
    }

    /**
     * 查询 OA 申请类型。
     */
    @ULog("'查询 OA 申请类型'")
    @GetMapping(value = "/types", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:application:read')")
    public List<ApplicationTypeVO> listTypes() {
        return applicationService.listTypes();
    }

    /**
     * 查询全部 OA 申请类型配置。
     */
    @ULog("'查询全部 OA 申请类型配置'")
    @GetMapping(value = "/types/all", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:application-type:read')")
    public List<ApplicationTypeVO> listAllTypes() {
        return applicationService.listAllTypes();
    }

    /**
     * 创建 OA 申请类型配置。
     */
    @ULog("'创建 OA 申请类型配置'")
    @PostMapping(value = "/types", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:application-type:create')")
    public UUID createdType(@Validated @RequestBody ApplicationTypeSaveFrom from) {
        return applicationService.createdType(from);
    }

    /**
     * 修改 OA 申请类型配置。
     */
    @ULog("'修改 OA 申请类型配置'")
    @org.springframework.web.bind.annotation.PutMapping(value = "/types/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:application-type:update')")
    public void modifyType(@PathVariable UUID id, @Validated @RequestBody ApplicationTypeSaveFrom from) {
        applicationService.modifyType(id, from);
    }

    /**
     * 删除 OA 申请类型配置。
     */
    @ULog("'删除 OA 申请类型配置'")
    @DeleteMapping(value = "/types/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:application-type:disable')")
    public void deleteType(@PathVariable UUID id) {
        applicationService.deleteType(id);
    }

    /**
     * 撤回 OA 申请。
     */
    @ULog("'撤回 OA 申请'")
    @PostMapping(value = "/{id}/withdraw", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:application:update')")
    public void withdraw(@PathVariable UUID id) {
        applicationService.withdraw(id);
    }

    /**
     * 取消 OA 申请。
     */
    @ULog("'取消 OA 申请'")
    @PostMapping(value = "/{id}/cancel", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:application:update')")
    public void cancel(@PathVariable UUID id) {
        applicationService.cancel(id);
    }
}
