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

package com.devops00.spectra.upload.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.upload.javabean.entity.FileReference;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.UUID;

/** 文件引用 Mapper。 */
@Mapper
public interface FileReferenceMapper extends BaseMapper<FileReference> {

    @Select("SELECT * FROM spectra_core.file_reference WHERE file_asset_id = #{fileAssetId} "
            + "AND reference_type = #{referenceType} AND reference_id = #{referenceId} AND purpose = #{purpose} "
            + "AND deleted IS NULL LIMIT 1")
    FileReference findByKey(@Param("fileAssetId") UUID fileAssetId, @Param("referenceType") String referenceType,
                            @Param("referenceId") UUID referenceId, @Param("purpose") String purpose);

    @Select("SELECT * FROM spectra_core.file_reference WHERE reference_type = #{referenceType} "
            + "AND reference_id = #{referenceId} AND purpose = #{purpose} AND deleted IS NULL LIMIT 1")
    FileReference findByBusinessKey(@Param("referenceType") String referenceType, @Param("referenceId") UUID referenceId,
                                    @Param("purpose") String purpose);

    @Select("SELECT COUNT(*) FROM spectra_core.file_reference WHERE file_asset_id = #{fileAssetId} AND deleted IS NULL")
    int countByAssetId(@Param("fileAssetId") UUID fileAssetId);

    @Update("UPDATE spectra_core.file_reference SET deleted = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP "
            + "WHERE file_asset_id = #{fileAssetId} AND reference_type = #{referenceType} AND reference_id = #{referenceId} "
            + "AND purpose = #{purpose} AND deleted IS NULL")
    int softDeleteByKey(@Param("fileAssetId") UUID fileAssetId, @Param("referenceType") String referenceType,
                        @Param("referenceId") UUID referenceId, @Param("purpose") String purpose);

    @Update("UPDATE spectra_core.file_reference SET deleted = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP "
            + "WHERE reference_type = #{referenceType} AND reference_id = #{referenceId} AND purpose = #{purpose} AND deleted IS NULL")
    int softDeleteByBusinessKeyAndPurpose(@Param("referenceType") String referenceType, @Param("referenceId") UUID referenceId,
                                          @Param("purpose") String purpose);

    @Update("UPDATE spectra_core.file_reference SET deleted = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP "
            + "WHERE id = #{id} AND deleted IS NULL")
    int softDeleteById(@Param("id") UUID id);

    @Update("UPDATE spectra_core.file_reference SET deleted = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP "
            + "WHERE reference_type = #{referenceType} AND reference_id = #{referenceId} AND deleted IS NULL")
    int softDeleteByBusinessKey(@Param("referenceType") String referenceType, @Param("referenceId") UUID referenceId);
}
