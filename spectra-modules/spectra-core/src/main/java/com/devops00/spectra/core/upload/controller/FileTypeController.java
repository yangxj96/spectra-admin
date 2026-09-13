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
import com.devops00.spectra.core.upload.javabean.from.FileTypePolicySaveFrom;
import com.devops00.spectra.core.upload.javabean.vo.FileTypePolicyVO;
import com.devops00.spectra.core.upload.service.FileTypeManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Validated
public class FileTypeController {

    private final FileTypeManagementService managementService;

    /**
     * 按查询条件分页查询文件类型。
     *
     * @param page 分页参数。
     * @return 符合条件的分页结果。
     */
    @Audit("'分页查询文件类型策略'")
    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:read')")
    public IPage<FileTypePolicyVO> page(PageFrom page) {
        return managementService.page(page.toPage());
    }

    /**
     * 查询文件类型。
     *
     * @param id 数据记录的唯一标识。
     * @return 文件类型策略数据。
     */
    @Audit("'查询文件类型策略'")
    @GetMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:read')")
    public FileTypePolicyVO get(@PathVariable UUID id) {
        return managementService.get(id);
    }

    /**
     * 构建文件类型。
     *
     * @param from 请求表单数据。
     * @return 文件类型策略数据。
     */
    @Audit("'创建文件类型策略'")
    @PostMapping(version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:manage')")
    public FileTypePolicyVO create(@Valid @RequestBody FileTypePolicySaveFrom from) {
        return managementService.create(from);
    }

    /**
     * 更新文件类型。
     *
     * @param id   数据记录的唯一标识。
     * @param from 请求表单数据。
     * @return 文件类型策略数据。
     */
    @Audit("'修改文件类型策略'")
    @PutMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:manage')")
    public FileTypePolicyVO modify(@PathVariable UUID id, @Valid @RequestBody FileTypePolicySaveFrom from) {
        return managementService.modify(id, from);
    }

    /**
     * 处理文件类型相关数据。
     *
     * @param id 数据记录的唯一标识。
     * @return 文件类型策略数据。
     */
    @Audit("'启用文件类型策略'")
    @PostMapping(value = "/{id}/enable", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:manage')")
    public FileTypePolicyVO enable(@PathVariable UUID id) {
        return managementService.enable(id);
    }

    /**
     * 处理文件类型相关数据。
     *
     * @param id 数据记录的唯一标识。
     * @return 文件类型策略数据。
     */
    @Audit("'停用文件类型策略'")
    @PostMapping(value = "/{id}/disable", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:manage')")
    public FileTypePolicyVO disable(@PathVariable UUID id) {
        return managementService.disable(id);
    }
}
