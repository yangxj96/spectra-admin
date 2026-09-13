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
import com.devops00.spectra.core.upload.javabean.entity.FileUploadSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** 上传会话 Mapper。 */
@Mapper
public interface FileUploadSessionMapper extends BaseMapper<FileUploadSession> {

    int countActiveByOwner(@Param("ownerUserId") UUID ownerUserId);

    FileUploadSession findResumable(@Param("ownerUserId") UUID ownerUserId, @Param("sha256") String sha256,
                                    @Param("size") long size, @Param("now") Instant now,
                                    @Param("idleBefore") Instant idleBefore);

    FileUploadSession selectForUpdate(@Param("id") UUID id);

    List<FileUploadSession> findExpiredCandidates(@Param("now") Instant now, @Param("idleBefore") Instant idleBefore,
                                                  @Param("limit") int limit);

    List<FileUploadSession> findCleanupCandidates(@Param("now") Instant now, @Param("limit") int limit);

    int claimCleanupCandidate(@Param("id") UUID id, @Param("now") Instant now,
                              @Param("claimUntil") Instant claimUntil);

    int markExpired(@Param("id") UUID id, @Param("nextCleanupAt") Instant nextCleanupAt);

    int claimForVerification(@Param("id") UUID id, @Param("startedAt") Instant startedAt);

    int markReady(@Param("id") UUID id, @Param("fileAssetId") UUID fileAssetId, @Param("completedAt") Instant completedAt);

    int markFailed(@Param("id") UUID id, @Param("failureCode") String failureCode, @Param("nextCleanupAt") Instant nextCleanupAt);

    int markCanceled(@Param("id") UUID id, @Param("nextCleanupAt") Instant nextCleanupAt);

    int updateVerificationProgress(@Param("id") UUID id, @Param("bytes") long bytes);

    int touchActivity(@Param("id") UUID id, @Param("lastActivityAt") Instant lastActivityAt);

    int markCleanupRetry(@Param("id") UUID id, @Param("nextCleanupAt") Instant nextCleanupAt);

    int markCleaned(@Param("id") UUID id, @Param("cleanedAt") Instant cleanedAt);

    int deleteByIdPhysically(@Param("id") UUID id, @Param("cutoff") Instant cutoff);

    int deletePartsPhysically(@Param("id") UUID id);
}
