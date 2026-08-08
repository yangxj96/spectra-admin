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

package com.devops00.spectra.oa.document.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.document.javabean.from.DocumentFolderSaveFrom;
import com.devops00.spectra.oa.document.javabean.from.DocumentPageFrom;
import com.devops00.spectra.oa.document.javabean.from.DocumentSaveFrom;
import com.devops00.spectra.oa.document.javabean.from.DocumentVersionFrom;
import com.devops00.spectra.oa.document.javabean.vo.DocumentFolderVO;
import com.devops00.spectra.oa.document.javabean.vo.DocumentVersionVO;
import com.devops00.spectra.oa.document.javabean.vo.DocumentVO;
import com.devops00.spectra.oa.document.service.DocumentService;
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

import java.util.List;
import java.util.UUID;

/// 文档管理主接口
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/5 23:22
@Slf4j
@RestController
@RequestMapping("/oa/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService bindService;

    /// 分页查询文档。
    @ULog("'分页查询文档'")
    @GetMapping(value = "/page", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_DOCUMENT:QUERY')")
    public IPage<DocumentVO> page(PageFrom page, DocumentPageFrom params) {
        return bindService.page(page, params);
    }

    /// 查询文档详情。
    @ULog("'查询文档详情'")
    @GetMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_DOCUMENT:QUERY')")
    public DocumentVO get(@PathVariable UUID id) { return bindService.get(id); }

    /// 创建文档。
    @ULog("'创建文档'")
    @PostMapping(version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_DOCUMENT:INSERT')")
    public UUID create(@Validated(Verify.Insert.class) @RequestBody DocumentSaveFrom from) { return bindService.created(from); }

    /// 修改文档。
    @ULog("'修改文档'")
    @PutMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_DOCUMENT:UPDATE')")
    public void update(@PathVariable UUID id, @Validated(Verify.Update.class) @RequestBody DocumentSaveFrom from) { bindService.modify(id, from); }

    /// 新增文档版本。
    @ULog("'新增文档版本'")
    @PostMapping(value = "/{id}/versions", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_DOCUMENT:UPDATE')")
    public UUID addVersion(@PathVariable UUID id, @Validated @RequestBody DocumentVersionFrom from) { return bindService.addVersion(id, from); }

    /// 查询文档版本。
    @ULog("'查询文档版本'")
    @GetMapping(value = "/{id}/versions", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_DOCUMENT:QUERY')")
    public List<DocumentVersionVO> versions(@PathVariable UUID id) { return bindService.versions(id); }

    /// 发布文档。
    @ULog("'发布文档'")
    @PostMapping(value = "/{id}/publish", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_DOCUMENT:UPDATE')")
    public void publish(@PathVariable UUID id) { bindService.publish(id); }

    /// 归档文档。
    @ULog("'归档文档'")
    @PostMapping(value = "/{id}/archive", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_DOCUMENT:UPDATE')")
    public void archive(@PathVariable UUID id) { bindService.archive(id); }

    /// 查询文档目录。
    @ULog("'查询文档目录'")
    @GetMapping(value = "/folders", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_DOCUMENT:QUERY')")
    public List<DocumentFolderVO> folders() { return bindService.folders(); }

    /// 创建文档目录。
    @ULog("'创建文档目录'")
    @PostMapping(value = "/folders", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_DOCUMENT:INSERT')")
    public UUID createFolder(@Validated(Verify.Insert.class) @RequestBody DocumentFolderSaveFrom from) { return bindService.createFolder(from); }

    /// 预览文档版本。
    @ULog("'预览文档版本'")
    @GetMapping(value = "/{id}/versions/{versionId}/preview", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_DOCUMENT:QUERY')")
    public void preview(@PathVariable UUID id, @PathVariable UUID versionId) { bindService.preview(id, versionId); }

    /// 下载文档版本。
    @ULog("'下载文档版本'")
    @GetMapping(value = "/{id}/versions/{versionId}/download", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_DOCUMENT:QUERY')")
    public void download(@PathVariable UUID id, @PathVariable UUID versionId) { bindService.download(id, versionId); }

    /// 恢复文档版本。
    @ULog("'恢复文档版本'")
    @PutMapping(value = "/{id}/versions/{versionId}/current", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_DOCUMENT:UPDATE')")
    public void restoreVersion(@PathVariable UUID id, @PathVariable UUID versionId) {
        bindService.restoreVersion(id, versionId);
    }

}
