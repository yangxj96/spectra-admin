package com.devops00.spectra.upload.javabean.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.upload.javabean.constant.UploadType;
import lombok.*;

/// 文件信息实体
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/8 00:03
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "file_info")
public class FileInfo extends BaseEntity {

    /// 存储文件名(系统生成)
    @TableField(value = "filename")
    private String filename;

    /// 原始文件名
    @TableField(value = "original_name")
    private String originalName;

    /// 文件类型(MIME)
    @TableField(value = "content_type")
    private String contentType;

    /// 文件大小(字节)
    @TableField(value = "size")
    private Long size;

    /// 文件哈希(MD5/SHA256，用于秒传)
    @TableField(value = "hash")
    private String hash;

    /// 存储类型(LOCAL/S3/OSS)
    @TableField(value = "storage_type")
    private UploadType storageType;

    /// 文件状态(ACTIVE/DELETED)
    @TableField(value = "status")
    private String status;

    /// 引用计数(用于秒传共享文件)
    @TableField(value = "ref_count")
    private Integer refCount;

}
