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

import com.devops00.spectra.common.port.file.FileReferenceCommand;
import com.devops00.spectra.common.port.file.FileReferenceService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.core.upload.javabean.from.FileReferenceRequest;
import com.devops00.spectra.core.upload.javabean.from.FileReferencePageRequest;
import com.devops00.spectra.core.upload.javabean.vo.FileReferenceAdminVO;
import com.devops00.spectra.common.port.file.FileReferenceView;
import com.devops00.spectra.core.upload.service.FileReferenceAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * 提供文件引用相关的 HTTP 接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@RestController
@RequestMapping("/file/references")
@RequiredArgsConstructor
@Slf4j
@Validated
public class FileReferenceController {

    private final FileReferenceService referenceService;

    private final FileReferenceAdminService adminService;

    /**
     * 处理文件引用相关数据。
     *
     * @param request 请求参数。
     * @return 文件引用视图数据。
     */
    @Audit("'登记文件引用'")
    @PostMapping(version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:reference')")
    public FileReferenceView register(@Validated @RequestBody FileReferenceRequest request) {
        return referenceService.register(new FileReferenceCommand(request.getFileAssetId(), request.getReferenceType(),
                request.getReferenceId(), request.getPurpose(), request.getDisplayName()));
    }

    /**
     * 删除或清理文件引用。
     *
     * @param referenceId 引用标识。
     */
    @Audit("'删除文件引用'")
    @DeleteMapping(value = "/{referenceId}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:reference')")
    public void remove(@PathVariable UUID referenceId) {
        referenceService.removeById(referenceId);
    }

    /**
     * 按查询条件分页查询文件引用。
     *
     * @param page    分页参数。
     * @param request 请求参数。
     * @return 符合条件的分页结果。
     */
    @Audit("'查询文件引用'")
    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:read')")
    public IPage<FileReferenceAdminVO> page(PageFrom page, FileReferencePageRequest request) {
        return adminService.page(page.toPage(), request);
    }
}
