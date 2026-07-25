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

package com.devops00.spectra.upload.javabean.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.*;

/// 文件分片信息实体
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/12/8 00:03
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "file_upload_chunk", schema = "spectra_upload")
public class FileUploadChunk extends BaseEntity {

    /// 上传任务ID
    @TableField(value = "upload_id")
    private String uploadId;

    /// 分片序号(从1开始)
    @TableField(value = "chunk_number")
    private Integer chunkNumber;

    /// 分片标识(用于S3/OSS合并)
    @TableField(value = "etag")
    private String etag;

    /// 分片大小(字节)
    @TableField(value = "size")
    private Long size;

}
