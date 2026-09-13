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

import java.util.List;
import java.util.UUID;

/**
 * 上传分片 Mapper。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Mapper
public interface FileUploadPartMapper extends BaseMapper<FileUploadPart> {

    /**
     * 查询会话标识。
     *
     * @param sessionId 会话标识。
     * @return 符合条件的数据集合。
     */
    List<FileUploadPart> findBySessionId(@Param("sessionId") UUID sessionId);

    /**
     * 查询更新。
     *
     * @param sessionId  会话标识。
     * @param partNumber 分片编号参数。
     * @return 文件上传分片数据。
     */
    FileUploadPart selectForUpdate(@Param("sessionId") UUID sessionId, @Param("partNumber") int partNumber);

    /**
     * 统计已确认状态数量。
     *
     * @param sessionId 会话标识。
     * @return 符合条件的数量。
     */
    int countConfirmed(@Param("sessionId") UUID sessionId);

    /**
     * 准备目标。
     *
     * @param sessionId  会话标识。
     * @param partNumber 分片编号参数。
     * @param sha256     文件内容的 SHA-256 摘要。
     * @param attempt    尝试参数。
     * @return 符合条件的数量。
     */
    int prepareTarget(@Param("sessionId") UUID sessionId, @Param("partNumber") int partNumber,
                      @Param("sha256") String sha256, @Param("attempt") int attempt);

    /**
     * 设置文件上传分片。
     *
     * @param sessionId  会话标识。
     * @param partNumber 分片编号参数。
     * @param size       大小参数。
     * @param sha256     文件内容的 SHA-256 摘要。
     * @param etag       ETag参数。
     * @return 符合条件的数量。
     */
    int markUploaded(@Param("sessionId") UUID sessionId, @Param("partNumber") int partNumber,
                     @Param("size") long size, @Param("sha256") String sha256, @Param("etag") String etag);

    /**
     * 设置已确认状态。
     *
     * @param sessionId  会话标识。
     * @param partNumber 分片编号参数。
     * @param size       大小参数。
     * @param sha256     文件内容的 SHA-256 摘要。
     * @param etag       ETag参数。
     * @return 符合条件的数量。
     */
    int markConfirmed(@Param("sessionId") UUID sessionId, @Param("partNumber") int partNumber,
                      @Param("size") long size, @Param("sha256") String sha256, @Param("etag") String etag);

    /**
     * 设置外部已确认状态。
     *
     * @param sessionId  会话标识。
     * @param partNumber 分片编号参数。
     * @param size       大小参数。
     * @param sha256     文件内容的 SHA-256 摘要。
     * @param etag       ETag参数。
     * @return 符合条件的数量。
     */
    int markExternalConfirmed(@Param("sessionId") UUID sessionId, @Param("partNumber") int partNumber,
                              @Param("size") long size, @Param("sha256") String sha256, @Param("etag") String etag);
}
