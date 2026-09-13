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

/** 上传分片 Mapper。 */
@Mapper
public interface FileUploadPartMapper extends BaseMapper<FileUploadPart> {

    List<FileUploadPart> findBySessionId(@Param("sessionId") UUID sessionId);

    FileUploadPart selectForUpdate(@Param("sessionId") UUID sessionId, @Param("partNumber") int partNumber);

    int countConfirmed(@Param("sessionId") UUID sessionId);

    int prepareTarget(@Param("sessionId") UUID sessionId, @Param("partNumber") int partNumber,
                      @Param("sha256") String sha256, @Param("attempt") int attempt);

    int markUploaded(@Param("sessionId") UUID sessionId, @Param("partNumber") int partNumber,
                     @Param("size") long size, @Param("sha256") String sha256, @Param("etag") String etag);

    int markConfirmed(@Param("sessionId") UUID sessionId, @Param("partNumber") int partNumber,
                      @Param("size") long size, @Param("sha256") String sha256, @Param("etag") String etag);

    int markExternalConfirmed(@Param("sessionId") UUID sessionId, @Param("partNumber") int partNumber,
                              @Param("size") long size, @Param("sha256") String sha256, @Param("etag") String etag);
}
