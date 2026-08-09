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
import com.devops00.spectra.upload.javabean.constant.UploadType;
import lombok.*;

import java.util.UUID;

/**
 * 文件上传-上传任务表
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/4/1 17:24
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "file_upload_task", schema = "spectra_upload")
public class FileUploadTask extends BaseEntity {

    /**
     * 上传任务ID（前端使用的唯一标识）
     */
    @TableField(value = "upload_id")
    private String uploadId;

    /**
     * 文件名
     */
    @TableField(value = "filename")
    private String filename;

    /**
     * 文件哈希（用于秒传判断）
     */
    @TableField(value = "hash")
    private String hash;

    /**
     * 文件总大小（字节）
     */
    @TableField(value = "size")
    private Long size;

    /**
     * 分片大小（字节）
     */
    @TableField(value = "chunk_size")
    private Long chunkSize;

    /**
     * 总分片数
     */
    @TableField(value = "total_chunks")
    private Integer totalChunks;

    /**
     * 存储类型（LOCAL/S3/OSS）
     */
    @TableField(value = "storage_type")
    private UploadType storageType;

    /**
     * 上传状态（INIT(初始化)/UPLOADING(上传中)/MERGING(合并中)/DONE(完成)/FAILED(失败)）
     */
    @TableField(value = "status")
    private String status;

    /**
     * S3协议扩展ID
     */
    @TableField(value = "eid")
    private String eid;

    /**
     * 关联文件ID（上传完成后生成）
     */
    @TableField(value = "file_id")
    private UUID fileId;
}
