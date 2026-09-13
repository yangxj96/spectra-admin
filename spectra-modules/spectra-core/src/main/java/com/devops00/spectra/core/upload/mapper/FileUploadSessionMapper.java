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

/**
 * 上传会话 Mapper。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Mapper
public interface FileUploadSessionMapper extends BaseMapper<FileUploadSession> {

    /**
     * 统计活动状态所有者数量。
     *
     * @param ownerUserId 所有者用户标识。
     * @return 符合条件的数量。
     */
    int countActiveByOwner(@Param("ownerUserId") UUID ownerUserId);

    /**
     * 查询可恢复状态。
     *
     * @param ownerUserId 所有者用户标识。
     * @param sha256      文件内容的 SHA-256 摘要。
     * @param size        大小参数。
     * @param now         用于计算有效期的当前时刻。
     * @param idleBefore  筛选空闲时间早于该时刻的会话。
     * @return 文件上传会话数据。
     */
    FileUploadSession findResumable(@Param("ownerUserId") UUID ownerUserId, @Param("sha256") String sha256,
                                    @Param("size") long size, @Param("now") Instant now,
                                    @Param("idleBefore") Instant idleBefore);

    /**
     * 查询更新。
     *
     * @param id 数据记录的唯一标识。
     * @return 文件上传会话数据。
     */
    FileUploadSession selectForUpdate(@Param("id") UUID id);

    /**
     * 查询过期状态。
     *
     * @param now        用于计算有效期的当前时刻。
     * @param idleBefore 筛选空闲时间早于该时刻的会话。
     * @param limit      本次查询允许返回的最大记录数。
     * @return 符合条件的数据集合。
     */
    List<FileUploadSession> findExpiredCandidates(@Param("now") Instant now, @Param("idleBefore") Instant idleBefore,
                                                  @Param("limit") int limit);

    /**
     * 查询清理。
     *
     * @param now   用于计算有效期的当前时刻。
     * @param limit 本次查询允许返回的最大记录数。
     * @return 符合条件的数据集合。
     */
    List<FileUploadSession> findCleanupCandidates(@Param("now") Instant now, @Param("limit") int limit);

    /**
     * 处理清理候选项相关数据。
     *
     * @param id         数据记录的唯一标识。
     * @param now        用于计算有效期的当前时刻。
     * @param claimUntil 上传声明的有效截止时间。
     * @return 符合条件的数量。
     */
    int claimCleanupCandidate(@Param("id") UUID id, @Param("now") Instant now,
                              @Param("claimUntil") Instant claimUntil);

    /**
     * 设置过期状态。
     *
     * @param id            数据记录的唯一标识。
     * @param nextCleanupAt 下一清理参数。
     * @return 符合条件的数量。
     */
    int markExpired(@Param("id") UUID id, @Param("nextCleanupAt") Instant nextCleanupAt);

    /**
     * 处理文件上传会话相关数据。
     *
     * @param id        数据记录的唯一标识。
     * @param startedAt 上传或处理任务的开始时间。
     * @return 符合条件的数量。
     */
    int claimForVerification(@Param("id") UUID id, @Param("startedAt") Instant startedAt);

    /**
     * 设置就绪状态。
     *
     * @param id          数据记录的唯一标识。
     * @param fileAssetId 文件资产标识。
     * @param completedAt 操作完成时间。
     * @return 符合条件的数量。
     */
    int markReady(@Param("id") UUID id, @Param("fileAssetId") UUID fileAssetId, @Param("completedAt") Instant completedAt);

    /**
     * 设置文件上传会话。
     *
     * @param id            数据记录的唯一标识。
     * @param failureCode   失败编码参数。
     * @param nextCleanupAt 下一清理参数。
     * @return 符合条件的数量。
     */
    int markFailed(@Param("id") UUID id, @Param("failureCode") String failureCode, @Param("nextCleanupAt") Instant nextCleanupAt);

    /**
     * 设置文件上传会话。
     *
     * @param id            数据记录的唯一标识。
     * @param nextCleanupAt 下一清理参数。
     * @return 符合条件的数量。
     */
    int markCanceled(@Param("id") UUID id, @Param("nextCleanupAt") Instant nextCleanupAt);

    /**
     * 更新文件上传会话。
     *
     * @param id    数据记录的唯一标识。
     * @param bytes 字节数参数。
     * @return 符合条件的数量。
     */
    int updateVerificationProgress(@Param("id") UUID id, @Param("bytes") long bytes);

    /**
     * 更新文件上传会话。
     *
     * @param id             数据记录的唯一标识。
     * @param lastActivityAt 最后一个参数。
     * @return 符合条件的数量。
     */
    int touchActivity(@Param("id") UUID id, @Param("lastActivityAt") Instant lastActivityAt);

    /**
     * 设置清理重试。
     *
     * @param id            数据记录的唯一标识。
     * @param nextCleanupAt 下一清理参数。
     * @return 符合条件的数量。
     */
    int markCleanupRetry(@Param("id") UUID id, @Param("nextCleanupAt") Instant nextCleanupAt);

    /**
     * 设置文件上传会话。
     *
     * @param id        数据记录的唯一标识。
     * @param cleanedAt 上传会话的清理时间。
     * @return 符合条件的数量。
     */
    int markCleaned(@Param("id") UUID id, @Param("cleanedAt") Instant cleanedAt);

    /**
     * 删除或清理标识。
     *
     * @param id     数据记录的唯一标识。
     * @param cutoff 清理操作使用的截止时间。
     * @return 符合条件的数量。
     */
    int deleteByIdPhysically(@Param("id") UUID id, @Param("cutoff") Instant cutoff);

    /**
     * 删除或清理部分。
     *
     * @param id 数据记录的唯一标识。
     * @return 符合条件的数量。
     */
    int deletePartsPhysically(@Param("id") UUID id);
}
