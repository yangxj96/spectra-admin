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
import com.devops00.spectra.upload.javabean.entity.FileUploadSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** 上传会话 Mapper。 */
@Mapper
public interface FileUploadSessionMapper extends BaseMapper<FileUploadSession> {

    @Select("SELECT COUNT(*) FROM spectra_core.file_upload_session WHERE owner_user_id = #{ownerUserId} "
            + "AND status IN ('UPLOADING', 'VERIFYING') AND expires_at > CURRENT_TIMESTAMP AND deleted IS NULL")
    int countActiveByOwner(@Param("ownerUserId") UUID ownerUserId);

    @Select("SELECT * FROM spectra_core.file_upload_session WHERE owner_user_id = #{ownerUserId} "
            + "AND content_sha256 = #{sha256} AND size = #{size} AND status IN ('UPLOADING', 'VERIFYING') "
            + "AND expires_at > #{now} AND last_activity_at > #{idleBefore} AND deleted IS NULL "
            + "ORDER BY created_at DESC LIMIT 1")
    FileUploadSession findResumable(@Param("ownerUserId") UUID ownerUserId, @Param("sha256") String sha256,
                                    @Param("size") long size, @Param("now") Instant now,
                                    @Param("idleBefore") Instant idleBefore);

    @Select("SELECT * FROM spectra_core.file_upload_session WHERE id = #{id} AND deleted IS NULL FOR UPDATE")
    FileUploadSession selectForUpdate(@Param("id") UUID id);

    @Select("SELECT * FROM spectra_core.file_upload_session WHERE status IN ('UPLOADING', 'VERIFYING') "
            + "AND deleted IS NULL AND (expires_at <= #{now} "
            + "OR (status = 'UPLOADING' AND last_activity_at <= #{idleBefore})) "
            + "ORDER BY expires_at LIMIT #{limit} FOR UPDATE SKIP LOCKED")
    List<FileUploadSession> findExpiredCandidates(@Param("now") Instant now, @Param("idleBefore") Instant idleBefore,
                                                  @Param("limit") int limit);

    @Select("SELECT * FROM spectra_core.file_upload_session WHERE status IN ('FAILED', 'CANCELED', 'EXPIRED', 'CLEANED') "
            + "AND deleted IS NULL AND next_cleanup_at IS NOT NULL AND next_cleanup_at <= #{now} "
            + "ORDER BY next_cleanup_at LIMIT #{limit} FOR UPDATE SKIP LOCKED")
    List<FileUploadSession> findCleanupCandidates(@Param("now") Instant now, @Param("limit") int limit);

    @Update("UPDATE spectra_core.file_upload_session SET next_cleanup_at = #{claimUntil}, "
            + "updated_at = CURRENT_TIMESTAMP WHERE id = #{id} AND status IN "
            + "('FAILED', 'CANCELED', 'EXPIRED', 'CLEANED') AND deleted IS NULL "
            + "AND next_cleanup_at IS NOT NULL AND next_cleanup_at <= #{now}")
    int claimCleanupCandidate(@Param("id") UUID id, @Param("now") Instant now,
                              @Param("claimUntil") Instant claimUntil);

    @Update("UPDATE spectra_core.file_upload_session SET status = 'EXPIRED', failure_code = 'FILE_UPLOAD_EXPIRED', "
            + "next_cleanup_at = #{nextCleanupAt}, updated_at = CURRENT_TIMESTAMP "
            + "WHERE id = #{id} AND status IN ('UPLOADING', 'VERIFYING') AND deleted IS NULL")
    int markExpired(@Param("id") UUID id, @Param("nextCleanupAt") Instant nextCleanupAt);

    @Update("UPDATE spectra_core.file_upload_session SET status = 'VERIFYING', verify_started_at = #{startedAt}, "
            + "verify_total_bytes = size, verify_processed_bytes = 0, updated_at = CURRENT_TIMESTAMP "
            + "WHERE id = #{id} AND status = 'UPLOADING' AND deleted IS NULL")
    int claimForVerification(@Param("id") UUID id, @Param("startedAt") Instant startedAt);

    @Update("UPDATE spectra_core.file_upload_session SET status = 'READY', file_asset_id = #{fileAssetId}, "
            + "completed_at = #{completedAt}, verify_finished_at = #{completedAt}, verify_processed_bytes = size, "
            + "updated_at = CURRENT_TIMESTAMP WHERE id = #{id} AND status = 'VERIFYING' AND deleted IS NULL")
    int markReady(@Param("id") UUID id, @Param("fileAssetId") UUID fileAssetId, @Param("completedAt") Instant completedAt);

    @Update("UPDATE spectra_core.file_upload_session SET status = 'FAILED', failure_code = #{failureCode}, "
            + "verify_finished_at = CURRENT_TIMESTAMP, next_cleanup_at = #{nextCleanupAt}, updated_at = CURRENT_TIMESTAMP "
            + "WHERE id = #{id} AND status IN ('UPLOADING', 'VERIFYING') AND deleted IS NULL")
    int markFailed(@Param("id") UUID id, @Param("failureCode") String failureCode, @Param("nextCleanupAt") Instant nextCleanupAt);

    @Update("UPDATE spectra_core.file_upload_session SET status = 'CANCELED', next_cleanup_at = #{nextCleanupAt}, "
            + "updated_at = CURRENT_TIMESTAMP WHERE id = #{id} AND status = 'UPLOADING' AND deleted IS NULL")
    int markCanceled(@Param("id") UUID id, @Param("nextCleanupAt") Instant nextCleanupAt);

    @Update("UPDATE spectra_core.file_upload_session SET verify_processed_bytes = LEAST(verify_total_bytes, #{bytes}), "
            + "updated_at = CURRENT_TIMESTAMP WHERE id = #{id} AND status = 'VERIFYING'")
    int updateVerificationProgress(@Param("id") UUID id, @Param("bytes") long bytes);

    @Update("UPDATE spectra_core.file_upload_session SET last_activity_at = #{lastActivityAt}, "
            + "updated_at = CURRENT_TIMESTAMP WHERE id = #{id} AND status = 'UPLOADING' AND deleted IS NULL")
    int touchActivity(@Param("id") UUID id, @Param("lastActivityAt") Instant lastActivityAt);

    @Update("UPDATE spectra_core.file_upload_session SET cleanup_attempts = cleanup_attempts + 1, "
            + "next_cleanup_at = #{nextCleanupAt}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int markCleanupRetry(@Param("id") UUID id, @Param("nextCleanupAt") Instant nextCleanupAt);

    @Update("UPDATE spectra_core.file_upload_session SET status = 'CLEANED', next_cleanup_at = #{cleanedAt}, "
            + "updated_at = CURRENT_TIMESTAMP WHERE id = #{id} AND status IN "
            + "('FAILED', 'CANCELED', 'EXPIRED', 'CLEANED')")
    int markCleaned(@Param("id") UUID id, @Param("cleanedAt") Instant cleanedAt);

    @Delete("DELETE FROM spectra_core.file_upload_session WHERE id = #{id} AND status = 'CLEANED' "
            + "AND next_cleanup_at IS NOT NULL AND next_cleanup_at <= #{cutoff}")
    int deleteByIdPhysically(@Param("id") UUID id, @Param("cutoff") Instant cutoff);

    @Delete("DELETE FROM spectra_core.file_upload_part WHERE upload_session_id = #{id}")
    int deletePartsPhysically(@Param("id") UUID id);
}
