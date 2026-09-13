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

package com.devops00.spectra.core.upload.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.upload.javabean.entity.FileReference;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

/** 文件引用 Mapper。 */
@Mapper
public interface FileReferenceMapper extends BaseMapper<FileReference> {

    FileReference findByKey(@Param("fileAssetId") UUID fileAssetId, @Param("referenceType") String referenceType,
                            @Param("referenceId") UUID referenceId, @Param("purpose") String purpose);

    FileReference findByBusinessKey(@Param("referenceType") String referenceType, @Param("referenceId") UUID referenceId,
                                    @Param("purpose") String purpose);

    int countByAssetId(@Param("fileAssetId") UUID fileAssetId);

    int softDeleteByKey(@Param("fileAssetId") UUID fileAssetId, @Param("referenceType") String referenceType,
                        @Param("referenceId") UUID referenceId, @Param("purpose") String purpose);

    int softDeleteByBusinessKeyAndPurpose(@Param("referenceType") String referenceType, @Param("referenceId") UUID referenceId,
                                          @Param("purpose") String purpose);

    int softDeleteById(@Param("id") UUID id);

    int softDeleteByBusinessKey(@Param("referenceType") String referenceType, @Param("referenceId") UUID referenceId);
}
