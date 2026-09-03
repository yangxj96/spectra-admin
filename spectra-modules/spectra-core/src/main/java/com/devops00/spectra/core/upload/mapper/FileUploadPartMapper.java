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
import com.devops00.spectra.core.upload.javabean.entity.FileUploadPart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.UUID;

/** 上传分片 Mapper。 */
@Mapper
public interface FileUploadPartMapper extends BaseMapper<FileUploadPart> {

    @Select("SELECT * FROM spectra_core.file_upload_part WHERE upload_session_id = #{sessionId} "
            + "AND deleted IS NULL ORDER BY part_number")
    List<FileUploadPart> findBySessionId(@Param("sessionId") UUID sessionId);

    @Select("SELECT * FROM spectra_core.file_upload_part WHERE upload_session_id = #{sessionId} "
            + "AND part_number = #{partNumber} AND deleted IS NULL FOR UPDATE")
    FileUploadPart selectForUpdate(@Param("sessionId") UUID sessionId, @Param("partNumber") int partNumber);

    @Select("SELECT COUNT(*) FROM spectra_core.file_upload_part WHERE upload_session_id = #{sessionId} "
            + "AND status = 'CONFIRMED' AND deleted IS NULL")
    int countConfirmed(@Param("sessionId") UUID sessionId);

    @Update("UPDATE spectra_core.file_upload_part SET expected_sha256 = #{sha256}, upload_attempt = #{attempt}, "
            + "updated_at = CURRENT_TIMESTAMP WHERE upload_session_id = #{sessionId} AND part_number = #{partNumber} "
            + "AND status <> 'CONFIRMED' AND deleted IS NULL")
    int prepareTarget(@Param("sessionId") UUID sessionId, @Param("partNumber") int partNumber,
                      @Param("sha256") String sha256, @Param("attempt") int attempt);

    @Update("UPDATE spectra_core.file_upload_part SET uploaded_size = #{size}, actual_sha256 = #{sha256}, "
            + "provider_etag = #{etag}, status = 'UPLOADED', uploaded_at = CURRENT_TIMESTAMP, "
            + "updated_at = CURRENT_TIMESTAMP WHERE upload_session_id = #{sessionId} AND part_number = #{partNumber} "
            + "AND status <> 'CONFIRMED' AND deleted IS NULL")
    int markUploaded(@Param("sessionId") UUID sessionId, @Param("partNumber") int partNumber,
                     @Param("size") long size, @Param("sha256") String sha256, @Param("etag") String etag);

    @Update("UPDATE spectra_core.file_upload_part SET status = 'CONFIRMED', uploaded_size = #{size}, "
            + "actual_sha256 = #{sha256}, provider_etag = #{etag}, uploaded_at = COALESCE(uploaded_at, CURRENT_TIMESTAMP), "
            + "updated_at = CURRENT_TIMESTAMP WHERE upload_session_id = #{sessionId} AND part_number = #{partNumber} "
            + "AND status IN ('UPLOADED', 'CONFIRMED') AND deleted IS NULL")
    int markConfirmed(@Param("sessionId") UUID sessionId, @Param("partNumber") int partNumber,
                      @Param("size") long size, @Param("sha256") String sha256, @Param("etag") String etag);

    @Update("UPDATE spectra_core.file_upload_part SET status = 'CONFIRMED', uploaded_size = #{size}, "
            + "actual_sha256 = #{sha256}, provider_etag = #{etag}, uploaded_at = COALESCE(uploaded_at, CURRENT_TIMESTAMP), "
            + "updated_at = CURRENT_TIMESTAMP WHERE upload_session_id = #{sessionId} AND part_number = #{partNumber} "
            + "AND status = 'PENDING' AND deleted IS NULL")
    int markExternalConfirmed(@Param("sessionId") UUID sessionId, @Param("partNumber") int partNumber,
                              @Param("size") long size, @Param("sha256") String sha256, @Param("etag") String etag);
}
