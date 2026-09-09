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
import com.devops00.spectra.framework.persistence.base.BaseService;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
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
     *
     * @param page   分页条件，包含页码、页大小和排序字段。
     * @param params 文档关键字、发布状态和所属文件夹等分页筛选条件。
     * @return 返回按分页条件查询的OA 文档分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<DocumentVO> page(PageFrom page, DocumentPageFrom params);

    /**
     * 查询文档详情。
     *
     * @param id 目标OA 业务记录的唯一标识。
     * @return 返回符合条件的OA 文档详情；记录不存在或当前用户不可见时抛出业务异常，不返回 null。
     */
    DocumentVO get(UUID id);

    /**
     * 创建文档。
     *
     * @param from 文档所属文件夹、标题、摘要和可见范围等创建字段。
     * @return 返回新建文档的唯一标识，供后续修改、版本管理和发布操作定位文档；校验或写入失败时抛出业务异常，不返回 null。
     */
    UUID created(DocumentSaveFrom from);

    /**
     * 修改文档。
     *
     * @param id   待修改文档的唯一标识。
     * @param from 文档所属文件夹、标题、摘要和可见范围等修改字段。
     */
    void modify(UUID id, DocumentSaveFrom from);

    /**
     * 新增文档版本。
     *
     * @param id   要新增版本的文档唯一标识。
     * @param from 新版本文件资产、文件名、大小、媒体类型和版本说明。
     * @return 返回新建文档版本的唯一标识，供版本查询、发布或恢复操作定位该版本；校验或写入失败时抛出业务异常，不返回 null。
     */
    UUID addVersion(UUID id, DocumentVersionFrom from);

    /**
     * 查询文档版本列表。
     *
     * @param id 要查询版本列表的文档唯一标识。
     * @return 返回符合查询条件的OA 文档版本列表；无匹配时返回空列表，不返回 null。
     */
    List<DocumentVersionVO> versions(UUID id);

    /**
     * 发布文档。
     *
     * @param id 要发布的文档唯一标识；发布前必须存在可用的当前版本。
     */
    void publish(UUID id);

    /**
     * 归档文档。
     *
     * @param id 要归档的文档唯一标识。
     */
    void archive(UUID id);

    /**
     * 查询文档目录。
     *
     * @return 返回符合查询条件的OA 文档文件夹列表；无匹配时返回空列表，不返回 null。
     */
    List<DocumentFolderVO> folders();

    /**
     * 创建文档目录。
     *
     * @param from 文件夹父节点、名称、可见范围和排序值等目录字段。
     * @return 返回新建文档文件夹的唯一标识，供文档归档和目录查询定位文件夹；校验或写入失败时抛出业务异常，不返回 null。
     */
    UUID createFolder(DocumentFolderSaveFrom from);

    /**
     * 恢复文档当前版本。
     *
     * @param id        要恢复历史版本的文档唯一标识。
     * @param versionId 要设为当前版本的文档版本唯一标识。
     */
    void restoreVersion(UUID id, UUID versionId);
}
