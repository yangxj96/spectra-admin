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

package com.devops00.spectra.oa.reimbursement.javabean.converter;

import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.application.javabean.entity.ApplicationAttachment;
import com.devops00.spectra.oa.reimbursement.javabean.entity.Reimbursement;
import com.devops00.spectra.oa.reimbursement.javabean.entity.ReimbursementItem;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementAttachmentFrom;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementItemFrom;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementSaveFrom;
import com.devops00.spectra.oa.reimbursement.javabean.vo.ReimbursementAttachmentVO;
import com.devops00.spectra.oa.reimbursement.javabean.vo.ReimbursementItemVO;
import com.devops00.spectra.oa.reimbursement.javabean.vo.ReimbursementVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/**
 * 报销 MapStruct 转换器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface ReimbursementConverter {
    /**
     * 报销单实体转视图对象。
     */
    ReimbursementVO toVO(Reimbursement source);

    /**
     * 报销保存入参转实体。
     */
    Reimbursement toEntity(ReimbursementSaveFrom source);

    /**
     * 使用保存入参更新报销实体。
     */
    void updateEntity(ReimbursementSaveFrom source, @MappingTarget Reimbursement target);

    /**
     * 报销明细入参转实体。
     */
    ReimbursementItem toItemEntity(ReimbursementItemFrom source);

    /**
     * 报销明细实体转视图对象。
     */
    ReimbursementItemVO toItemVO(ReimbursementItem source);

    /**
     * 报销附件入参转通用附件实体。
     */
    ApplicationAttachment toAttachmentEntity(ReimbursementAttachmentFrom source);

    /**
     * 通用附件实体转报销附件视图对象。
     */
    @Mapping(target = "previewUrl", source = "fileId", qualifiedByName = "toPreviewUrl")
    ReimbursementAttachmentVO toAttachmentVO(ApplicationAttachment source);

    /**
     * 将文件 ID 转换为文件预览地址。
     */
    @Named("toPreviewUrl")
    default String toPreviewUrl(java.util.UUID fileId) {
        return fileId == null ? null : "/api/file/upload/preview/" + fileId;
    }
}
