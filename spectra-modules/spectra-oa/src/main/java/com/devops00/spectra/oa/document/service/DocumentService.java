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

package com.devops00.spectra.oa.document.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.oa.document.javabean.entity.Document;
import com.devops00.spectra.oa.document.javabean.from.DocumentFolderSaveFrom;
import com.devops00.spectra.oa.document.javabean.from.DocumentPageFrom;
import com.devops00.spectra.oa.document.javabean.from.DocumentSaveFrom;
import com.devops00.spectra.oa.document.javabean.from.DocumentVersionFrom;
import com.devops00.spectra.oa.document.javabean.vo.DocumentFolderVO;
import com.devops00.spectra.oa.document.javabean.vo.DocumentVO;
import com.devops00.spectra.oa.document.javabean.vo.DocumentVersionVO;

import java.util.List;
import java.util.UUID;

/**
 * 文档表主表-服务
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/3/30 14:12
 */
public interface DocumentService extends BaseService<Document> {
    /**
     * 分页查询文档。
     */
    IPage<DocumentVO> page(PageFrom page, DocumentPageFrom params);

    /**
     * 查询文档详情。
     */
    DocumentVO get(UUID id);

    /**
     * 创建文档。
     */
    UUID created(DocumentSaveFrom from);

    /**
     * 修改文档。
     */
    void modify(UUID id, DocumentSaveFrom from);

    /**
     * 新增文档版本。
     */
    UUID addVersion(UUID id, DocumentVersionFrom from);

    /**
     * 查询文档版本列表。
     */
    List<DocumentVersionVO> versions(UUID id);

    /**
     * 发布文档。
     */
    void publish(UUID id);

    /**
     * 归档文档。
     */
    void archive(UUID id);

    /**
     * 查询文档目录。
     */
    List<DocumentFolderVO> folders();

    /**
     * 创建文档目录。
     */
    UUID createFolder(DocumentFolderSaveFrom from);

    /**
     * 恢复文档当前版本。
     */
    void restoreVersion(UUID id, UUID versionId);
}
