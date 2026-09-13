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
import com.devops00.spectra.core.upload.javabean.entity.FileType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

/**
 * 文件类型 Mapper。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Mapper
public interface FileTypeMapper extends BaseMapper<FileType> {

    /**
     * 查询启用状态编码。
     *
     * @param code 编码参数。
     * @return 文件类型数据。
     */
    FileType findEnabledByCode(@Param("code") String code);

    /**
     * 查询启用状态内容类型。
     *
     * @param contentType 内容类型参数。
     * @return 文件类型数据。
     */
    FileType findEnabledByContentType(@Param("contentType") String contentType);

    /**
     * 查询标识停用状态。
     *
     * @param id 数据记录的唯一标识。
     * @return 文件类型数据。
     */
    FileType findByIdIncludingDisabled(@Param("id") UUID id);
}
