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

/**
 * 文件资产 Mapper。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Mapper
public interface FileAssetMapper extends BaseMapper<FileAsset> {

    /**
     * 查询就绪状态。
     *
     * @param sha256 文件内容的 SHA-256 摘要。
     * @param size   大小参数。
     * @return 符合条件的数据集合。
     */
    FileAsset findReady(@Param("sha256") String sha256, @Param("size") long size);

    /**
     * 查询清理。
     *
     * @param now   用于计算有效期的当前时刻。
     * @param limit 本次查询允许返回的最大记录数。
     * @return 符合条件的数据集合。
     */
    List<FileAsset> findCleanupCandidates(@Param("now") Instant now, @Param("limit") int limit);

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
     * 查询孤立对象。
     *
     * @param cutoff 清理操作使用的截止时间。
     * @param limit  本次查询允许返回的最大记录数。
     * @return 符合条件的数据集合。
     */
    List<FileAsset> findOrphanCandidates(@Param("cutoff") Instant cutoff, @Param("limit") int limit);

    /**
     * 删除或清理标识。
     *
     * @param id 数据记录的唯一标识。
     * @return 符合条件的数量。
     */
    int deleteByIdPhysically(@Param("id") UUID id);

    /**
     * 设置删除中状态。
     *
     * @param id 数据记录的唯一标识。
     * @return 符合条件的数量。
     */
    int markDeleting(@Param("id") UUID id);

    /**
     * 设置删除状态。
     *
     * @param id 数据记录的唯一标识。
     * @return 符合条件的数量。
     */
    int markDeleted(@Param("id") UUID id);

    /**
     * 设置清理重试。
     *
     * @param id            数据记录的唯一标识。
     * @param nextCleanupAt 下一清理参数。
     * @return 符合条件的数量。
     */
    int markCleanupRetry(@Param("id") UUID id, @Param("nextCleanupAt") Instant nextCleanupAt);

    /**
     * 设置孤立状态。
     *
     * @param id            数据记录的唯一标识。
     * @param nextCleanupAt 下一清理参数。
     * @return 符合条件的数量。
     */
    int markOrphaned(@Param("id") UUID id, @Param("nextCleanupAt") Instant nextCleanupAt);
}
