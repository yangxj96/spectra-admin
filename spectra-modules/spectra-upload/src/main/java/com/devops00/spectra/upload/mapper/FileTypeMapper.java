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
import com.devops00.spectra.common.mybatis.PgJsonbNodeTypeHandler;
import com.devops00.spectra.upload.javabean.entity.FileType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.UUID;

/** 文件类型 Mapper。 */
@Mapper
public interface FileTypeMapper extends BaseMapper<FileType> {

    @Results({
            @Result(column = "allowed_extensions", property = "allowedExtensions", typeHandler = PgJsonbNodeTypeHandler.class),
            @Result(column = "allowed_content_types", property = "allowedContentTypes", typeHandler = PgJsonbNodeTypeHandler.class),
            @Result(column = "magic_rules", property = "magicRules", typeHandler = PgJsonbNodeTypeHandler.class)
    })
    @Select("SELECT * FROM spectra_core.file_type WHERE code = #{code} AND deleted IS NULL AND enabled = true LIMIT 1")
    FileType findEnabledByCode(@Param("code") String code);

    @Results({
            @Result(column = "allowed_extensions", property = "allowedExtensions", typeHandler = PgJsonbNodeTypeHandler.class),
            @Result(column = "allowed_content_types", property = "allowedContentTypes", typeHandler = PgJsonbNodeTypeHandler.class),
            @Result(column = "magic_rules", property = "magicRules", typeHandler = PgJsonbNodeTypeHandler.class)
    })
    @Select("SELECT * FROM spectra_core.file_type WHERE enabled = true AND upload_enabled = true "
            + "AND deleted IS NULL AND EXISTS (SELECT 1 FROM jsonb_array_elements_text(allowed_content_types) AS item(value) "
            + "WHERE LOWER(item.value) = LOWER(#{contentType})) ORDER BY id LIMIT 1")
    FileType findEnabledByContentType(@Param("contentType") String contentType);

    @Results({
            @Result(column = "allowed_extensions", property = "allowedExtensions", typeHandler = PgJsonbNodeTypeHandler.class),
            @Result(column = "allowed_content_types", property = "allowedContentTypes", typeHandler = PgJsonbNodeTypeHandler.class),
            @Result(column = "magic_rules", property = "magicRules", typeHandler = PgJsonbNodeTypeHandler.class)
    })
    @Select("SELECT * FROM spectra_core.file_type WHERE id = #{id} AND deleted IS NULL LIMIT 1")
    FileType findByIdIncludingDisabled(@Param("id") UUID id);
}
