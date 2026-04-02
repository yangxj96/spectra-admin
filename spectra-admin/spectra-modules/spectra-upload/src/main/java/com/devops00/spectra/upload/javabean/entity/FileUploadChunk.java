package com.devops00.spectra.upload.javabean.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.*;

/// 文件分片信息实体
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/8 00:03
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "file_upload_chunk")
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

    /// 分片状态(UPLOADED(已上传)/FAILED(上传失败)
    @TableField(value = "status")
    private String status;

}
