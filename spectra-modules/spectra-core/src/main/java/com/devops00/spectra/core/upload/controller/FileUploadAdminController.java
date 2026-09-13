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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.core.upload.javabean.from.FileAdminOperationFrom;
import com.devops00.spectra.core.upload.javabean.from.FileUploadAdminPageRequest;
import com.devops00.spectra.core.upload.javabean.vo.FileUploadAdminDetailVO;
import com.devops00.spectra.core.upload.javabean.vo.FileUploadAdminVO;
import com.devops00.spectra.core.upload.service.FileUploadAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Validated
public class FileUploadAdminController {

    private final FileUploadAdminService adminService;

    /**
     * 按查询条件分页查询文件上传。
     *
     * @param page    分页参数。
     * @param request 请求参数。
     * @return 符合条件的分页结果。
     */
    @Audit("'分页查询文件上传任务'")
    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:read')")
    public IPage<FileUploadAdminVO> page(PageFrom page, FileUploadAdminPageRequest request) {
        return adminService.page(page.toPage(), request);
    }

    /**
     * 按查询条件查询文件上传详情。
     *
     * @param uploadId 上传标识。
     * @return 文件上传详情数据。
     */
    @Audit("'查询文件上传任务详情'")
    @GetMapping(value = "/{uploadId}/admin-detail", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:read')")
    public FileUploadAdminDetailVO detail(@PathVariable UUID uploadId) {
        return adminService.detail(uploadId);
    }

    /**
     * 取消文件上传。
     *
     * @param uploadId  上传标识。
     * @param operation 操作参数。
     */
    @Audit("'管理员取消文件上传任务'")
    @PostMapping(value = "/{uploadId}/admin-cancel", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:manage')")
    public void cancel(@PathVariable UUID uploadId, @Valid @RequestBody FileAdminOperationFrom operation) {
        adminService.cancel(uploadId, operation);
    }
}
