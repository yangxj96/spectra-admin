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

package com.devops00.spectra.oa.document.javabean.converter;

import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.document.javabean.entity.Document;
import com.devops00.spectra.oa.document.javabean.entity.DocumentFolder;
import com.devops00.spectra.oa.document.javabean.entity.DocumentVersion;
import com.devops00.spectra.oa.document.javabean.from.DocumentSaveFrom;
import com.devops00.spectra.oa.document.javabean.vo.DocumentFolderVO;
import com.devops00.spectra.oa.document.javabean.vo.DocumentVersionVO;
import com.devops00.spectra.oa.document.javabean.vo.DocumentVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/// 文档相关对象转换器。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/8
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface DocumentConverter {

    /// 文档实体转详情视图。
    @Mapping(target = "id", source = "document.id")
    @Mapping(target = "createdAt", source = "document.createdAt")
    @Mapping(target = "updatedAt", source = "document.updatedAt")
    @Mapping(target = "currentVersion", source = "version")
    DocumentVO toVO(Document document, DocumentVersion version);

    /// 文档版本实体转视图。
    @Mapping(target = "current", source = "currentVersion")
    DocumentVersionVO toVersionVO(DocumentVersion source);

    /// 文档目录实体转视图。
    DocumentFolderVO toFolderVO(DocumentFolder source);

    /// 保存入参转文档实体。
    Document toEntity(DocumentSaveFrom source);

    /// 使用保存入参更新文档实体。
    void updateEntity(DocumentSaveFrom source, @MappingTarget Document target);
}
