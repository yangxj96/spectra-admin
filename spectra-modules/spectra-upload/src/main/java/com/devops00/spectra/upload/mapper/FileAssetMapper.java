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
import com.devops00.spectra.upload.javabean.entity.FileAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** 文件资产 Mapper。 */
@Mapper
public interface FileAssetMapper extends BaseMapper<FileAsset> {

    @Select("SELECT * FROM spectra_core.file_asset WHERE content_sha256 = #{sha256} AND size = #{size} "
            + "AND status = 'READY' AND deleted IS NULL LIMIT 1")
    FileAsset findReady(@Param("sha256") String sha256, @Param("size") long size);

    @Select("SELECT * FROM spectra_core.file_asset WHERE status IN ('READY', 'DELETING') AND deleted IS NULL "
            + "AND orphaned_at IS NOT NULL AND next_cleanup_at IS NOT NULL AND next_cleanup_at <= #{now} "
            + "ORDER BY next_cleanup_at LIMIT #{limit} FOR UPDATE SKIP LOCKED")
    List<FileAsset> findCleanupCandidates(@Param("now") Instant now, @Param("limit") int limit);

    @Update("UPDATE spectra_core.file_asset SET next_cleanup_at = #{claimUntil}, "
            + "updated_at = CURRENT_TIMESTAMP WHERE id = #{id} AND status IN ('READY', 'DELETING') "
            + "AND deleted IS NULL AND next_cleanup_at IS NOT NULL AND next_cleanup_at <= #{now}")
    int claimCleanupCandidate(@Param("id") UUID id, @Param("now") Instant now,
                              @Param("claimUntil") Instant claimUntil);

    @Select("SELECT a.* FROM spectra_core.file_asset a WHERE a.status = 'READY' AND a.deleted IS NULL "
            + "AND a.orphaned_at IS NULL AND a.completed_at <= #{cutoff} "
            + "AND NOT EXISTS (SELECT 1 FROM spectra_core.file_reference r WHERE r.file_asset_id = a.id AND r.deleted IS NULL) "
            + "ORDER BY a.completed_at LIMIT #{limit} FOR UPDATE SKIP LOCKED")
    List<FileAsset> findOrphanCandidates(@Param("cutoff") Instant cutoff, @Param("limit") int limit);

    @org.apache.ibatis.annotations.Delete("DELETE FROM spectra_core.file_asset WHERE id = #{id} AND status = 'DELETED'")
    int deleteByIdPhysically(@Param("id") UUID id);

    @Update("UPDATE spectra_core.file_asset SET status = 'DELETING', updated_at = CURRENT_TIMESTAMP "
            + "WHERE id = #{id} AND status = 'READY' AND deleted IS NULL")
    int markDeleting(@Param("id") UUID id);

    @Update("UPDATE spectra_core.file_asset SET status = 'DELETED', deleted = CURRENT_TIMESTAMP, "
            + "updated_at = CURRENT_TIMESTAMP WHERE id = #{id} AND status = 'DELETING'")
    int markDeleted(@Param("id") UUID id);

    @Update("UPDATE spectra_core.file_asset SET cleanup_attempts = cleanup_attempts + 1, "
            + "next_cleanup_at = #{nextCleanupAt}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int markCleanupRetry(@Param("id") UUID id, @Param("nextCleanupAt") Instant nextCleanupAt);

    @Update("UPDATE spectra_core.file_asset SET orphaned_at = COALESCE(orphaned_at, CURRENT_TIMESTAMP), "
            + "next_cleanup_at = COALESCE(next_cleanup_at, #{nextCleanupAt}), updated_at = CURRENT_TIMESTAMP "
            + "WHERE id = #{id} AND status = 'READY' AND deleted IS NULL")
    int markOrphaned(@Param("id") UUID id, @Param("nextCleanupAt") Instant nextCleanupAt);
}
