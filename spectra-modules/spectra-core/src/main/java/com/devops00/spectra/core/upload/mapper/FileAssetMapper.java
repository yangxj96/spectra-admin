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
import com.devops00.spectra.core.upload.javabean.entity.FileAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** 文件资产 Mapper。 */
@Mapper
public interface FileAssetMapper extends BaseMapper<FileAsset> {

    FileAsset findReady(@Param("sha256") String sha256, @Param("size") long size);

    List<FileAsset> findCleanupCandidates(@Param("now") Instant now, @Param("limit") int limit);

    int claimCleanupCandidate(@Param("id") UUID id, @Param("now") Instant now,
                              @Param("claimUntil") Instant claimUntil);

    List<FileAsset> findOrphanCandidates(@Param("cutoff") Instant cutoff, @Param("limit") int limit);

    int deleteByIdPhysically(@Param("id") UUID id);

    int markDeleting(@Param("id") UUID id);

    int markDeleted(@Param("id") UUID id);

    int markCleanupRetry(@Param("id") UUID id, @Param("nextCleanupAt") Instant nextCleanupAt);

    int markOrphaned(@Param("id") UUID id, @Param("nextCleanupAt") Instant nextCleanupAt);
}
