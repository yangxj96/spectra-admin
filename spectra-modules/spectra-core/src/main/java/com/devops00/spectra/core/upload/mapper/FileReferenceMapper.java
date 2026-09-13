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

/**
 * 文件引用 Mapper。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Mapper
public interface FileReferenceMapper extends BaseMapper<FileReference> {

    /**
     * 查询键。
     *
     * @param fileAssetId   文件资产标识。
     * @param referenceType 引用类型参数。
     * @param referenceId   引用标识。
     * @param purpose       用途参数。
     * @return 文件引用数据。
     */
    FileReference findByKey(@Param("fileAssetId") UUID fileAssetId, @Param("referenceType") String referenceType,
                            @Param("referenceId") UUID referenceId, @Param("purpose") String purpose);

    /**
     * 查询业务键。
     *
     * @param referenceType 引用类型参数。
     * @param referenceId   引用标识。
     * @param purpose       用途参数。
     * @return 文件引用数据。
     */
    FileReference findByBusinessKey(@Param("referenceType") String referenceType, @Param("referenceId") UUID referenceId,
                                    @Param("purpose") String purpose);

    /**
     * 统计资产标识数量。
     *
     * @param fileAssetId 文件资产标识。
     * @return 符合条件的数量。
     */
    int countByAssetId(@Param("fileAssetId") UUID fileAssetId);

    /**
     * 处理删除键相关数据。
     *
     * @param fileAssetId   文件资产标识。
     * @param referenceType 引用类型参数。
     * @param referenceId   引用标识。
     * @param purpose       用途参数。
     * @return 符合条件的数量。
     */
    int softDeleteByKey(@Param("fileAssetId") UUID fileAssetId, @Param("referenceType") String referenceType,
                        @Param("referenceId") UUID referenceId, @Param("purpose") String purpose);

    /**
     * 处理删除业务键用途相关数据。
     *
     * @param referenceType 引用类型参数。
     * @param referenceId   引用标识。
     * @param purpose       用途参数。
     * @return 符合条件的数量。
     */
    int softDeleteByBusinessKeyAndPurpose(@Param("referenceType") String referenceType, @Param("referenceId") UUID referenceId,
                                          @Param("purpose") String purpose);

    /**
     * 处理删除标识相关数据。
     *
     * @param id 数据记录的唯一标识。
     * @return 符合条件的数量。
     */
    int softDeleteById(@Param("id") UUID id);

    /**
     * 处理删除业务键相关数据。
     *
     * @param referenceType 引用类型参数。
     * @param referenceId   引用标识。
     * @return 符合条件的数量。
     */
    int softDeleteByBusinessKey(@Param("referenceType") String referenceType, @Param("referenceId") UUID referenceId);
}
