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

/**
 * 提供文件资产相关的 HTTP 接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@RestController
@RequestMapping("/file/assets")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasPermission(null, 'file:admin:read')")
public class FileAssetController {

    private final FileAssetApplicationService assetService;

    /**
     * 按查询条件分页查询文件资产。
     *
     * @param page    分页参数。
     * @param request 请求参数。
     * @return 符合条件的分页结果。
     */
    @Audit("'分页查询文件资产'")
    @GetMapping(value = "/page", version = "1.0.0")
    public IPage<FileAssetVO> page(PageFrom page, FileAssetPageRequest request) {
        return assetService.page(page.toPage(), request);
    }

    /**
     * 删除或清理文件资产。
     *
     * @param fileAssetId 文件资产标识。
     */
    @Audit("'删除文件资产'")
    @DeleteMapping(value = "/{fileAssetId}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:admin:delete')")
    public void delete(@PathVariable UUID fileAssetId) {
        assetService.delete(fileAssetId);
    }
}
